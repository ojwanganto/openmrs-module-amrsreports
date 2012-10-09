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
import org.openmrs.module.amrsreport.cohort.definition.MohCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

import java.util.*;

@Handler(supports = {MohCohortDefinition.class})
public class MOH361BCohort implements CohortDefinitionEvaluator {

	private static final Log log = LogFactory.getLog(MOH361BCohort.class);
	public static final String ENCOUNTER_TYPE_ADULT_RETURN = "ADULTRETURN";
	public static final String ENCOUNTER_TYPE_ADULT_INITIAL = "ADULTINITIAL";
	public static final String FIRST_HIV_RAPID_TEST_QUALITATIVE_CONCEPT = "HIV RAPID TEST, QUALITATIVE";
	public static final String SECOND_HIV_RAPID_TEST_QUALITATIVE_CONCEPT = "HIV RAPID TEST 2, QUALITATIVE";
	public static final String POSITIVE_CONCEPT = "POSITIVE";
	public static final String HIV_ENZYME_IMMUNOASSAY_QUALITATIVE_CONCEPT = "HIV ENZYME IMMUNOASSAY, QUALITATIVE";
    public static final String ARV_PLAN ="ANTIRETROVIRAL PLAN";
    public static final String START_DRUGS="START DRUGS";
    public static final String CONT_REGIMEN="CONTINUE REGIMEN";
    public static final String CHANGE_FORMULATION="CHANGE FORMULATION";
    public static final String CHANGE_REGIMEN="CHANGE REGIMEN";
    public static final String REFILLED="REFILLED";
    public static final String NOT_REFILLED="NOT REFILLED";
    public static final String DRUG_SUBSTITUTION="DRUG SUBSTITUTION";
    public static final String DRUG_RESTART="DRUG RESTART";
    public static final String DOSING_CHANGE="DOSING CHANGE";



