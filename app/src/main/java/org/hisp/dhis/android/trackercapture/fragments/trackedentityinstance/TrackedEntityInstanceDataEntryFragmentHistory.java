package org.hisp.dhis.android.trackercapture.fragments.trackedentityinstance;

import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.IndicatorRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;

import java.util.List;
import java.util.Map;

/**
 * Created by John Melin on 09/10/2017.
 */

public class TrackedEntityInstanceDataEntryFragmentHistory {
    private Enrollment enrollment;
    private Program program;
    private TrackedEntityInstance trackedEntityInstance;
    private OrganisationUnit organisationUnit;

    private Map<String, TrackedEntityAttributeValue> trackedEntityAttributeValueMap;
    private Map<String, String> dataElementNames;
    private List<IndicatorRow> indicatorRows;
    private List<Row> dataEntryRows;

}
