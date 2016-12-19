/**
 * @file        WebTest.java
 * @brief       Source file for the WebTest class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Dec 17, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

package io.tengentoppa.yggdrasil;

// JUnit Classes
import org.junit.Test;
import org.junit.Assert;

/**
 * @brief   A test class for the methods in the Web class.
 */
public class WebTest {

    // ====================================================================
    // TESTS

    /**
     * @brief   Tests if an "https://"-qualified query is returned unchanged.
     * @return  Does not even.
     */
    @Test
    public void webTest_testHttps_assert() {
        String query = "https://www.google.com";
        Assert.assertTrue(query.equals(WebHelper.getUrl(query)));
    }

    /**
     * @brief   Tests if an "http://"-qualified query is altered
     *          to be qualified by "https://".
     * @return  Does not even.
     */
    @Test
    public void webTest_testHttp_assert() {
        String query = "http://www.google.com";
        Assert.assertTrue(WebHelper.getUrl(query).equals(
                                        "https://www.google.com"));
    }

    /**
     * @brief   Tests if "www."-qualified query is altered
     *          to be qualified by "https://" as well.
     * @return  Does not even.
     */
    @Test
    public void webTest_testWww_assert() {
        String query = "www.google.com";
        Assert.assertTrue(WebHelper.getUrl(query).equals(
                                        "https://www.google.com"));
    }

    /**
     * @brief   Tests if a query ending in a domain name and not prefixed
     *          by either "https://" or "www." gets qualified.
     * @return  Does not even.
     */
    @Test
    public void webTest_testDomain_assert() {
        String query = null;
        query = "google.com";
        Assert.assertTrue(WebHelper.getUrl(query).equals(
                                        "https://www.google.com"));
        query = "gnu.org";
        Assert.assertTrue(WebHelper.getUrl(query).equals(
                                        "https://www.gnu.org"));
        query = "rutgers.edu";
        Assert.assertTrue(WebHelper.getUrl(query).equals(
                                        "https://www.rutgers.edu"));
        query = "senate.gov";
        Assert.assertTrue(WebHelper.getUrl(query).equals(
                                        "https://www.senate.gov"));
    }

    /**
     * @brief   Tests if free-form query is converted into a Google search.
     * @return  Does not even.
     */
    @Test
    public void webTest_testGoogle_assert() {
        String query = "whiskey tango foxtrot";
        Assert.assertTrue(WebHelper.getUrl(query).equals(
                    "https://www.google.com/search?q=whiskey+tango+foxtrot"));
    }

}
