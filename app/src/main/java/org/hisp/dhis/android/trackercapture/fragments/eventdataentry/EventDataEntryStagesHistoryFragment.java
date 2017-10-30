package org.hisp.dhis.android.trackercapture.fragments.eventdataentry;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.persistence.models.ProgramStageEventDataElements;
import org.hisp.dhis.android.trackercapture.R;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStagesEventsTable;
import org.hisp.dhis.android.sdk.ui.activities.OnBackPressedListener;

import java.util.ArrayList;

/*
 * Created by John Melin on 11/10/2017.
 */

public class EventDataEntryStagesHistoryFragment extends Fragment implements OnBackPressedListener {
    public static final String TAG = EventDataEntryStagesHistoryFragment.class.getSimpleName();

    private LinearLayout mainLayout;
    private LinearLayout historyColumns;
    private ProgramStagesEventsTable programStagesEventsTable;
    private LayoutInflater inflater;
    public static final String PROGRAM_EVENTS_HISTORY = "extra:ProgramEventsHistory";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.inflater = inflater;

        mainLayout = (LinearLayout) inflater.inflate(R.layout.fragment_events_history, container, false);
        historyColumns = (LinearLayout) mainLayout.findViewById(R.id.table);

        setCloseButtonListener();
        setupHistoryTable();

