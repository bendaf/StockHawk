package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChartActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ChartActivity.class.getSimpleName();

    public static final String EXTRA_CHART_SYMBOL = "extra chart symbol";
    private static final int SYMBOL_LOADER = 1;
    @BindView(R.id.chart) LineChart chart;

    List<Entry> entries = new ArrayList<>();
    String mSymbol = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        ButterKnife.bind(this);
        if(getIntent().hasExtra(EXTRA_CHART_SYMBOL)) {
            mSymbol = getIntent().getExtras().getString(EXTRA_CHART_SYMBOL);
        }
        getSupportLoaderManager().initLoader(SYMBOL_LOADER, null, this);
    }

    @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(mSymbol),
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, null);
    }

    @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: " + mSymbol);
        if(!mSymbol.equals("") && entries.size() <= 0) {
            data.moveToFirst();
            String history[] = data.getString(Contract.Quote.POSITION_HISTORY).split("\\n");
            for(String h : history) {
                if(!h.equals("")) {
                    String line[] = h.split(", ");
                    entries.add(new Entry(Long.parseLong(line[0]), Float.parseFloat(line[1])));
                }
            }
            if(entries.size() > 0) {
                Collections.sort(entries, new EntryXComparator());
                LineDataSet dataSet = new LineDataSet(entries, mSymbol);
                dataSet.setColor(getColorCompat(this, R.color.material_blue_500));
                chart.setData(new LineData(dataSet));
                XAxis xAxis = chart.getXAxis();
                xAxis.setValueFormatter(new IAxisValueFormatter() {
                    @Override public String getFormattedValue(float value, AxisBase axis) {

                        return (String) DateFormat.format("MM-dd", (long) value);
                    }
                });
                chart.invalidate();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @ColorInt public static int getColorCompat(Context c, @ColorRes int res) {
        if(Build.VERSION.SDK_INT >= 23) {
            return c.getColor(res);
        } else {
            return c.getResources().getColor(res);
        }
    }

    @Override public void onLoaderReset(Loader<Cursor> loader) {
        entries = new ArrayList<>();
    }
}
