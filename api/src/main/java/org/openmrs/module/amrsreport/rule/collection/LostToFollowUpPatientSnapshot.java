package org.openmrs.module.amrsreport.rule.collection;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.module.amrsreport.rule.MohEvaluableNameConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: oliver
 * Date: 11/15/12
 * Time: 12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class LostToFollowUpPatientSnapshot {

    public static final String CONCEPT_DATE_OF_DEATH = "DATE OF DEATH";
    public static final String CONCEPT_DEATH_REPORTED_BY = "DEATH REPORTED BY";
    public static final String CONCEPT_CAUSE_FOR_DEATH = "CAUSE FOR DEATH";
    public static final String CONCEPT_DECEASED = "DECEASED";
    public static final String CONCEPT_PATIENT_DIED = "PATIENT DIED";
    public static final String CONCEPT_TRANSFER_CARE_TO_OTHER_CENTER = "TRANSFER CARE TO OTHER CENTER";
    public static final String CONCEPT_AMPATH = "AMPATH";
    public static final String CONCEPT_RETURN_VISIT_DATE_EXP_CARE_NURSE = "RETURN VISIT DATE, EXPRESS CARE NURSE";


    private Map<String, Concept> cachedConcepts = null;
    public Result consume(Obs o){
        Concept ob = o.getConcept();
        Concept answer = o.getValueCoded();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        if(ob.equals(getCachedConcept(CONCEPT_DATE_OF_DEATH))){
            return new Result("DEAD | " + sdf.format(sdf.format(o.getObsDatetime())));
        }
        else if(ob.equals(getCachedConcept(CONCEPT_DEATH_REPORTED_BY))){
            return new Result("DEAD | " + sdf.format(sdf.format(o.getObsDatetime())));
        }else if(ob.equals(getCachedConcept(CONCEPT_CAUSE_FOR_DEATH))){
            return new Result("DEAD | " + sdf.format(sdf.format(o.getObsDatetime())));
        }else if(ob.equals(getCachedConcept(CONCEPT_DECEASED))){
            return new Result("DEAD | " + sdf.format(sdf.format(o.getObsDatetime())));
        }else if(ob.equals(getCachedConcept(CONCEPT_PATIENT_DIED))){
            return new Result("DEAD | " + sdf.format(o.getObsDatetime()));
        }


        if(ob.equals(getCachedConcept(CONCEPT_TRANSFER_CARE_TO_OTHER_CENTER))){
            if(answer == getCachedConcept(CONCEPT_AMPATH))
                return new Result("TO | (Ampath) " + sdf.format(o.getObsDatetime()));
            else
                return new Result("TO | (Non-Ampath) " + sdf.format(o.getObsDatetime()));
        }

        if(ob.equals(getCachedConcept(MohEvaluableNameConstants.RETURN_VISIT_DATE).getConceptId())){
            if(sdf.format(o.getObsDatetime()) != null){
                long requiredTimeToShowup = ((o.getValueDatetime().getTime()) - (o.getObsDatetime().getTime())) + (long)(1000 * 60 * 60 * 24 * 30.4375 * 3);
                long todayTimeFromSchedule = (new Date()).getTime() - (o.getObsDatetime().getTime());
                if( requiredTimeToShowup < todayTimeFromSchedule ){
                    return new Result("LTFU | " + sdf.format(o.getValueDatetime()));
                }
            }
        }

        if(ob.equals(getCachedConcept(CONCEPT_RETURN_VISIT_DATE_EXP_CARE_NURSE))){
            if(sdf.format(o.getObsDatetime()) != null){
                long requiredTimeToShowup = ((o.getValueDatetime().getTime()) - (o.getObsDatetime().getTime())) + (long)(1000 * 60 * 60 * 24 * 30.4375 * 3);
                long todayTimeFromSchedule = (new Date()).getTime() - (o.getObsDatetime().getTime());
                if( requiredTimeToShowup < todayTimeFromSchedule ){
                    return new Result("LTFU | " + sdf.format(o.getValueDatetime()));
                }
            }
        }

      return null;
        }



        /**
         * maintains a cache of concepts and stores them by name
         *
         * @param name the name of the cached concept to retrieve
         * @return the concept matching the name
         */
    public Concept getCachedConcept(String name) {
        if (cachedConcepts == null) {
            cachedConcepts = new HashMap<String, Concept>();
        }
        if (!cachedConcepts.containsKey(name)) {
            cachedConcepts.put(name, Context.getConceptService().getConcept(name));
        }
        return cachedConcepts.get(name);
    }


}
