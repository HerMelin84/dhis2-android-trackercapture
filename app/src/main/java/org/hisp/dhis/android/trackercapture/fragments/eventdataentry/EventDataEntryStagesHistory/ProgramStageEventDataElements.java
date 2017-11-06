package org.hisp.dhis.android.trackercapture.fragments.eventdataentry.EventDataEntryStagesHistory;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.utils.Utils;

public class ProgramStageEventDataElements implements Comparable<ProgramStageEventDataElements>, Serializable {
    private String reportingDateString;
    private String stageName;
    private ArrayList<String> dateElementNames;
    private ArrayList<String> dataElementValues;
    private int columnLength;

    public ProgramStageEventDataElements(String reportingDate,
                                   String stageName,
                                   List<DataValue> dataValues,
                                   List<ProgramStageDataElement> dataElements) {
        this.reportingDateString = Utils.removeTimeFromDateString(reportingDate);
        this.stageName = stageName;
        this.dataElementValues = new ArrayList<>();
        this.dateElementNames = new ArrayList<>();
        setDataElements(dataElements, dataValues);
        columnLength = dataElementValues.size();
    }

    public ArrayList<String> getDataElementValues() {
        return dataElementValues;
    }

    public ArrayList<String> getDataElementNames() {
        return dateElementNames;
    }

    private void setDataElements(List<ProgramStageDataElement> dataElements,
                                 List<DataValue> dataValues) {
        for (ProgramStageDataElement stageDataElement: dataElements) {
            String dataValue = getDataValue(stageDataElement.getDataelement(), dataValues);
            String dataElement = getDataElement(stageDataElement);

            addDataRow(dataElement, dataValue);
        }
    }

    private String getDataValue(String dataElement, List<DataValue> dataValues) {
        for (DataValue dataValue : dataValues) {
            if (dataValue.getDataElement().equals(dataElement)) {
                String value = dataValue.getValue();
                switch (value) {
                    case "true": return "Yes";
                    case "false": return "No";
                    case "": return "no value";
                }
                return dataValue.getValue();
            }
        }
        return "no value";
    }

    int getColumnLength() {
        return columnLength;
    }

    private String getDataElement(ProgramStageDataElement stageDataElement) {
        return stageDataElement.getDataElement().getDisplayName();
    }

    private void addDataRow(String dataElementName, String dataElementValue) {
        dateElementNames.add(dataElementName);
        dataElementValues.add(dataElementValue);
    }

    String getDateValue() {
        return reportingDateString;
    }

    String getStageName() {
        return stageName;
    }

    @Override
    public int compareTo(@NonNull ProgramStageEventDataElements eventValues) {
        return getStageName().compareTo(eventValues.getStageName());
    }

}
