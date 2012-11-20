
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.api.PatientService;
import org.openmrs.api.PatientSetService;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.amrsreport.rule.collection.MohGenderRule;

import java.lang.String;

/**
 * Created with IntelliJ IDEA.
 * User: oliver
 * Date: 11/20/12
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class MohGenderRuleTest extends BaseModuleContextSensitiveTest {
    /**
     * @verifies get Gender of a patient
     * @see org.openmrs.module.amrsreport.rule.collection.MohGenderRule#evaluate(org.openmrs.logic.LogicContext, Integer, java.util.Map)
     */
    @Test
    public void evaluate_shouldGetGenderOfAPatient() throws Exception {

        Patient patient = Context.getPatientService().getPatient(8);
        String expectedResult = "F";

        Assert.assertNotNull("A patient with the id was not found",patient) ;
        MohGenderRule sampleGenderRule = new MohGenderRule();

        String ruleResult = sampleGenderRule.evaluate(null,patient.getId(),null).toString();

        //Assert.assertTrue("The two strings are not equal",expectedResult.equals(ruleResult));

    }
}
