package org.openmrs.module.amrsreport.rule.location;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.amrsreport.cache.MohCacheUtils;
import org.openmrs.module.amrsreport.rule.MohEvaluableNameConstants;
import org.openmrs.module.amrsreport.service.MohCoreService;
import org.openmrs.module.amrsreport.util.MohFetchRestriction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * test file for MohLocationRule class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class MohLocationRuleTest {

    private static final List<String> initConcepts = Arrays.asList(
            MohEvaluableNameConstants.TRANSFER_CARE_TO_OTHER_CENTER,
            MohEvaluableNameConstants.REASON_FOR_MISSED_VISIT,
            MohEvaluableNameConstants.TRANSFER_CARE_TO_OTHER_CENTER_DETAILED,
            MohEvaluableNameConstants.REASON_EXITED_CARE,
            MohEvaluableNameConstants.OUTCOME_AT_END_OF_TUBERCULOSIS_TREATMENT,

            MohEvaluableNameConstants.WITHIN_AMPATH_CLINICS,
            MohEvaluableNameConstants.AMPATH_CLINIC_TRANSFER,
            MohEvaluableNameConstants.FREETEXT_GENERAL,
            MohEvaluableNameConstants.NON_AMPATH,
            MohEvaluableNameConstants.PATIENT_TRANSFERRED_OUT
    );

    private static final int PATIENT_ID = 5;
    private EncounterService encounterService;
    private ConceptService conceptService;
    private MohCoreService mohCoreService;
    private List<Obs> currentObs;
    private List<Encounter> currentEncounters;
    private MohLocationRule rule;


    @Before
    public void setup() {

        // initialize the current obs and Encounters
        currentObs = new ArrayList<Obs>();
        currentEncounters = new ArrayList<Encounter>();

        // build the concept service
        int i = 0;
        conceptService = Mockito.mock(ConceptService.class);

        for (String conceptName : initConcepts) {

            Mockito.when(conceptService.getConcept(conceptName)).thenReturn(new Concept(i++));
        }
        Mockito.when(conceptService.getConcept((String) null)).thenReturn(null);

        //set up MohCoreService
        mohCoreService = Mockito.mock(MohCoreService.class);

        //return current Observations

        Mockito.when(mohCoreService.getPatientObservations(Mockito.eq(PATIENT_ID),
                Mockito.anyMap(), Mockito.any(MohFetchRestriction.class))).thenReturn(currentObs);

        //return current encounters

        Mockito.when(mohCoreService.getPatientEncounters(Mockito.eq(PATIENT_ID),
                Mockito.anyMap(), Mockito.any(MohFetchRestriction.class))).thenReturn(currentEncounters);

        // set up Context
        PowerMockito.mockStatic(Context.class);
        Mockito.when(Context.getConceptService()).thenReturn(conceptService);
        Mockito.when(Context.getService(MohCoreService.class)).thenReturn(mohCoreService);

        rule = new MohLocationRule();

    }

    /**
     * generate a date from a string
     *
     * @param date
     * @return
     */
    private Date makeDate(String date) {
        try {
            return new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).parse(date);
        } catch (Exception e) {
            // pass
        }
        return new Date();
    }

    /**
     * adds an observation with the given date as the obs datetime
     *
     * @param concept
     * @param date
     */
    private void addObs(String concept, String location,String answer, String date) {
        Obs obs = new Obs();
        obs.setConcept(conceptService.getConcept(concept));
        obs.setValueCoded(conceptService.getConcept(answer));
        obs.setLocation(addLocation(location));
        obs.setObsDatetime(makeDate(date));

        currentObs.add(obs);
    }

    private Location addLocation(String location){
        Location location1= new Location();
        location1.setName(location);
        return location1;
    }


    /**
     * @verifies test for TRANSFER_CARE_TO_OTHER_CENTER using Obs
     * @see MohLocationRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldTestForTRANSFER_CARE_TO_OTHER_CENTERUsingObs() throws Exception {
        addObs(MohEvaluableNameConstants.TRANSFER_CARE_TO_OTHER_CENTER,MohEvaluableNameConstants.NON_AMPATH,"Turbo","16 Oct 2012");

        Assert.assertEquals("TRANSFER CARE TO OTHER CENTER tested false ",new Result("Transferred Within Ampath to: NON-AMPATH").toString(),rule.evaluate(null,PATIENT_ID,null).toString());

    }

    /**
     * @verifies test for TRANSFER_CARE_TO_OTHER_CENTER within Ampath
     * @see MohLocationRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldTestForTRANSFER_CARE_TO_OTHER_CENTERWithinAmpath() throws Exception {
        addObs(MohEvaluableNameConstants.TRANSFER_CARE_TO_OTHER_CENTER,MohEvaluableNameConstants.WITHIN_AMPATH_CLINICS,"Turbo","16 Oct 2012");

        Assert.assertEquals("TRANSFER CARE TO OTHER CENTER tested false ",new Result("Transferred Within Ampath to: NON-AMPATH"),rule.evaluate(null,PATIENT_ID,null));
    }

    /**
     * @verifies test for REASON_FOR_MISSED_VISIT using Obs
     * @see MohLocationRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldTestForREASON_FOR_MISSED_VISITUsingObs() throws Exception {
        //TODO auto-generated

    }

    /**
     * @verifies test for TRANSFER_CARE_TO_OTHER_CENTER_DETAILED using Obs
     * @see MohLocationRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldTestForTRANSFER_CARE_TO_OTHER_CENTER_DETAILEDUsingObs() throws Exception {
        //TODO auto-generated

    }

    /**
     * @verifies test for REASON_EXITED_CARE using Obs
     * @see MohLocationRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldTestForREASON_EXITED_CAREUsingObs() throws Exception {
        //TODO auto-generated

    }

    /**
     * @verifies test for the OUTCOME_AT_END_OF_TUBERCULOSIS_TREATMENT using Obs
     * @see MohLocationRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldTestForTheOUTCOME_AT_END_OF_TUBERCULOSIS_TREATMENTUsingObs() throws Exception {
        //TODO auto-generated

    }


}
