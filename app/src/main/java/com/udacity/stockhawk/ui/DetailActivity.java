package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/**
 * Created by jaime on 11-May-17.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int STOCK_DETAIL_LOADER = 1;

    @BindView(R.id.chart)
    LineChart chart;

    private String mSymbol;
    private List<Entry> mEntriesPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);
        ButterKnife.bind(this);

        if(getIntent().getExtras().getString(MainActivity.STOCK_SYMBOL) != null){
            mSymbol = getIntent().getExtras().getString(MainActivity.STOCK_SYMBOL);
            getSupportActionBar().setTitle(mSymbol);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportLoaderManager().initLoader(STOCK_DETAIL_LOADER,null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(getCallingActivity() != null){

                    onBackPressed();
                    return true;
                }else{
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void renderChart(Cursor data){
        mEntriesPrice = new ArrayList<Entry>();
        String[] historyData = null;
        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()){
            float price = data.getFloat(data.getColumnIndexOrThrow(Contract.Quote.COLUMN_PRICE));
            float absoluteChange = data.getFloat(data.getColumnIndexOrThrow(Contract.Quote.COLUMN_ABSOLUTE_CHANGE));
            float percentChange = data.getFloat(data.getColumnIndexOrThrow(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));
            String history = data.getString(data.getColumnIndexOrThrow(Contract.Quote.COLUMN_HISTORY));
            historyData = history.split("\n");
            //mEntriesPrice.add(new Entry(price, Float.parseFloat(time)));
        }
        for (int i = 0; i < historyData.length; i++){
            String value = historyData[i];
            String[] priceAndTimeValues = value.split(",");
            long x = Long.parseLong(priceAndTimeValues[0]);
            float y = Float.parseFloat(priceAndTimeValues[1]);
            mEntriesPrice.add(new Entry(x,y));
        }

        Collections.sort(mEntriesPrice, new EntryXComparator());
        LineDataSet dataSet = new LineDataSet(mEntriesPrice, "Time");
        dataSet.setColor(R.color.colorPrimary);
        dataSet.setValueTextColor(R.color.colorAccent);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                Contract.Quote.COLUMN_SYMBOL + "= ?",
                new String[]{mSymbol},
                Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() >= 0) renderChart(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