	public EvaluatedCohort evaluate(final CohortDefinition cohortDefinition, final EvaluationContext evaluationContext) throws EvaluationException {

        MohCohortDefinition mohCohortDefinition = (MohCohortDefinition) cohortDefinition;
		EncounterService service = Context.getEncounterService();
		ConceptService conceptService = Context.getConceptService();
		CohortDefinitionService definitionService = Context.getService(CohortDefinitionService.class);

		// limit to people with adult initial or return encounters
		EncounterCohortDefinition encounterCohortDefinition = new EncounterCohortDefinition();

		encounterCohortDefinition.addEncounterType(service.getEncounterType(ENCOUNTER_TYPE_ADULT_INITIAL));
		encounterCohortDefinition.addEncounterType(service.getEncounterType(ENCOUNTER_TYPE_ADULT_RETURN));

		// find people who have adult encounters
		Cohort encounterCohort = definitionService.evaluate(encounterCohortDefinition, evaluationContext);

		// TODO set these with GPs and a settings page
		Concept firstRapidConcept = conceptService.getConcept(FIRST_HIV_RAPID_TEST_QUALITATIVE_CONCEPT);
		Concept secondRapidConcept = conceptService.getConcept(SECOND_HIV_RAPID_TEST_QUALITATIVE_CONCEPT);
		Concept positiveConcept = conceptService.getConcept(POSITIVE_CONCEPT);

		// define search for all people who have rapid test positive results
		CodedObsCohortDefinition firstRapidCohortDefinition = new CodedObsCohortDefinition();
		firstRapidCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		firstRapidCohortDefinition.setQuestion(firstRapidConcept);
		firstRapidCohortDefinition.setOperator(SetComparator.IN);
		firstRapidCohortDefinition.setValueList(Arrays.asList(positiveConcept));

		// define search all people who have rapid test 2 positive results
		CodedObsCohortDefinition secondRapidCohortDefinition = new CodedObsCohortDefinition();
		secondRapidCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		secondRapidCohortDefinition.setQuestion(secondRapidConcept);
		secondRapidCohortDefinition.setOperator(SetComparator.IN);
		secondRapidCohortDefinition.setValueList(Arrays.asList(positiveConcept));

		// combine rapid test definitions
		CompositionCohortDefinition rapidCompositionCohortDefinition = new CompositionCohortDefinition();
		rapidCompositionCohortDefinition.addSearch("PositiveFirstRapid", firstRapidCohortDefinition, null);
		rapidCompositionCohortDefinition.addSearch("PositiveSecondRapid", secondRapidCohortDefinition, null);
		rapidCompositionCohortDefinition.setCompositionString("PositiveFirstRapid OR PositiveSecondRapid");
		Cohort rapidCompositionCohort = definitionService.evaluate(rapidCompositionCohortDefinition, evaluationContext);

		// set age limits
		AgeCohortDefinition ageCohortDefinition = new AgeCohortDefinition();
		ageCohortDefinition.setMinAge(18);
		ageCohortDefinition.setMinAgeUnit(DurationUnit.MONTHS);
		ageCohortDefinition.setMaxAge(14);
		ageCohortDefinition.setMaxAgeUnit(DurationUnit.YEARS);

		// TODO set this concept with a GP and settings page
		Concept elisaConcept = conceptService.getConcept(HIV_ENZYME_IMMUNOASSAY_QUALITATIVE_CONCEPT);

		// define search for all people with a positive elisa evaluation
		CodedObsCohortDefinition elisaCohortDefinition = new CodedObsCohortDefinition();
		elisaCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
		elisaCohortDefinition.setQuestion(elisaConcept);
		elisaCohortDefinition.setOperator(SetComparator.IN);
		elisaCohortDefinition.setValueList(Arrays.asList(positiveConcept));

		// find patients within age limits who had elisa positive results
		CompositionCohortDefinition elisaCompositionCohortDefinition = new CompositionCohortDefinition();
		elisaCompositionCohortDefinition.addSearch("PaediatricAge", ageCohortDefinition, null);
		elisaCompositionCohortDefinition.addSearch("PositiveElisa", elisaCohortDefinition, null);
		elisaCompositionCohortDefinition.setCompositionString("PaediatricAge AND PositiveElisa");
		Cohort elisaCompositionCohort = definitionService.evaluate(elisaCompositionCohortDefinition, evaluationContext);

		// Check for the elisa to make sure the elisa happened after 18 months
		PersonAttributeCohortDefinition personAttributeCohortDefinition = new PersonAttributeCohortDefinition();
		personAttributeCohortDefinition.setAttributeType(Context.getPersonService().getPersonAttributeTypeByName("Health Center"));
		personAttributeCohortDefinition.setValueLocations(mohCohortDefinition.getLocationList());


        List<Concept> onARVresult = new ArrayList<Concept>();
        onARVresult.add(conceptService.getConcept(START_DRUGS));
        onARVresult.add(conceptService.getConcept(CONT_REGIMEN));
        onARVresult.add(conceptService.getConcept(CHANGE_FORMULATION));
        onARVresult.add(conceptService.getConcept(CHANGE_REGIMEN));
        onARVresult.add(conceptService.getConcept(REFILLED));
        onARVresult.add(conceptService.getConcept(DRUG_SUBSTITUTION));
        onARVresult.add(conceptService.getConcept(DRUG_RESTART));
        onARVresult.add(conceptService.getConcept(DOSING_CHANGE));
        onARVresult.add(conceptService.getConcept(NOT_REFILLED));

        Concept onArvConcept = conceptService.getConcept(ARV_PLAN);
        CodedObsCohortDefinition patientsOnARVCohortDef = new CodedObsCohortDefinition();
        firstRapidCohortDefinition.setTimeModifier(PatientSetService.TimeModifier.ANY);
        firstRapidCohortDefinition.setQuestion(onArvConcept);
        firstRapidCohortDefinition.setOperator(SetComparator.IN);
        firstRapidCohortDefinition.setValueList(onARVresult);

		// build the patientIds by combining all found patients
		Set<Integer> patientIds = new HashSet<Integer>();
		patientIds.addAll(encounterCohort.getMemberIds());
		patientIds.addAll(rapidCompositionCohort.getMemberIds());
		patientIds.addAll(elisaCompositionCohort.getMemberIds());


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