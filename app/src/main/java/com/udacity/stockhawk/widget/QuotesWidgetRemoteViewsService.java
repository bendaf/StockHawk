package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.ChartActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by bendaf on 2017. 04. 20. StockHawk.
 *
 */

public class QuotesWidgetRemoteViewsService extends RemoteViewsService {
    @Override public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            DecimalFormat dollarFormatWithPlus;
            DecimalFormat dollarFormat;
            private Cursor data = null;

            @Override public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
            }

            @Override public void onDataSetChanged() {
                if(data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                Uri weatherForLocationUri = Contract.Quote.URI;
                data = getContentResolver().query(weatherForLocationUri,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override public void onDestroy() {
                if(data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override public RemoteViews getViewAt(int position) {

                if(position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_quote);

                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                views.setTextViewText(R.id.symbol, symbol);

                views.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                if(rawAbsoluteChange > 0) {
                    views.setViewVisibility(R.id.change2, View.GONE);
                    views.setViewVisibility(R.id.change, View.VISIBLE);
                } else {
                    views.setViewVisibility(R.id.change2, View.VISIBLE);
                    views.setViewVisibility(R.id.change, View.GONE);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                views.setTextViewText(R.id.change, change);
                views.setTextViewText(R.id.change2, change);

                setRemoteContentDescription(views, data.getString(Contract.Quote.POSITION_SYMBOL));
                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(ChartActivity.EXTRA_CHART_SYMBOL, symbol);
                views.setOnClickFillInIntent(R.id.ll_root, fillInIntent);
                return views;
            }

            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.symbol, description);
            }

            @Override public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @Override public int getViewTypeCount() {
                return 1;
            }

            @Override public long getItemId(int position) {
                if(data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override public boolean hasStableIds() {
                return true;
            }
        };
    }
}
