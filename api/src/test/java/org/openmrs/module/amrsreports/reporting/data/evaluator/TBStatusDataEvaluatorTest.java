package org.openmrs.module.amrsreports.reporting.data.evaluator;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.amrsreports.AmrsReportsConstants;
import org.openmrs.module.amrsreports.MohTestUtils;
import org.openmrs.module.amrsreports.model.PatientTBTreatmentData;
import org.openmrs.module.amrsreports.model.SortedObsFromDate;
import org.openmrs.module.amrsreports.reporting.data.DateARTStartedDataDefinition;
import org.openmrs.module.amrsreports.reporting.data.TBStatusDataDefinition;
import org.openmrs.module.amrsreports.reporting.data.TbTreatmentStartDateDataDefinition;
import org.openmrs.module.amrsreports.rule.MohEvaluableNameConstants;
import org.openmrs.module.reporting.data.MappedData;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.context.PersonEvaluationContext;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test class for TBStatusDataEvaluator class
 */
public class TBStatusDataEvaluatorTest extends BaseModuleContextSensitiveTest {

    protected Patient patient;
    private Date evaluationDate;
    private PersonEvaluationContext evaluationContext;
    private TBStatusDataEvaluator evaluator;
    private TBStatusDataDefinition definition;

    @Before
    public void setUp() throws Exception {

        executeDataSet("datasets/concepts-tb-status.xml");

        patient = MohTestUtils.createTestPatient();

        Cohort c = new Cohort(Collections.singleton(patient.getId()));

        evaluationDate = new Date();
        evaluationContext = new PersonEvaluationContext(evaluationDate);
        evaluationContext.setBaseCohort(c);

        definition = new TBStatusDataDefinition();

        MappedData<DateARTStartedDataDefinition> artDateMap = new MappedData<DateARTStartedDataDefinition>();
        artDateMap.setParameterizable(new DateARTStartedDataDefinition());
        definition.setEffectiveDateDefinition(artDateMap);

        evaluator = new TBStatusDataEvaluator();

    }

    /**
     * @should return value_datetime for PATIENT_REPORTED_X_RAY_CHEST_with_NORMAL
     * @throws Exception
     */
    @Test
    public void shouldReturnValue_datetimeForPATIENT_REPORTED_X_RAY_CHEST_with_NORMAL() throws Exception {
        MohTestUtils.addCodedObs(patient, MohEvaluableNameConstants.PATIENT_REPORTED_X_RAY_CHEST,
                MohEvaluableNameConstants.NORMAL, "16 Oct 1990");

        SortedObsFromDate actual  = getActualResult();
        ArrayList<Obs> observations= new ArrayList<Obs>(actual.getData());

        assertThat(observations.size(), is(1));

        String valueCoded = observations.get(0).getValueCoded().getName().toString();
        String thisConcept = observations.get(0).getConcept().getName().toString();
        assertEquals(valueCoded,MohEvaluableNameConstants.NORMAL);
        assertEquals(thisConcept,MohEvaluableNameConstants.PATIENT_REPORTED_X_RAY_CHEST);




    }

 /**
 * @should return value_datetime for SPUTUM FOR AFB with NEGATIVE result
 * @throws Exception
 */
        @Test
        public void shouldReturnValue_datetimeForSPUTUM_FOR_AFB_with_NEGATIVE() throws Exception {
            MohTestUtils.addCodedObs(patient, MohEvaluableNameConstants.SPUTUM_FOR_AFB,
                    MohEvaluableNameConstants.NEGATIVE, "16 Oct 1990");

            SortedObsFromDate actual  = getActualResult();
            ArrayList<Obs> observations= new ArrayList<Obs>(actual.getData());

            assertThat(observations.size(), is(1));

            String valueCoded = observations.get(0).getValueCoded().getName().toString();
            String thisConcept = observations.get(0).getConcept().getName().toString();
            assertEquals(valueCoded,MohEvaluableNameConstants.NEGATIVE);
            assertEquals(thisConcept,MohEvaluableNameConstants.SPUTUM_FOR_AFB);

        }

    /**
     * @should return value_datetime for COUGH DURATION, CODED with WEEKS result
     * @throws Exception
     */
    @Test
    public void shouldReturnValue_datetimeForCOUGH_DURATION_CODED_with_WEEKS() throws Exception {
        MohTestUtils.addCodedObs(patient, MohEvaluableNameConstants.COUGH_DURATION_CODED,
                MohEvaluableNameConstants.WEEKS, "16 Oct 1990");

        SortedObsFromDate actual  = getActualResult();
        ArrayList<Obs> observations= new ArrayList<Obs>(actual.getData());

        assertThat(observations.size(), is(1));

        String valueCoded = observations.get(0).getValueCoded().getName().toString();
        String thisConcept = observations.get(0).getConcept().getName().toString();
        assertEquals(valueCoded,MohEvaluableNameConstants.WEEKS);
        assertEquals(thisConcept,MohEvaluableNameConstants.COUGH_DURATION_CODED);

    }



    private SortedObsFromDate getActualResult() throws EvaluationException {

        EvaluatedPersonData actual = evaluator.evaluate(definition, evaluationContext);
        Map<Integer, Object> data =  actual.getData();

        SortedObsFromDate patientData = (SortedObsFromDate) data.get(patient.getId());
        return patientData;
    }

}
