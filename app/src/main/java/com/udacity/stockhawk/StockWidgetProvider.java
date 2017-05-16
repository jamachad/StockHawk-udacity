package com.udacity.stockhawk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by jaime on 14-May-17.
 */

public class StockWidgetProvider extends AppWidgetProvider {
    public static final String DETAIL_ACTION = "com.udacity.stockhawk.stockwidgetprovider.DETAIL_ACTION";


    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(DETAIL_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String symbol = intent.getStringExtra(MainActivity.STOCK_SYMBOL);
            Intent detailIntent = new Intent(context, DetailActivity.class);
            detailIntent.putExtra(MainActivity.STOCK_SYMBOL, symbol);
            context.startActivity(detailIntent);
            //Toast.makeText(context, "Touched view " + symbol, Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];


            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
            //rv.setOnClickPendingIntent(R.id.container, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            //appWidgetManager.updateAppWidget(appWidgetId, views);


            //----------------------------------------

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intentWidgetService = new Intent(context, StockWidgetService.class);
            // Add the app widget ID to the intent extras.
            intentWidgetService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intentWidgetService.setData(Uri.parse(intentWidgetService.toUri(Intent.URI_INTENT_SCHEME)));

            // Instantiate the RemoteViews object for the app widget layout.
            //RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.stock_widget);

            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(R.id.list_view_stocks, intentWidgetService);

            //deprecated
            //rv.setRemoteAdapter(appWidgetIds[i], R.id.list_view_stocks, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.list_view_stocks, R.id.empty_view);

            //
            // Do additional processing specific to this app widget...
            //

            Intent detailIntent = new Intent(context, StockWidgetProvider.class);
            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            detailIntent.setAction(StockWidgetProvider.DETAIL_ACTION);
            detailIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            //intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent pendingIntentService = PendingIntent.getBroadcast
                    (context, appWidgetIds[i], detailIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            rv.setPendingIntentTemplate(R.id.list_view_stocks, pendingIntentService);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);


    }
}
