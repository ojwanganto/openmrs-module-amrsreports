package org.openmrs.module.amrsreports.reporting.converter;

import org.h2.java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.amrsreports.rule.MohEvaluableNameConstants;
import org.openmrs.module.amrsreports.snapshot.ARVPatientSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A test class for ARVPatientSnapshotARTReasonConverter
 */
public class ARVPatientSnapshotARTReasonConverterTest {
    @Test
    public void shouldTestForClinicalStagingOnly() throws Exception {
        ARVPatientSnapshot s = new ARVPatientSnapshot();
        List<String> extras = new ArrayList<String>();
        extras.add("WHO Stage 4");
        s.set("reason",ARVPatientSnapshot.REASON_CLINICAL);
        s.set("extras",extras);

        Assert.assertEquals("CLINICAL"
                + "\n" + "WHO Stage 4",new ARVPatientSnapshotARTReasonConverter().convert(s));



    }

    @Test
    public void shouldTestForCD4Only() throws Exception {
        ARVPatientSnapshot s = new ARVPatientSnapshot();
        List<String> extras = new ArrayList<String>();
        extras.add("WHO Stage 4");
        extras.add("CD4 Count: 500");
        extras.add("CD4 %:5");
        s.set("reason","CD4");
        s.set("extras",extras);

        Assert.assertEquals("CD4"
                + "\n" + "CD4 Count: 500",new ARVPatientSnapshotARTReasonConverter().convert(s));



    }

    @Test
    public void shouldTestForCD4andClinical() throws Exception {
        ARVPatientSnapshot s = new ARVPatientSnapshot();
        List<String> extras = new ArrayList<String>();
        extras.add("WHO Stage 4");
        extras.add("CD4 Count: 500");
        extras.add("CD4 %:5");
        s.set("reason",ARVPatientSnapshot.REASON_CLINICAL_CD4);
        s.set("extras",extras);

        Assert.assertEquals("WHO Stage 4"
                + "\n" + "CD4 Count: 500",new ARVPatientSnapshotARTReasonConverter().convert(s));

    }

    @Test
    public void shouldTestForTransferIn() throws Exception {
        ARVPatientSnapshot s = new ARVPatientSnapshot();
        List<String> extras = new ArrayList<String>();
        extras.add("WHO Stage 4");
        extras.add("CD4 Count: 500");
        extras.add("CD4 %:5");
        s.set("reason",ARVPatientSnapshot.REASON_CLINICAL);
        s.set("ti","TI");
        s.set("extras",extras);

        Assert.assertEquals( "TI"
                + "\n" + "CLINICAL",new ARVPatientSnapshotARTReasonConverter().convert(s));

    }
}
