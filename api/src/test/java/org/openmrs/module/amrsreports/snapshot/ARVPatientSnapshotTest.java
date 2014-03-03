package org.openmrs.module.amrsreports.snapshot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.amrsreports.cache.MohCacheUtils;
import org.openmrs.module.amrsreports.reporting.common.ObsRepresentation;
import org.openmrs.module.amrsreports.rule.MohEvaluableNameConstants;
import org.openmrs.module.amrsreports.MohTestUtils;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;

import java.util.Date;

/**
 * Test class for PatientSnapshot
 */
public class ARVPatientSnapshotTest extends BaseModuleContextSensitiveTest {

//	private static final ConceptService conceptService = Context.getConceptService();
	private static final ConceptService conceptService = null;

    @Before
    public void setUp() throws Exception{
        executeDataSet("datasets/concepts-who-stage.xml");
    }

	/**
	 * @verifies recognize and set WHO stage from an obs or specify peds WHO
	 * @see org.openmrs.module.amrsreports.snapshot.PatientSnapshot#consume(org.openmrs.Obs)
	 */
    @Test
    @Ignore
    public void shouldCheckContentOfTestDataset() throws Exception {
        TestUtil.printOutTableContents(getConnection(),"concept,concept_name");
    }

    @Test
    public void shouldConfirmAdultWhoStage(){
        ARVPatientSnapshot arvPatientSnapshot = new ARVPatientSnapshot();
        Obs adultStage2 = makeObs(MohEvaluableNameConstants.WHO_STAGE_ADULT, MohEvaluableNameConstants.WHO_STAGE_2_ADULT, MohTestUtils.makeDate("25 Jan 2010"));
        ObsRepresentation or = makeObsRepresentation(adultStage2);
        arvPatientSnapshot.consume(or);
        Assert.assertEquals(2, arvPatientSnapshot.get("adultWHOStage"));
    }

    @Test
    public void shouldConfirmPedsWhoStage(){
        ARVPatientSnapshot arvPatientSnapshot = new ARVPatientSnapshot();
        Obs pedStage2 = makeObs(MohEvaluableNameConstants.WHO_STAGE_PEDS, MohEvaluableNameConstants.WHO_STAGE_2_PEDS, MohTestUtils.makeDate("25 Jan 2010"));

        ObsRepresentation or = makeObsRepresentation(pedStage2);
        arvPatientSnapshot.consume(or);
        arvPatientSnapshot.consume(pedStage2);
        Assert.assertEquals(2, arvPatientSnapshot.get("pedsWHOStage"));
    }

    @Test
    public void shouldTestForHivDNAPCR(){
        ARVPatientSnapshot arvPatientSnapshot = new ARVPatientSnapshot();
        Obs obsHiv = makeObs(MohEvaluableNameConstants.HIV_DNA_PCR, MohEvaluableNameConstants.POSITIVE, MohTestUtils.makeDate("25 Jan 2010"));

        ObsRepresentation or = makeObsRepresentation(obsHiv);
        arvPatientSnapshot.consume(or);
        arvPatientSnapshot.consume(obsHiv);
        Assert.assertEquals(true, arvPatientSnapshot.get("HIVDNAPCRPositive"));
    }

	/**
	 * @verifies determine eligibility based on age group and flags
	 * @see org.openmrs.module.amrsreports.snapshot.PatientSnapshot#eligible()
	 */
	@Test
	public void eligible_shouldDetermineEligibilityBasedOnAgeGroupAndFlags() throws Exception {

		ARVPatientSnapshot arvPatientSnapshot = new ARVPatientSnapshot();
		arvPatientSnapshot.setAgeGroup(MohEvaluableNameConstants.AgeGroup.EIGHTEEN_MONTHS_TO_FIVE_YEARS);
		arvPatientSnapshot.set("pedsWHOStage", 4);
		Assert.assertTrue(arvPatientSnapshot.eligible());

		String expectedReason = (String) arvPatientSnapshot.get("reason");
		Assert.assertTrue("They are not equal", expectedReason.equals("Clinical Only"));

		arvPatientSnapshot.set("reason", "Clinical Only");
		Assert.assertEquals("That is pedsWHOStage", 4, arvPatientSnapshot.get("pedsWHOStage"));
	}

    private Obs makeObs(String conceptName, String conceptAnswer, Date date) {
        Obs o = new Obs();
        o.setConcept(MohCacheUtils.getConcept(conceptName));
        o.setValueCoded(MohCacheUtils.getConcept(conceptAnswer));
        o.setObsDatetime(date);
        return o;
    }

    /*make ObsRepresentation from Obs for consumption by PatientSnapshot*/
    private ObsRepresentation makeObsRepresentation(Obs obs){

        ObsRepresentation obsRepresentation = new ObsRepresentation();
        obsRepresentation.setConceptId(obs.getConcept().getConceptId());
        obsRepresentation.setValueCodedId(obs.getValueCoded().getConceptId());
        obsRepresentation.setObsDatetime(obs.getObsDatetime());
        return obsRepresentation;

    }
}
