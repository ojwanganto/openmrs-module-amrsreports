/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.amrsreport.rule.collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicContext;
import org.openmrs.logic.result.Result;
import org.openmrs.module.amrsreport.rule.MohEvaluableRule;

import java.util.*;

public class MohPregnancyPMTCReferralRule extends MohEvaluableRule {

	public static final String TOKEN = "MOH Pregnancy PMTC Referral";
	 
    private static final Log log = LogFactory.getLog(MohPregnancyPMTCReferralRule.class);

    public static final String ESTIMATED_DATE_OF_CONFINEMENT = "ESTIMATED DATE OF CONFINEMENT";

    public static final String ESTIMATED_DATE_OF_CONFINEMENT_ULTRASOUND = "ESTIMATED DATE OF CONFINEMENT, ULTRASOUND";

    public static final String CURRENT_PREGNANT = "CURRENT PREGNANT";

    public static final String NO_OF_WEEK_OF_PREGNANCY = "NO OF WEEK OF PREGNANCY";

    public static final String FUNDAL_LENGTH = "FUNDAL LENGTH";

    public static final String PREGNANCY_URINE_TEST = "PREGNANCY URINE TEST";

    public static final String URGENT_MEDICAL_ISSUES = "URGENT MEDICAL ISSUES";

    public static final String PROBLEM_ADDED = "PROBLEM ADDED";

    public static final String FOETAL_MOVEMENT = "FOETAL MOVEMENT";

    public static final String REASON_FOR_CURRENT_VISIT = "REASON FOR CURRENT VISIT";

    public static final String REASON_FOR_NEXT_VISIT = "REASON FOR NEXT VISIT";

    public static final String YES = "YES";

    public static final String MONTH_OF_CURRENT_GESTATION = "MONTH OF CURRENT GESTATION";

    public static final String POSITIVE = "POSITIVE";

    public static final String PREGNANCY = "PREGNANCY";

    public static final String PREGNANCY_ECTOPIC = "PREGNANCY, ECTOPIC";

    public static final String ANTENATAL_CARE = "ANTENATAL CARE";

    private Map<String, Concept> cachedConcepts = null;

    private List<Concept> cachedQuestions = null;



    /**
     * @param context
     * @param patientId
     * @param parameters
     * @return
     * @see org.openmrs.logic.Rule#eval(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Override
    protected Result evaluate(final LogicContext context, final Integer patientId, final Map<String, Object> parameters) {

            MohPregnancyPMTCReferralRuleSnapshot mohPregnancyPMTCReferralRuleSnapshot=new MohPregnancyPMTCReferralRuleSnapshot();
            Result result = new Result();

            ConceptService conceptService = Context.getConceptService();
            ObsService obsService = Context.getObsService();

            Patient patient = Context.getPatientService().getPatient(patientId);
            List<Obs> observations = obsService.getObservations(Arrays.<Person>asList(patient), null, getQuestionConcepts(),
                            null, null, null, null, null, null, null, null, false);

            for (Obs observation : observations) {
                    Date dueDate = null;

                    if (observation.getConcept().equals(getCachedConcept(ESTIMATED_DATE_OF_CONFINEMENT)))
                            dueDate = observation.getValueDatetime();
                    else if (observation.getConcept().equals(getCachedConcept(ESTIMATED_DATE_OF_CONFINEMENT_ULTRASOUND)))
                            dueDate = observation.getValueDatetime();

                    if (mohPregnancyPMTCReferralRuleSnapshot.consume(observation))
                            result = new Result(new Date(), Result.Datatype.DATETIME, Boolean.TRUE, null, dueDate, null, "PMTCT", null);
                    else
                            result = new Result(new Date(), Result.Datatype.DATETIME, Boolean.FALSE, null, null, null, StringUtils.EMPTY, null);
            }

            return result;
    }

    /**
     * maintains a cache of concepts and stores them by name
     *
     * @param name the name of the cached concept to retrieve
     * @return the concept matching the name
     */
    private  Concept getCachedConcept(String name) {
        if (cachedConcepts == null) {
            cachedConcepts = new HashMap<String, Concept>();
        }
        if (!cachedConcepts.containsKey(name)) {
            cachedConcepts.put(name, Context.getConceptService().getConcept(name));
        }
        return cachedConcepts.get(name);
    }


    private List<Concept> getQuestionConcepts() {
            if (cachedQuestions == null) {
                    cachedQuestions = new ArrayList<Concept>();
                    cachedQuestions.add(getCachedConcept(ESTIMATED_DATE_OF_CONFINEMENT));
                    cachedQuestions.add(getCachedConcept(ESTIMATED_DATE_OF_CONFINEMENT_ULTRASOUND));
                    cachedQuestions.add(getCachedConcept(CURRENT_PREGNANT));
                    cachedQuestions.add(getCachedConcept(NO_OF_WEEK_OF_PREGNANCY));
                    cachedQuestions.add(getCachedConcept(MONTH_OF_CURRENT_GESTATION));
                    cachedQuestions.add(getCachedConcept(FUNDAL_LENGTH));
                    cachedQuestions.add(getCachedConcept(PREGNANCY_URINE_TEST));
                    cachedQuestions.add(getCachedConcept(URGENT_MEDICAL_ISSUES));
                    cachedQuestions.add(getCachedConcept(PROBLEM_ADDED));
                    cachedQuestions.add(getCachedConcept(FOETAL_MOVEMENT));
                    cachedQuestions.add(getCachedConcept(REASON_FOR_CURRENT_VISIT));
                    cachedQuestions.add(getCachedConcept(REASON_FOR_NEXT_VISIT));
            }
            return cachedQuestions;
    }

    /**
     * Get the token name of the rule that can be used to reference the rule from LogicService
     *
     * @return the token name
     */
    @Override
    protected String getEvaluableToken() {
            return TOKEN;
    }}
