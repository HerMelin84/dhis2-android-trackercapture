package org.hisp.dhis.android.trackercapture.ui.rows.eventstageshistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.ui.adapters.rows.events.EventRow;
import org.hisp.dhis.android.trackercapture.R;
import org.hisp.dhis.android.trackercapture.ui.rows.upcomingevents.EventRowType;

/*
 * Created by John Melin on 17/10/2017.
 */

public class EventHistoryColumnNamesRow implements EventRow {
    private String mTitle;
    private String mFirstItem;
    private String mSecondItem;

    @Override
    public View getView(LayoutInflater inflater, View convertView, ViewGroup container) {
        View view = null;

        return view;
    }

    @Override
    public int getViewType() {
        return EventRowType.COLUMN_NAMES_ROW.ordinal();
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setSecondItem(String secondItem) {
        this.mSecondItem = secondItem;
    }

    public void setFirstItem(String firstItem) {
        this.mFirstItem = firstItem;
    }

    private static class ViewHolder {
        public final TextView title;
        public final TextView firstItem;
        public final TextView secondItem;


        private ViewHolder(TextView title, TextView firstItem,
                           TextView secondItem) {
            this.title = title;
            this.firstItem = firstItem;
            this.secondItem = secondItem;
        }
    }
}
