package com.udacity.stockhawk;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by jaime on 14-May-17.
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return  new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class StockRemoteViewsFactory implements RemoteViewsFactory {

        private Context mContext;
        private int mAppWidgetId;
        private Cursor mCursor;
        private final DecimalFormat dollarFormatWithPlus;
        private final DecimalFormat dollarFormat;
        private final DecimalFormat percentageFormat;

        public StockRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");

        }

        @Override
        public void onCreate() {
            //no need to call cursor here since onDataSetChanged() get's called immediately after onCreate
        }

        @Override
        public void onDataSetChanged() {
            mCursor = mContext.getContentResolver().query(
                    Contract.Quote.URI,
                    null,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
        }

        @Override
        public void onDestroy() { mCursor.close(); }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            return drawWidget(i);
        }

        private RemoteViews drawWidget(int i){
            mCursor.moveToPosition(i);
            float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            String symbol = mCursor.getString(mCursor.getColumnIndexOrThrow(Contract.Quote.COLUMN_SYMBOL));

            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
            rv.setTextViewText(R.id.symbol, symbol);
            rv.setTextViewText(R.id.price, dollarFormat.format(mCursor.getFloat(mCursor.getColumnIndexOrThrow(Contract.Quote.COLUMN_PRICE))));


            if (rawAbsoluteChange > 0) {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                rv.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(mContext)
                    .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                rv.setTextViewText(R.id.change, change);
            } else {
                rv.setTextViewText(R.id.change, percentage);
            }

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(MainActivity.STOCK_SYMBOL, symbol);

            rv.setOnClickFillInIntent(R.id.list_item, fillInIntent);
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
