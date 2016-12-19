/**
 * @file        Attributes.java
 * @summary     Source file for the Attributes class.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Sep 27, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

package io.tengentoppa.yggdrasil;

// Android
import android.util.Log;

// Standard Java
import java.util.Objects;

/**
 * @summary The Attributes class.
 */
public class Attributes {

    // ====================================================================
    // PUBLIC METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    public static final int DEFAULT_ITERATIONS = 10000;
    public static final int NO_TRUNCATION      = -1;

    // --------------------------------------------------------------------
    // CONSTRUCTORS

    /**
     * @summary The no-argument constructor.
     * @return  Does not even.
     */
    public Attributes() {
        m_domain = null;
        m_iterations = null;
        m_truncation = Attributes.NO_TRUNCATION;
        m_specialCharsFlag = 1;
    }

    /**
     * @summary The variable argument constructor.
     *          The members m_domain and m_iterations are
     *          allowed to be null. However, the other two aren't. 
     * @return  Does not even.
     */
    public Attributes(String domain, Integer... opts) {
        m_domain     = domain;
        m_iterations = ((opts.length > 0) ? opts[0] : null);
        m_truncation =
            ((opts.length > 1) ?
             ((null != opts[1]) ? opts[1] : Attributes.NO_TRUNCATION ) :
             Attributes.NO_TRUNCATION);
        m_specialCharsFlag =
            ((opts.length > 2) ?
             ((null != opts[2]) ? opts[2] : 1) :
             1);
    }

    // --------------------------------------------------------------------
    // ACCESSORS

    /**
     * @summary Domain accessor
     * @return  {String}
     */
    public String domain() {
        return m_domain;
    }

    /**
     * @summary Iterations accessor
     * @return  {Integer}
     */
    public Integer iterations() {
        return m_iterations;
    }

    /**
     * @summary Truncation accessor
     * @return  {Integer}
     */
    public Integer truncation() {
        return m_truncation;
    }

    /**
     * @summary Special Characters Flag accessor
     * @return  {Integer}
     */
    public Integer specialCharsFlag() {
        return m_specialCharsFlag;
    }

    // --------------------------------------------------------------------
    // MUTATORS

    /**
     * @summary Domain modifier
     * @return  {null}
     */
    public void setDomain(String domain) {
        this.m_domain = domain;
    }

    /**
     * @summary Iterations modifier
     * @return  {null}
     */
    public void setIterations(Integer iterations) {
        this.m_iterations = iterations;
    }

    /**
     * @summary Truncation modifier
     * @return  {null}
     */
    public void setTruncation(Integer truncation) {
        // Sanity check
        if (null == truncation) {
            Log.e(LOG_CATEGORY, "ERROR: " +
                  "Thwarted attempt to set truncation to null!");
            return;
        }
        this.m_truncation = truncation;
    }

    /**
     * @summary Special Characters Flag modifier
     * @return  {null}
     */
    public void setSpecialCharsFlag(Integer specialCharsFlag) {
        // Sanity check
        if (null == specialCharsFlag) {
            Log.e(LOG_CATEGORY, "ERROR: " +
                  "Thwarted attempt to set truncation to null!");
            return;
        }
        this.m_specialCharsFlag = specialCharsFlag;
    }

    // --------------------------------------------------------------------
    // EQUALITY

    /**
     * @summary Method to provide an equality comparison for the Attributes
     *          class.
     * @return  {boolean} True or False.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Attributes)) {
            return false;
        }
        if (object == this) {
            return true;
        }

        Attributes rhs = (Attributes)object;
        return ((null == rhs.domain() ?
                 null == this.domain() :
                 rhs.domain().equals(this.domain())) &&
                (null == rhs.iterations() ?
                 null == this.iterations() :
                 rhs.iterations().equals(this.iterations())) &&
                rhs.truncation().equals(this.truncation()) &&
                rhs.specialCharsFlag().equals(this.specialCharsFlag()));
    }

    /**
     * @summary Method to provide a custom hashCode() operator in line with
     *          the custom "equals" operator.
     * @return  {int} The computed hashCode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.domain(),
                            this.iterations(),
                            this.truncation(),
                            this.specialCharsFlag());
    }

    // --------------------------------------------------------------------
    // UTILITIES

    /**
     * @summary A method to check if this Attributes object is not the
     *          default initialized object
     * @return  Returns true or false
     */
    public boolean attributesExist() {
        return ((null != m_domain) ||
                (null != m_iterations) ||
                (!m_truncation.equals(NO_TRUNCATION)) ||
                (!m_specialCharsFlag.equals(1)));
    }

    // ====================================================================
    // PRIVATE METHODS

    // --------------------------------------------------------------------
    // CONSTANTS

    private static final String LOG_CATEGORY    = "YGGDRASIL.ATTRIBUTES";

    // --------------------------------------------------------------------
    // DATA MEMBERS

    private String  m_domain;           /** @brief The website
                                          * domain-subdomain.
                                          */
    private Integer m_iterations;       /** @brief The number of PBKDF2
                                          * iterations.
                                          */
    private Integer m_truncation;       /** @brief The truncation size
                                          * for the generated password.
                                          */
    private Integer m_specialCharsFlag; /** @brief An indicator for
                                          * whether special characters
                                          * are allowed. Boolean, but
                                          * represented as {0, 1} for
                                          * encoding.
                                          */
}