        return mainLayout;
    }

    public void setCloseButtonListener() {
        Button closeButton = (Button) mainLayout.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBack();
            }
        });
    }

    public void setupHistoryTable() {
        if(programStagesEventsTable == null) return;
        int columnLength = getColumnLength();

        // TODO sorting should rather be done here than in ProgramEventQuery for higher control and safety

        addPreviousPregnanciesStageColumn(columnLength);

        addANCLabelColumn(columnLength);

        addANCStageColumns(columnLength);
    }

    private int getColumnLength(){
        int columnLength = 0;
        for(ProgramStageEventDataElements event : programStagesEventsTable.getProgramStageEventValues()) {
            int tempLength = event.getColumnLength();
            if (tempLength > columnLength) columnLength = tempLength;
        }
        return columnLength;
    }

    private void addPreviousPregnanciesStageColumn(int columnLength){
        for(ProgramStageEventDataElements event : programStagesEventsTable.getProgramStageEventValues()) {
            if(event.getStageName().equals("Previous pregnancies")){

                String stageName = event.getStageName();
                String date = event.getDateValue();
                ArrayList<String> dataElementNames = event.getDataElementNames();
                ArrayList<String> dataElementValues = event.getDataElementValues();

                historyColumns.addView(createEventStageColumn(stageName, date,
                        dataElementNames, dataElementValues, columnLength));

                historyColumns.addView(createSideLineView());
                historyColumns.addView(createSideSeparatorView());
                historyColumns.addView(createSideLineView());

                programStagesEventsTable.getProgramStageEventValues().remove(event);
            }
        }
    }

    public void addANCLabelColumn(int columnLength){
        String label = getBoldString("ANC");

        ArrayList<String> dataElementNames = getANC1DataElementNames();
        ArrayList<String> dataElementValues = getEmptyStringArrayList(dataElementNames.size());

        historyColumns.addView(createEventStageColumn(label, "",
                dataElementNames, dataElementValues, columnLength));
        historyColumns.addView(createSideLineView());
    }

    private void addANCStageColumns(int columnLength){
        for(ProgramStageEventDataElements event : programStagesEventsTable.getProgramStageEventValues()) {
            String stageName = event.getStageName();
            String date = event.getDateValue();

            //ArrayList<String> dataElementNames = event.getDataElementNames();
            ArrayList<String> dataElementValues = event.getDataElementValues();
            ArrayList<String> dataElementNames = getEmptyStringArrayList(dataElementValues.size());

            historyColumns.addView(createEventStageColumn(stageName, date,
                    dataElementNames, dataElementValues, columnLength));
            historyColumns.addView(createSideLineView());
        }
    }

    private LinearLayout createEventStageColumn(String stageName,
                                                String date,
                                                ArrayList<String> dataElementNames,
                                                ArrayList<String> dataElementValues,
                                                int columnLength) {
        LinearLayout column = createColumn();
        String label = getBoldString(stageName) + getNewLineString(date);
        column.addView(createTextBox(label, R.layout.text_box_blue));

        addDataElementBoxesToColumn(column, dataElementNames, dataElementValues, columnLength);
        return column;
    }

    private void addDataElementBoxesToColumn(LinearLayout column,
                                             ArrayList<String> dataElementNames,
                                             ArrayList<String> dataElementValues,
                                             int columnLength) {
        for(int i = 0; i < columnLength; i++) {
            if(pastDataPoint(i, dataElementValues)) addEmptyLabel(column);
            else addLabelBoxToColumn(column, dataElementNames, dataElementValues, i);
        }
    }

    private void addLabelBoxToColumn(LinearLayout column,
                                  ArrayList<String> dataElementNames,
                                  ArrayList<String> dataElementValues,
                                  int i) {
        String label = getBoldStringWithFontSize(dataElementNames.get(i), 10);
        String value = getNewLineString(dataElementValues.get(i));
        column.addView(createTextBox(label + value, R.layout.text_box_white));
    }

    private void addEmptyLabel(LinearLayout column) {
        column.addView(createTextBox("", R.layout.text_box_white));
    }

    private RelativeLayout createTextBox(String label, int layout){
        RelativeLayout textBox = (RelativeLayout) inflater.inflate(layout, null);
        textBox.setLayoutParams(getTextBoxLayoutParams());

        TextView text = (TextView) textBox.findViewById(R.id.textView);
        text.setText(Html.fromHtml(label));
        return textBox;
    }

    private LinearLayout createColumn(){
        LinearLayout column = (LinearLayout) inflater.inflate(R.layout.text_box_column, null);
        column.setLayoutParams(getColumnLayoutParams());
        return column;
    }

    private View createSideSeparatorView() {
        View line = new View(getContext());
        line.setBackgroundColor(Color.WHITE);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                35, LinearLayout.LayoutParams.MATCH_PARENT));
        return line;
    }

    private View createSideLineView() {
        View line = new View(getContext());
        line.setBackgroundColor(Color.BLACK);
        line.setLayoutParams(new LinearLayout.LayoutParams(
                1, LinearLayout.LayoutParams.MATCH_PARENT));
        return line;
    }

    private ArrayList<String> getEmptyStringArrayList(int length) {
        ArrayList<String> list = new ArrayList<>();
        for(int i = 0; i < length; i++) list.add("");
        return list;
    }

    public ArrayList<String> getANC1DataElementNames(){
        ArrayList<String> dataElementNames = new ArrayList<>();

        for(ProgramStageEventDataElements event : programStagesEventsTable.getProgramStageEventValues()) {
            if(event.getStageName().equals("ANC1")){
                dataElementNames = event.getDataElementNames();
            }
        }
        return dataElementNames;
    }

    public void setProgramStagesEventsTable(ProgramStagesEventsTable programStagesEventsTable) {
        this.programStagesEventsTable = programStagesEventsTable;
    }

    private LinearLayout.LayoutParams getTextBoxLayoutParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f);
    }

    private LinearLayout.LayoutParams getColumnLayoutParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f);
    }

    private boolean hasValue(ArrayList<String> dataElementValues, int i){
        return !dataElementValues.get(i).isEmpty();
    }

    private boolean pastDataPoint(int i, ArrayList<String> dataElementValues) {
        return i > (dataElementValues.size()-1);
    }

    private String getBoldStringWithFontSize(String text, int size ){
        return "<b><font size=+"+ size + ">" + text + "</font></b>";
    }

    private String getBoldString(String text) {
        return "<b>" + text + "</b>";
    }

    private String getNewLineString(String text) {
        return "<br>" + text + "</br>";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean doBack() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return false;
    }
}
