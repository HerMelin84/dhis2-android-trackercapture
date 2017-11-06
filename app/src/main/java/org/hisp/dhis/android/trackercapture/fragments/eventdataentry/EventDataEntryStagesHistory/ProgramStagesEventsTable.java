package org.hisp.dhis.android.trackercapture.fragments.eventdataentry.EventDataEntryStagesHistory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by John Melin on 11/10/2017.
 */

public class ProgramStagesEventsTable implements Serializable {
    private List<ProgramStageEventDataElements> programStageEventValues;
    private ArrayList<String> eventStageNames;
    private ArrayList<String> eventStageDates;
    private ArrayList<ArrayList<String>> eventStageValues;


    public ProgramStagesEventsTable(List<ProgramStageEventDataElements> programStageEventValues) {
        this.programStageEventValues = programStageEventValues;
        eventStageNames = addEventStageNames();
        eventStageDates = addEventStageDates();
    }

    private ArrayList<String> addEventStageNames() {
        ArrayList<String> names = new ArrayList<>();
        for(ProgramStageEventDataElements event : programStageEventValues){
            names.add(event.getStageName());
        }
        return names;
    }

    private ArrayList<String> addEventStageDates() {
        ArrayList<String> dates = new ArrayList<>();

        for(ProgramStageEventDataElements event : programStageEventValues){
            dates.add(event.getDateValue());
        }
        return dates;

    }

    public int getColumnLength() {
        int biggestLength = 0;

        for(ProgramStageEventDataElements elements : programStageEventValues) {
            int tempLength = elements.getColumnLength();

            if (tempLength > biggestLength){
                biggestLength = tempLength;
            }
        }

        return biggestLength;
    }

    public ArrayList<String> getEventStageNames() {
        return eventStageNames;
    }

    public ArrayList<String> getEventStageDates() {
        return eventStageDates;
    }

    public ArrayList<ArrayList<String>> getEventStageValueRows() {
        return eventStageValues;
    }

    public ArrayList<String> getValueRow(int index) {
        return eventStageValues.get(index);
    }

    public List<ProgramStageEventDataElements> getProgramStageEventValues() { return programStageEventValues; }
}
