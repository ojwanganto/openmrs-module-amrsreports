package org.openmrs.module.amrsreport.rule.collection;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.amrsreport.rule.collection.MohGenderRule;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.lang.String;

/**
 * Created with IntelliJ IDEA.
 * User: oliver
 * Date: 11/21/12
 * Time: 12:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class MohGenderRuleTest extends BaseModuleContextSensitiveTest {
    /**
     * @verifies get Gender of a patient
     * @see MohGenderRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldGetGenderOfAPatient() throws Exception {

        Patient patient = Context.getPatientService().getPatient(8);
        Patient patient1 = Context.getPatientService().getPatient(2);

        Assert.assertTrue("patient returned null",patient!=null);
        Assert.assertTrue("patient1 returned null",patient1!=null);

        String expectedString="F";
        String expectedString1="M";

        MohGenderRule mohGenderRule = new MohGenderRule();
        String foundString= mohGenderRule.evaluate(null,patient.getId(),null).toString();
        String foundString1= mohGenderRule.evaluate(null,patient1.getId(),null).toString();

        Assert.assertTrue(foundString, expectedString.equals(foundString));
        Assert.assertTrue(foundString1, expectedString1.equals(foundString1));
    }
}
