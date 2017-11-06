/*
 *  Copyright (c) 2016, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.trackercapture.fragments.programoverview;

import android.content.Context;

import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.persistence.loaders.Query;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.trackercapture.fragments.eventdataentry.EventDataEntryStagesHistory.ProgramStageEventDataElements;
import org.hisp.dhis.android.trackercapture.fragments.eventdataentry.EventDataEntryStagesHistory.ProgramStagesEventsTable;
import org.hisp.dhis.android.sdk.persistence.models.ProgramIndicator;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.IndicatorRow;
import org.hisp.dhis.android.sdk.utils.Utils;
import org.hisp.dhis.android.sdk.utils.comparators.EventDateComparator;
import org.hisp.dhis.android.sdk.utils.services.ProgramIndicatorService;
import org.hisp.dhis.android.trackercapture.ui.rows.programoverview.ProgramStageEventRow;
import org.hisp.dhis.android.trackercapture.ui.rows.programoverview.ProgramStageLabelRow;
import org.hisp.dhis.android.trackercapture.ui.rows.programoverview.ProgramStageRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class ProgramOverviewFragmentQuery implements Query<ProgramOverviewFragmentForm> {
    public static final String CLASS_TAG = ProgramOverviewFragmentQuery.class.getSimpleName();

    private final String mProgramId;
    private final long mTrackedEntityInstanceId;
    private ProgramOverviewFragmentForm programOverviewFragmentForm;

    ProgramOverviewFragmentQuery(String programId, long trackedEntityInstanceId) {
        mProgramId = programId;
        mTrackedEntityInstanceId = trackedEntityInstanceId;
    }

    @Override
    public ProgramOverviewFragmentForm query(Context context) {
        programOverviewFragmentForm = getInitProgramOverviewForm();
        TrackedEntityInstance trackedEntityInstance = programOverviewFragmentForm.getTrackedEntityInstance();

        if(trackedEntityInstance == null) return programOverviewFragmentForm;

        Enrollment activeEnrollment =
                getActiveEnrollment(TrackerController.getEnrollments(mProgramId, trackedEntityInstance));

        if (activeEnrollment == null) return programOverviewFragmentForm;

        List<TrackedEntityAttributeValue> attributeValues =
                getVisibleTrackedEntityAttributeValues(trackedEntityInstance);

        // Set all remaining data
        setProgramOverviewEnrollmentDate(activeEnrollment);

        setTrackedEntityAttributeValues(attributeValues);

        setProgramStageRows(activeEnrollment);

        setProgramIndicatorRows();

        setProgramStagesEventsTable(activeEnrollment);

        return programOverviewFragmentForm;
    }

    private void setProgramStageRows(Enrollment activeEnrollment) {
        List<ProgramStageRow> programStageRows = getProgramStageRows(activeEnrollment);
        programOverviewFragmentForm.setProgramStageRows(programStageRows);
    }


    private List<ProgramStageRow> getProgramStageRows(Enrollment enrollment) {
        List<Event> events = enrollment.getEvents(true);
        HashMap<String, List<Event>> eventsByStage = getEventsByStage(events);

        Program program = MetaDataController.getProgram(mProgramId);
        List<ProgramStage> programStages = program.getProgramStages();
        List<ProgramStageRow> rows = new ArrayList<>();

        addProgramStagesWithEventsToRows(programStages, eventsByStage, rows);
        return rows;
    }

    private void addProgramStagesWithEventsToRows(List<ProgramStage> programStages,
                                                  HashMap<String, List<Event>> eventsByStage,
                                                  List<ProgramStageRow> rows) {
        for(ProgramStage programStage: programStages) {
            List<Event> eventsForStage = eventsByStage.get(programStage.getUid());

            ProgramStageLabelRow labelRow = new ProgramStageLabelRow(programStage);
            rows.add(labelRow);

            if(eventsForStage == null) continue;

            EventDateComparator comparator = new EventDateComparator();
            Collections.sort(eventsForStage, comparator);

            addEventsToProgramStageRow(eventsForStage, labelRow, rows);
        }
    }

    private void addEventsToProgramStageRow(List<Event> eventsForStage,
                                            ProgramStageLabelRow labelRow,
                                            List<ProgramStageRow> rows) {
        for(Event event: eventsForStage) {
            ProgramStageEventRow row = new ProgramStageEventRow(event);
            row.setLabelRow(labelRow);
            labelRow.getEventRows().add(row);
            rows.add(row);
        }
    }

    private HashMap<String, List<Event>> getEventsByStage(List<Event> events) {
        HashMap<String, List<Event>> eventsByStage = new HashMap<>();
        for(Event event: events) addEventsByStage(eventsByStage, event);

        return eventsByStage;
    }

    private void addEventsByStage(HashMap<String, List<Event>> eventsByStage, Event event) {
        List<Event> eventsForStage = eventsByStage.get(event.getProgramStageId());

        if(eventsForStage == null) {
            eventsForStage = new ArrayList<>();
            eventsByStage.put(event.getProgramStageId(), eventsForStage);
        }

        eventsForStage.add(event);
    }

    private ProgramOverviewFragmentForm getInitProgramOverviewForm() {
        ProgramOverviewFragmentForm programOverviewFragmentForm = new ProgramOverviewFragmentForm();

        TrackedEntityInstance trackedEntityInstance = TrackerController.getTrackedEntityInstance(mTrackedEntityInstanceId);
        Program program = MetaDataController.getProgram(mProgramId);

        programOverviewFragmentForm.setTrackedEntityInstance(trackedEntityInstance);
        programOverviewFragmentForm.setProgramIndicatorRows(new LinkedHashMap<ProgramIndicator, IndicatorRow>());
        programOverviewFragmentForm.setProgram(program);
        programOverviewFragmentForm.setDateOfEnrollmentLabel(program.getEnrollmentDateLabel());
        programOverviewFragmentForm.setIncidentDateLabel(program.getIncidentDateLabel());
        return programOverviewFragmentForm;
    }

    private Enrollment getActiveEnrollment(List<Enrollment> enrollments) {
        Enrollment activeEnrollment = null;
        if(enrollments == null) return null;

        for(Enrollment enrollment: enrollments) {
            if(enrollment.getStatus().equals(Enrollment.ACTIVE)) {
                activeEnrollment = enrollment;
            }
        }
        return activeEnrollment;
    }

    private void setProgramOverviewEnrollmentDate(Enrollment activeEnrollment) {
        programOverviewFragmentForm.setEnrollment(activeEnrollment);
        programOverviewFragmentForm.setDateOfEnrollmentValue(Utils.removeTimeFromDateString(activeEnrollment.getEnrollmentDate()));
        programOverviewFragmentForm.setIncidentDateValue(Utils.removeTimeFromDateString(activeEnrollment.getIncidentDate()));
    }

    private void setTrackedEntityAttributeValues(List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        if(trackedEntityAttributeValues == null) return;
        int attributeSize = trackedEntityAttributeValues.size();

        if(attributeSize > 0) {
            TrackedEntityAttributeValue teav = trackedEntityAttributeValues.get(0);
            String teavId = teav.getTrackedEntityAttributeId();
            String teavName = MetaDataController.getTrackedEntityAttribute(teavId).getName();
            String teavValue = trackedEntityAttributeValues.get(0).getValue();
            programOverviewFragmentForm.setAttribute1Label(teavName);
            programOverviewFragmentForm.setAttribute1Value(teavValue);
        }

        if(attributeSize > 1) {
            TrackedEntityAttributeValue teav = trackedEntityAttributeValues.get(1);
            String teavId = teav.getTrackedEntityAttributeId();
            String teavName = MetaDataController.getTrackedEntityAttribute(teavId).getName();
            String teavValue = trackedEntityAttributeValues.get(1).getValue();
            programOverviewFragmentForm.setAttribute2Label(teavName);
            programOverviewFragmentForm.setAttribute2Value(teavValue);
        }
    }

    private void setProgramIndicatorRows() {
        List<ProgramIndicator> programIndicators =
                programOverviewFragmentForm.getProgram().getProgramIndicators();
        Map<ProgramIndicator, IndicatorRow> programIndicatorRows =
                programOverviewFragmentForm.getProgramIndicatorRows();

        if(programIndicators != null ) {
            Enrollment enrollment = programOverviewFragmentForm.getEnrollment();

            for(ProgramIndicator programIndicator : programIndicators) {
                String value = ProgramIndicatorService.getProgramIndicatorValue(enrollment, programIndicator);

                if(!programIndicator.isDisplayInForm() || value == null) continue;

                String displayDescription = programIndicator.getDisplayDescription();
                IndicatorRow indicatorRow = new IndicatorRow(programIndicator, value, displayDescription);

                programIndicatorRows.put(programIndicator, indicatorRow);
            }

        } else programIndicatorRows.clear();
    }

    private void setProgramStagesEventsTable(Enrollment enrollment) {
        List<Event> events = enrollment.getEvents(true);
        ArrayList<ProgramStageEventDataElements > programStageEventsDataElements = new ArrayList<>();

        //HISTORY only needs to retrieve completed events
        for (Event event : events) {
            if(event.getStatus().equals("COMPLETED")) {
                programStageEventsDataElements.add(getEventDataElements(event));
            }
        }

        // TODO This sorting only works for the hardcoded ANC events as it sorts by name. In the future date sorting needs to be added.
        Collections.sort(programStageEventsDataElements);
        ProgramStagesEventsTable programStagesEventsTable = new ProgramStagesEventsTable(programStageEventsDataElements);
        programOverviewFragmentForm.setProgramStagesEventsTable(programStagesEventsTable);
    }

    private ProgramStageEventDataElements getEventDataElements(Event event) {
        String completedDate = event.getEventDate();
        ProgramStage programStage = MetaDataController.getProgramStage(event.getProgramStageId());
        String stageName = programStage.getDisplayName();

        List<ProgramStageDataElement> dataElements = programStage.getProgramStageDataElements();
        List<DataValue> dataValues = event.getDataValues();

        return new ProgramStageEventDataElements(completedDate, stageName, dataValues, dataElements);
    }

    private List<TrackedEntityAttributeValue> getVisibleTrackedEntityAttributeValues(TrackedEntityInstance tei) {
        return TrackerController.getVisibleTrackedEntityAttributeValues(tei.getLocalId());
    }
}