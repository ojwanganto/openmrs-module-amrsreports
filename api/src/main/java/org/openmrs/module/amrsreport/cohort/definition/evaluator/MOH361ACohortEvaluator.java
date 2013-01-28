/**
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.amrsreport.cohort.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.amrsreport.cohort.definition.MOH361ACohortDefinition;
import org.openmrs.module.amrsreport.rule.MohEvaluableNameConstants;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Handler(supports = {MOH361ACohortDefinition.class})
public class MOH361ACohortEvaluator implements CohortDefinitionEvaluator {

	private static final Log log = LogFactory.getLog(MOH361ACohortEvaluator.class);


	public EvaluatedCohort evaluate(final CohortDefinition cohortDefinition, final EvaluationContext evaluationContext) throws EvaluationException {

		MOH361ACohortDefinition mohCohortDefinition = (MOH361ACohortDefinition) cohortDefinition;

		EncounterService service = Context.getEncounterService();
		ConceptService conceptService = Context.getConceptService();
		CohortDefinitionService definitionService = Context.getService(CohortDefinitionService.class);


        // Define concepts for children
        Concept firstRapidConcept = conceptService.getConcept(MohEvaluableNameConstants.HIV_RAPID_TEST_QUALITATIVE);
        Concept elisaConcept = conceptService.getConcept(MohEvaluableNameConstants.HIV_ENZYME_IMMUNOASSAY_QUALITATIVE);
        Concept hivDnaPcrConcept = conceptService.getConcept(MohEvaluableNameConstants.HIV_DNA_PCR);
        Concept positiveConcept = conceptService.getConcept(MohEvaluableNameConstants.POSITIVE);


        // define concepts for location filter (transfers within and without Ampath centre)
        Concept transferConcept = conceptService.getConcept(MohEvaluableNameConstants.TRANSFER_CARE_TO_OTHER_CENTER);
        Concept withinConcept = conceptService.getConcept(MohEvaluableNameConstants.WITHIN_AMPATH_CLINICS);
        Concept nonAmpathConcept = conceptService.getConcept(MohEvaluableNameConstants.NON_AMPATH);
        Concept missedVisitConcept = conceptService.getConcept(MohEvaluableNameConstants.REASON_FOR_MISSED_VISIT);
        Concept transferVisitConcept = conceptService.getConcept(MohEvaluableNameConstants.AMPATH_CLINIC_TRANSFER);
        Concept transferCareToOtherCentreDetailed = conceptService.getConcept(MohEvaluableNameConstants.TRANSFER_CARE_TO_OTHER_CENTER_DETAILED);
        Concept transferCareToOtherCentreDetailedPositive = conceptService.getConcept(MohEvaluableNameConstants.FREETEXT_GENERAL);
        Concept reasonExitedCare = conceptService.getConcept(MohEvaluableNameConstants.REASON_EXITED_CARE);
        Concept reasonExitedCareValue = conceptService.getConcept(MohEvaluableNameConstants.PATIENT_TRANSFERRED_OUT);

        Concept tboutcome = conceptService.getConcept(MohEvaluableNameConstants.OUTCOME_AT_END_OF_TUBERCULOSIS_TREATMENT);
        Concept tboutcomeValue = conceptService.getConcept(MohEvaluableNameConstants.PATIENT_TRANSFERRED_OUT);

		// Define cohort using encounter types
		EncounterCohortDefinition encounterCohortDefinition = new EncounterCohortDefinition();
		encounterCohortDefinition.addEncounterType(service.getEncounterType(MohEvaluableNameConstants.ENCOUNTER_TYPE_ADULT_INITIAL));
		encounterCohortDefinition.addEncounterType(service.getEncounterType(MohEvaluableNameConstants.ENCOUNTER_TYPE_ADULT_RETURN));
        encounterCohortDefinition.addEncounterType(service.getEncounterType(MohEvaluableNameConstants.ENCOUNTER_TYPE_BASELINEINVESTIGATION));
		encounterCohortDefinition.setLocationList(mohCohortDefinition.getLocationList());

		Cohort encounterCohort = definitionService.evaluate(encounterCohortDefinition, evaluationContext);

		// define search for all people who have rapid test positive results
		CodedObsCohortDefinition firstRapidCohortDefinition = new CodedObsCohortDefinition();
		firstRapidCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		firstRapidCohortDefinition.setLocationList(mohCohortDefinition.getLocationList());
		firstRapidCohortDefinition.setQuestion(firstRapidConcept);
		firstRapidCohortDefinition.setOperator(SetComparator.IN);
		firstRapidCohortDefinition.setValueList(Arrays.asList(positiveConcept));


        // define search for kids with a positive elisa evaluation
        CodedObsCohortDefinition elisaCohortDefinition = new CodedObsCohortDefinition();
        elisaCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
        elisaCohortDefinition.setLocationList(mohCohortDefinition.getLocationList());
        elisaCohortDefinition.setQuestion(elisaConcept);
        elisaCohortDefinition.setOperator(SetComparator.IN);
        elisaCohortDefinition.setValueList(Arrays.asList(positiveConcept));

        // set age limits
        AgeCohortDefinition ageCohortDefinition = new AgeCohortDefinition();
        ageCohortDefinition.setMinAge(18);
        ageCohortDefinition.setMinAgeUnit(DurationUnit.MONTHS);

        /*Find all paeds who had elisa test positive and were at least 18 months old at the time*/
        CompositionCohortDefinition elisaCompositionCohortDefinition = new CompositionCohortDefinition();
        elisaCompositionCohortDefinition.addSearch("elisaAgeLimit", ageCohortDefinition, null);
        elisaCompositionCohortDefinition.addSearch("PositiveElisa", elisaCohortDefinition, null);
        elisaCompositionCohortDefinition.setCompositionString("elisaAgeLimit AND PositiveElisa");


        //find kids with HIV DNA PCR test positive
        CodedObsCohortDefinition hivDnaPcrTestCohort = new CodedObsCohortDefinition();
        hivDnaPcrTestCohort.setTimeModifier(PatientSetService.TimeModifier.ANY);
        hivDnaPcrTestCohort.setLocationList(mohCohortDefinition.getLocationList());
        hivDnaPcrTestCohort.setQuestion(hivDnaPcrConcept);
        hivDnaPcrTestCohort.setOperator(SetComparator.IN);
        hivDnaPcrTestCohort.setValueList(Arrays.asList(positiveConcept));

        //combine tests for paeds
        CompositionCohortDefinition finalPeadsCohortDef = new CompositionCohortDefinition();
        finalPeadsCohortDef.addSearch("rapidCohortDef",firstRapidCohortDefinition, null);
        finalPeadsCohortDef.addSearch("elisaCohortDef",elisaCohortDefinition, null);
        finalPeadsCohortDef.addSearch("hivDnaPcrDef",hivDnaPcrTestCohort, null);
        finalPeadsCohortDef.setCompositionString("rapidCohortDef OR elisaCohortDef OR hivDnaPcrDef");

        Cohort paedsCompositionCohort = definitionService.evaluate(finalPeadsCohortDef, evaluationContext);


		//find patients by location(health centre)
		PersonAttributeCohortDefinition personAttributeCohortDefinition = new PersonAttributeCohortDefinition();
		personAttributeCohortDefinition.setAttributeType(Context.getPersonService().getPersonAttributeTypeByName("Health Center"));
		personAttributeCohortDefinition.setValueLocations(mohCohortDefinition.getLocationList());


        /*
        * tests for transfers
        * */


		CodedObsCohortDefinition transferCohortDefinition = new CodedObsCohortDefinition();
		transferCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		transferCohortDefinition.setLocationList(mohCohortDefinition.getLocationList());
		transferCohortDefinition.setQuestion(transferConcept);
		transferCohortDefinition.setOperator(SetComparator.IN);
		transferCohortDefinition.setValueList(Arrays.asList(withinConcept,nonAmpathConcept));

        // define missed visits at this location
        CodedObsCohortDefinition missedVisitCohortDefinition = new CodedObsCohortDefinition();
        missedVisitCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
        missedVisitCohortDefinition.setLocationList(mohCohortDefinition.getLocationList());
        missedVisitCohortDefinition.setQuestion(missedVisitConcept);
        missedVisitCohortDefinition.setOperator(SetComparator.IN);
        missedVisitCohortDefinition.setValueList(Arrays.asList(transferVisitConcept));

        // Define cohort for transfer care to other centre detailed
        CodedObsCohortDefinition detailedTransfer = new CodedObsCohortDefinition();
        detailedTransfer.setTimeModifier(PatientSetService.TimeModifier.ANY);
        detailedTransfer.setLocationList(mohCohortDefinition.getLocationList());
        detailedTransfer.setQuestion(transferCareToOtherCentreDetailed);
        detailedTransfer.setOperator(SetComparator.IN);
        detailedTransfer.setValueList(Arrays.asList(transferCareToOtherCentreDetailedPositive));

        /*
        * Combine results for transfers within,without,detailed and missed visits
        * */


		CompositionCohortDefinition transferCompositionCohortDefinition = new CompositionCohortDefinition();
		transferCompositionCohortDefinition.addSearch("transferwithinNwithoutCoh", transferCohortDefinition, null);
		transferCompositionCohortDefinition.addSearch("missedVisitsCoh", missedVisitCohortDefinition, null);
        transferCompositionCohortDefinition.addSearch("detailedTransferCoh",detailedTransfer, null);
		transferCompositionCohortDefinition.setCompositionString("transferwithinNwithoutCoh OR  missedVisitsCoh OR detailedTransferCoh");

		Cohort transferCompositionCohort = definitionService.evaluate(transferCompositionCohortDefinition, evaluationContext);

        /*
        Cohort Definitions for transfer out due to exit from care
        */
        // define cohort for people who have exit care
        CodedObsCohortDefinition careExitCohortDefinition = new CodedObsCohortDefinition();
        careExitCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
        careExitCohortDefinition.setLocationList(mohCohortDefinition.getLocationList());
        careExitCohortDefinition.setQuestion(reasonExitedCare);
        careExitCohortDefinition.setOperator(SetComparator.IN);
        careExitCohortDefinition.setValueList(Arrays.asList(reasonExitedCareValue));


        // define cohort for people who transferred out at the end of TB treatment
        CodedObsCohortDefinition tBTreatmentCohortDefinition = new CodedObsCohortDefinition();
        tBTreatmentCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
        tBTreatmentCohortDefinition.setLocationList(mohCohortDefinition.getLocationList());
        tBTreatmentCohortDefinition.setQuestion(missedVisitConcept);
        tBTreatmentCohortDefinition.setOperator(SetComparator.IN);
        tBTreatmentCohortDefinition.setValueList(Arrays.asList(transferVisitConcept));

        //combine results for transfers outside ampath centres due to exit from program
        CompositionCohortDefinition transferOutCompositionCohortDefinition = new CompositionCohortDefinition();
        transferOutCompositionCohortDefinition.addSearch("exitCare",careExitCohortDefinition, null);
        transferOutCompositionCohortDefinition.addSearch("endTBTreatment",tBTreatmentCohortDefinition, null);
        transferOutCompositionCohortDefinition.setCompositionString("exitCare OR endTBTreatment");

		// find all people with defined health center and a missed visit
		CompositionCohortDefinition missedVisitCompositionCohortDefinition = new CompositionCohortDefinition();
		missedVisitCompositionCohortDefinition.addSearch("HealthCenterAttribute", personAttributeCohortDefinition, null);
		missedVisitCompositionCohortDefinition.addSearch("MissedVisitTransfer", missedVisitCohortDefinition, null);
		missedVisitCompositionCohortDefinition.setCompositionString("HealthCenterAttribute AND MissedVisitTransfer");
		Cohort missedVisitCompositionCohort = definitionService.evaluate(missedVisitCompositionCohortDefinition, evaluationContext);


        //-------------------- just check up to this point. I havent finished combining the cohorts ---------
		// build the patientIds by combining all found patients
		Set<Integer> patientIds = new HashSet<Integer>();
		patientIds.addAll(encounterCohort.getMemberIds());
		patientIds.addAll(rapidCompositionCohort.getMemberIds());
		patientIds.addAll(elisaCompositionCohort.getMemberIds());
		patientIds.addAll(transferCompositionCohort.getMemberIds());
		patientIds.addAll(missedVisitCompositionCohort.getMemberIds());

		// find fake patients
		PersonAttributeCohortDefinition fakePatientCohortDefinition = new PersonAttributeCohortDefinition();
		fakePatientCohortDefinition.setAttributeType(Context.getPersonService().getPersonAttributeType(28));
		fakePatientCohortDefinition.setValues(Collections.singletonList("true"));
		Cohort fakePatientCohort = definitionService.evaluate(fakePatientCohortDefinition, evaluationContext);

		// remove fake patients from the list                
		patientIds.removeAll(fakePatientCohort.getMemberIds());

		// build the cohort from the resulting list of patient ids
		return new EvaluatedCohort(new Cohort(patientIds), cohortDefinition, evaluationContext);
	}
}