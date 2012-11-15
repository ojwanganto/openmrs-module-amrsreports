package org.openmrs.module.amrsreport.rule.collection;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.amrsreport.rule.collection.MohLostToFollowUpRule;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.openmrs.logic.result.Result;

/**
 * Created with IntelliJ IDEA.
 * User: oliver
 * Date: 11/15/12
 * Time: 10:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class MohLostToFollowUpRuleTest extends BaseModuleContextSensitiveTest {
    /**
     * @verifies get date and reason why a patient was lost
     * @see MohLostToFollowUpRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldGetDateAndReasonWhyAPatientWasLost() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        Patient patient = Context.getPatientService().getPatient(2);
        patient.setDead(true);
        patient.setDeathDate(new Date());

        Assert.assertNotNull(patient) ;
        Assert.assertTrue("The patient is not dead",patient.isDead());

        MohLostToFollowUpRule lostToFollowUpRule = new MohLostToFollowUpRule();
        String result= lostToFollowUpRule.evaluate(null,patient.getId(), null).toString();
        String expectedRes ="DEAD | " + sdf.format(new Date());

        Assert.assertTrue("Date and reason is not the same", result.equals(expectedRes));



    }
}
