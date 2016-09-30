/**
 * @file        AttributesTest.java
 * @brief       Source file for the AttributesTest class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Sep 27, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

package io.tengentoppa.yggdrasil;

// Standard Java
import java.io.UnsupportedEncodingException;
import java.security.Security;

// JUnit Classes
import org.junit.Test;
import org.junit.Assert;

/**
 * @brief   A test class for the methods in the Attributes class.
 */
public class AttributesTest {

    // ====================================================================
    // TESTS

    /**
     * @brief   Tests if attributes don't exist for
     *          a default Attributes object.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testAttributesDon__tExist_assert() {
        Attributes attributes = new Attributes();
        Assert.assertFalse(attributes.attributesExist());
    }

    /**
     * @brief   Tests if attributes exist for an Attributes object
     *          when domain is set.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testAttributesExistDomain_assert() {
        Attributes attributes = new Attributes();
        attributes.setDomain("google.com");
        Assert.assertTrue(attributes.attributesExist());
    }

    /**
     * @brief   Tests if attributes exist for an Attributes object
     *          when iterations are set.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testAttributesExistIterations_assert() {
        Attributes attributes = new Attributes();
        attributes.setIterations(10000);
        Assert.assertTrue(attributes.attributesExist());
    }

    /**
     * @brief   Tests if attributes exist for an Attributes object
     *          when truncation is set.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testAttributesExistTruncation_assert() {
        Attributes attributes = new Attributes();
        attributes.setTruncation(20);
        Assert.assertTrue(attributes.attributesExist());
    }

    /**
     * @brief   Tests if attributes exist for an Attributes object
     *          when asked for no special characters.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testAttributesExistNoSpecialChars_assert() {
        Attributes attributes = new Attributes();
        attributes.setSpecialCharsFlag(0);
        Assert.assertTrue(attributes.attributesExist());
    }

    /**
     * @brief   Tests if overrides to save don't exist when they shouldn't.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testOverridesToSaveDon__tExist_assert() {
        Attributes savedOverrides = new Attributes();
        Attributes proposedAttributes =
            new Attributes("google.com", 10000, -1, 1);
        Attributes attributes =
            new Attributes("google.com", 10000, -1, 1);
        Assert.assertFalse(
                AttributesCodec.getOverridesToSave(attributes,
                                                   savedOverrides,
                                                   proposedAttributes)
                .attributesExist());
    }

    /**
     * @brief   Tests if overrides to save don't exist when they shouldn't.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testOverridesToSaveDon__tExistReprise_assert() {
        Attributes savedOverrides =
            new Attributes(null, 10003, -1, 0);
        Attributes proposedAttributes =
            new Attributes("google.com", 10003, -1, 0);
        Attributes attributes =
            new Attributes("google.com", 10003, -1, 0);
        Assert.assertFalse(
                AttributesCodec.getOverridesToSave(attributes,
                                                   savedOverrides,
                                                   proposedAttributes)
                .attributesExist());
    }

    /**
     * @brief   Tests if overrides to save exist when they should.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testOverridesToSaveExist_assert() {
        Attributes savedOverrides = new Attributes();
        Attributes proposedAttributes =
            new Attributes("google.com", 10000, -1, 1);
        Attributes attributes =
            new Attributes("google.com", 10003, -1, 0);
        Attributes overridesToSave =
            new Attributes(null, 10003, -1, 0);
        Assert.assertEquals(
                AttributesCodec.getOverridesToSave(attributes,
                                                   savedOverrides,
                                                   proposedAttributes),
                overridesToSave);
    }

    /**
     * @brief   Tests if overrides to save exist when they should.
     * @return  Does not even.
     */
    @Test
    public void attributesTest_testOverridesToSaveExistReprise_assert() {
        Attributes savedOverrides =
            new Attributes(null, 10003, -1, 0);
        Attributes proposedAttributes =
            new Attributes("google.com", 10003, -1, 0);
        Attributes attributes =
            new Attributes("google.com", 10005, -1, 0);
        Attributes overridesToSave =
            new Attributes(null, 10005, -1, 0);
        Assert.assertEquals(
                AttributesCodec.getOverridesToSave(attributes,
                                                   savedOverrides,
                                                   proposedAttributes),
                overridesToSave);
    }

}
