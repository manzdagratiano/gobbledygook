/**
 * @module      workhorsefunctions
 * @overview    A suite of functions to implement the logic of interacting
 *              with the UI/DOM as well as the mechanism of interpreting
 *              and saving user preferences. This is the main "workhorse"
 *              responsible for implementing the algorithm.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Nov 19, 2014
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2014, 2015
 */

"use strict";

// ========================================================================
// GLOBAL CONSTANTS

/**
 * @namespace
 * @summary A global namespace for miscellaneous "environment" variables.
 */
var AUX_ENV                         = {

    /**
     * @summary The default number of iterations of PBKDF2 (when unset).
     */
    defaultIterations               : 10000,

    /**
     * @summary The actionable events that could be triggered when
     *          interacting with the UI.
     * @enum    {string}
     */
    events                          : {
        CHANGE                      : "change",
        CLICK                       : "click",
        DONE                        : "done",
        ERROR                       : "error",
        GENERATE                    : "generate"
    },

    /**
     * @summary The indentation for "pretty-printing" JSON objects.
     */
    indentation                     : 4,

    /**
     * @summary The text field input type;
     *          when "text", the text field shows its contents,
     *          when "password", the text field contents are not visible.
     * @enum    {string}
     */
    inputType                       : {
        PASSWORD                    : "password",
        TEXT                        : "text"
    },

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from; for instance,
     *          if a log is coming from within the main thread or
     *          a worker thread.
     */
    logCategory                     : "WORKHORSE: ",

    /**
     * @summary The UTF-8 code used to identify "success" or "failure"
     *          when communicating back to the user (in this case when
     *          saving site attributes).
     *          SUCCESS would show up as a "check mark", and
     *          FAILURE would show up as a "cross".
     * @enum    {string}
     */
    successString                   : {
        SUCCESS                     : "\u2713",
        FAILURE                     : "\u2717"
    },

    /**
     * @summary Types referenced for the "typeof" command.
     * @enum    {string}
     */
    types                           : {
        FUNCTION                    : "function",
        STRING                      : "string",
        UNDEFINED                   : "undefined"
    }

};  // end namepace AUX_ENV

// ========================================================================
// CLASS DEFINITIONS

// ------------------------------------------------------------------------
// The Attributes class

/**
 * @class
 * @summary A class to describe the user-level "attributes" of a website.
 * @param   {string} domain - The domain (default null).
 * @param   {int} iterations - The number of PBKDF2 iterations
 *          (default null).
 * @param   {int} truncation - The number of characters to truncate to
 *          (default Attributes.NO_TRUNCATION).
 */
var Attributes = function(domain, iterations, truncation) {
    this.domain     = (AUX_ENV.types.UNDEFINED === typeof(domain) ?
                       null : domain);
    this.iterations = (AUX_ENV.types.UNDEFINED === typeof(iterations) ?
                       null : iterations);
    this.truncation = (AUX_ENV.types.UNDEFINED === typeof(truncation) ?
                       Attributes.NO_TRUNCATION : truncation);
};

/**
 * @summary A static value which indicates that no truncation
 *          will be performed on the result (since the number of
 *          characters to truncate to cannot be negative).
 */
Attributes.NO_TRUNCATION = -1;

/**
 * @summary A function to check if an object of the Attributes class
 * has been "set", or has the default values.
 * @return  {boolean} "true" or "false"
 */
Attributes.prototype.attributesExist = function() {
    return (null !== this.domain &&
            null !== this.iterations &&
            Attributes.NO_TRUNCATION !== this.truncation);
};

// ------------------------------------------------------------------------
// The AttributesCodec class

/**
 * @namespace AttributesCodec
 * @summary A class to code/decode Attributes objects/lists to/from
 *          the format they are stored in the browser preferences system.
 */
var AttributesCodec = {

    delimiter : "|",

    /**
     * @summary A function to parse the string representing
     *          the list of site attributes stored in the browser
     *          preferences system.
     * @param   {string} encodedAttributesList - The encoded string
     *          containing a stringified version
     *          of the JSON object representing
     *          the (domain, encoded Attributes) pair.
     * @return  {object} A JSON of key-value pairs with site domain as key
     *          and site attributes (in compact string form) as value.
     *          Values are of the form <salt|iterations|truncation>,
     *          where at most two of those may be empty.
     */
    getDecodedAttributesList : function(encodedAttributesList) {
        var siteAttributesList = {};

        // JSON.parse will fail if the input string is empty;
        // catch any exceptions and leave it to the user to proceed or not
        // (Empty attributes will be returned in this case).
        try {
            siteAttributesList = JSON.parse(encodedAttributesList);
        } catch (e) {
            console.info(AUX_ENV.logCategory +
                         "WARN Failed to parse siteAttributesList" +
                         ", errorMsg=\"" + e + "\"");
            return {};
        }

        console.debug(AUX_ENV.logCategory + "siteAttributesList=" +
                      JSON.stringify(siteAttributesList,
                                     null,
                                     AUX_ENV.indentation));
        return siteAttributesList;
    },

    /**
     * @summary A function to parse the attributes string for a domain
     *          into an object of the Attributes class.
     * @param   {string} encodedAttributes - The encoded attribute string,
     *          of the form <salt|iterations|truncation>,
     *          where any number of the three may be empty.
     *          This format is chosen to eliminate storing redundant
     *          information for a domain when a cetain attribute is
     *          identical to the one produced by the default algorithm.
     * @return  {Attributes} The decoded object.
     *          If decoding fails,
     *          a default initialized object is returned.
     */
    getDecodedAttributes : function(encodedAttributes) {
        // Create a "default-initialized" object of type "Attributes"
        var attributes = new Attributes();

        if (AUX_ENV.types.UNDEFINED !== typeof(encodedAttributes)) {
            var siteAttributesArray =
                encodedAttributes.split(AttributesCodec.delimiter);
            // Sanity check
            if (3 !== siteAttributesArray.length) {
                console.error(AUX_ENV.logCategory +
                              "ERROR: Malformed Attributes! " +
                              "(Expected <salt|iterations|truncation>)");
                return attributes;
            }

            // Check if the salt is non-empty
            if (siteAttributesArray[0] !== "") {
                attributes.domain = siteAttributesArray[0];
            }

            // Check if the number of iterations is non-empty
            if (siteAttributesArray[1] !== "") {
                attributes.iterations = parseInt(siteAttributesArray[1]);
            }

            // Check if the truncation parameter is non-empty
            if (siteAttributesArray[2] !== "") {
                attributes.truncation = parseInt(siteAttributesArray[2]);
            }
        }

        return attributes;
    },

    /**
     * @summary A function to obtain the saved "Attributes" object
     *          corresonding to a site domain.
     * @param   {string} domain - The site domain to obtain
     *          the saved attributes for.
     * @param   {object} savedAttributesList - The JSON object containing
     *          (domain, encoded Attributes) pairs.
     * @return  {Attributes} The saved attributes. If no saved attributes
     *          exist, a default initialized object is returned.
     */
    getSavedAttributes : function(domain, savedAttributesList) {
        var savedAttributes =
            (savedAttributesList.hasOwnProperty(domain) ?
             savedAttributesList[domain] : undefined);
        return this.getDecodedAttributes(savedAttributes);
    },

    /**
     * @summary A function to encode an "Attributes" object to the form
     *          <domain|iterations|truncation> for storage in the browser
     *          preferences system. Attributes that are reproducible by the
     *          default algorithm will be skipped for efficient storage
     *          (since sync space is precious). However, if a previously
     *          overridden attribute is reset to the default value, it
     *          will still be saved, since this module can only compare
     *          differences and does not know about defaults.
     * @param   {Attributes} savedAttributes - The existing
     *          saved attributes for a domain (default if none existed).
     * @param   {Attributes} proposedAttributes - The proposed attributes
     *          following the application of the algorithm
     *          on the saved/default attributes.
     * @param   {Attributes} attributes - The actual attributes
     *          that were captured from the UI
     *          when the user hit "Generate",
     *          which will encompass any custom changes the user made.
     * @return  {string} The encoded attributes to save, if asked so.
     */
    getEncodedAttributes : function(savedAttributes,
                                    proposedAttributes,
                                    attributes) {
        var encodedAttributes = "";

        var domainToSave = "";
        var iterationsToSave = "";
        var truncationToSave = "";
        if (savedAttributes.attributesExist()) {
            domainToSave =
                (proposedAttributes.domain !== attributes.domain) ?
                attributes.domain :
                (null !== savedAttributes.domain ?
                 savedAttributes.domain : "");
            iterationsToSave =
                (proposedAttributes.iterations !== attributes.iterations) ?
                attributes.iterations :
                (null !== savedAttributes.iterations ?
                 ("" + savedAttributes.iterations) : "");
            truncationToSave =
                (proposedAttributes.truncation !== attributes.truncation) ?
                attributes.truncation :
                (Attributes.NO_TRUNCATION !== savedAttributes.truncation ?
                 ("" + savedAttributes.truncation) : "");
        } else {
            domainToSave =
                (proposedAttributes.domain !== attributes.domain) ?
                attributes.domain : "";
            iterationsToSave =
                (proposedAttributes.iterations !== attributes.iterations) ?
                ("" + attributes.iterations) : "";
            truncationToSave =
                (proposedAttributes.truncation !== attributes.truncation) ?
                ("" + attributes.truncation) : "";
        }

        // A saved attributes entry is created IFF
        // at least one of the three elements is not empty.
        if (!(("" === domainToSave) &&
              ("" === iterationsToSave) &&
              ("" === truncationToSave))) {
            encodedAttributes =
                domainToSave + AttributesCodec.delimiter +
                iterationsToSave + AttributesCodec.delimiter +
                truncationToSave;
        }

        return encodedAttributes;
    },

    /**
     * @summary A function to encode the JSON (domain, encodedAttributes)
     *          object to the string used for storage in the browser
     *          preference system. The format is just the
     *          JSON stringification of the object.
     * @param   {string} domain - The site domain.
     * @param   {object} siteAttributesList - A JSON
     *          (domain, encodedAttributes)
     *          representing existing saved attributes
     *          in the browser's preference system.
     * @return  {string} The encoded list of attributes.
     */
    getEncodedAttributesList : function(domain,
                                        siteAttributesList,
                                        savedAttributes,
                                        proposedAttributes,
                                        attributes) {
        var encodedAttributes =
            this.getEncodedAttributes(savedAttributes,
                                      proposedAttributes,
                                      attributes);
        if ("" === encodedAttributes) {
            console.info(AUX_ENV.logCategory + "No custom changes to save");
            return "";
        } else {
            console.info(AUX_ENV.logCategory +
                         "customAttributes=" + encodedAttributes);
        }

        // Create new, or update existing
        siteAttributesList[domain] = encodedAttributes;
        console.debug(AUX_ENV.logCategory + "Saving siteAttributesList=" +
                      JSON.stringify(siteAttributesList,
                                     null,
                                     AUX_ENV.indentation));

        var encodedAttributesList = JSON.stringify(siteAttributesList);
        console.debug(AUX_ENV.logCategory +
                      "encodedAttributesList=" + encodedAttributesList);
        return encodedAttributesList;
    }

};

// ------------------------------------------------------------------------
// The DOM class

/**
 * @namespace
 * @summary A namespace for the elements of the DOM of the UI.
 */
var DOM                             = {

    elements                        : {
        domain                      : "domain",
        password                    : "password",
        hash                        : "hash",
        showHash                    : "showHash",
        iterations                  : "iterations",
        truncate                    : "truncate",
        truncation                  : "truncation",
        saveAttributes              : "saveAttributes",
        attributesSaveSuccessLabel  : "attributesSaveSuccessLabel",
        generateButton              : "generateButton",
    },

    /**
     * @namespace
     * @summary A namespace for event listeners for DOM elements.
     */
    listeners                       : {

        /**
         * @summary Event listener for the "show" event for
         *          the proxy password, which will toggle the visibility
         *          of the generated password by flipping the type
         *          of the "hash" field to
         *          "text" (show) or to "password" (don't show).
         * @return  {undefined}
         */
        showListener : function() {
            var checkBox = document.getElementById(DOM.elements.showHash);
            var hashField = document.getElementById(DOM.elements.hash);
            if (checkBox.checked) {
                hashField.type = AUX_ENV.inputType.TEXT;
            } else {
                hashField.type = AUX_ENV.inputType.PASSWORD;
            }
        },


        /**
         * @summary Event listener for the truncate checkbox.
         *          When checked, the number of characters to truncate to
         *          can be changed;
         *          when unchecked, the number of characters to truncate
         *          is reset to Attributes.NO_TRUNCATION and disabled.
         * @return  {undefined}
         */
        truncateListener : function() {
            var checkBox = document.getElementById(DOM.elements.truncate);
            var truncationBox =
                document.getElementById(DOM.elements.truncation);
            if (checkBox.checked) {
                truncationBox.disabled = false;
            } else {
                truncationBox.value = Attributes.NO_TRUNCATION;
                truncationBox.disabled = true;
            }
        },


        /**
         * @summary Event listener for the "Generate" button.
         * @return  {undefined}
         */
        generateListener : function() {
            var button =
                document.getElementById(DOM.elements.generateButton);

            // Obtain the "domain", the "iterations" and the "truncation"
            // from the input boxes
            // even if they were restored from saved attributes.
            // This in case the user made any custom changes.
            var attributes =
                new Attributes(document.getElementById(
                                    DOM.elements.domain).value,
                               parseInt(document.getElementById(
                                    DOM.elements.iterations).value),
                               parseInt(document.getElementById(
                                    DOM.elements.truncation).value));
            // Note that the script never sees
            // the input password itself at all -
            // it is hashed through SHA256
            // at the time of extraction itself.
            // This ensures the freedom to subsequently log
            // all kinds of data to the console
            // without compromising security.
            Workhorse.generate({
                domain              : button.domain,
                saltKey             : button.saltKey,
                seedSHA             : sjcl.codec.hex.fromBits(
                                        sjcl.hash.sha256.hash(
                                            document.getElementById(
                                            DOM.elements.password).value)),
                attributes          : attributes,
                proposedAttributes  : button.proposedAttributes,
                savedAttributes     : button.savedAttributes,
                saveAttributes      : document.getElementById(
                                      DOM.elements.saveAttributes).checked,
                savedAttributesList : button.savedAttributesList
            });
        }

    },  // end namespace "listeners"

    /**
     * @namespace
     * @summary A namespace for functions to "toggle" certain UI elements.
     */
    togglers                        : {

        /**
         * @summary A function to toggle the type and text of the
         *          proxy password field
         *          based on the kind of event encountered.
         * @param   {string} eventType - An enum indicating
         *          the type of event -
         *          "click" (show password),
         *          "done" (normal, don't show password), or
         *          "error" (show error message),
         *          "generate" (disable the field)
         * @param   {string} message - The value of the text to set.
         * @return  {undefined}
         */
        toggleHashField : function(eventType, message) {
            var hashField = document.getElementById(DOM.elements.hash);
            if (eventType === AUX_ENV.events.CLICK) {
                hashField.disabled = false;
                hashField.type = AUX_ENV.inputType.TEXT;
            } else if (eventType === AUX_ENV.events.DONE) {
                hashField.disabled = false;
                hashField.type = AUX_ENV.inputType.PASSWORD;
            } else if (eventType === AUX_ENV.events.ERROR) {
                hashField.disabled = false;
                hashField.type = AUX_ENV.inputType.TEXT;
            } else if (eventType === AUX_ENV.events.GENERATE) {
                hashField.disabled = true;
            }

            if (AUX_ENV.types.STRING === typeof(message)) {
                hashField.value = message;
            }
        },

        /**
         * @summary A function to toggle the "attributesSaveSuccessLabel"
         *          of the DOM based on whether the operation of
         *          saving site attributes, when asked, was a success.
         *          A "true" input shows a check mark,
         *          while a "false" input shows a cross.
         * @return  {undefined}
         */
        toggleAttributesSaveSuccess : function(success) {
            document.getElementById(
                DOM.elements.attributesSaveSuccessLabel).innerHTML =
                (success ?  AUX_ENV.successString.SUCCESS :
                            AUX_ENV.successString.FAILURE);
        }

    },  // end namespace "togglers"

    /**
     * @summary Function to configure all the elements of the DOM for
     *          the url in question. This is the "entry" point to the
     *          algorithm, which sets the stage for generating
     *          the "pseudo" password, as well as for returning
     *          the payload to the main routine.
     * @param   {object} options - General options to the algorithm,
     *          independent of the url.
     * @param   {object} params - Parameters specific to the url.
     *          It is expected to have the following properties:
     *          @prop   {string} params.url -  The site url,
     *          @prop   {int} params.defaultIterations - The default number
     *                  of PBKDF2 iterations,
     *          @prop   {string} params.saltKey - The saved
     *                  key for generating the salt from the domain name,
     *          @prop   {string} params.encodedAttributesList - The saved
     *                  attributes list for all sites.
     * @return  {undefined}
     */
    configure : function(options, params) {
        console.info(AUX_ENV.logCategory +
                     "workhorseOptions=" +
                     JSON.stringify(options, null, AUX_ENV.indentation) +
                     ",\nworkhorseParams=" +
                     JSON.stringify(params, null, AUX_ENV.indentation));

        // ----------------------------------------------------------------
        // Private Methods

        /**
         * @brief   A function to obtain the domain name of the url
         *          (more correctly, the domain-subdomain-subsub...).
         *          This will be used as the "key" to identify this
         *          website, e.g.,
         *          https://foo.bar.baz.lol/firstLevel/secondLevelPage.html
         *          would be reduced to foo.bar.baz.lol for translation.
         *          This ensures a unique input for that website even if
         *          the website is reorganized internally.
         *          Of course, if multiple pages for the same
         *          domain-subdomain allow different logins,
         *          one will be using the same password per above,
         *          which is probably not all that bad.
         *          The author is not really aware of such a case and
         *          no attempt shall be made to address them differently.
         * @param   {string} url - The website url.
         * @return  {string} The extracted domain name.
         */
        var extractDomain = function(url) {
            // Chop off the beginning "http://", "https://",
            // or "ftp://" or whatever, if it exists.
            // This so that different modes of accessing the same domain
            // do not necessitate different passwords.
            var domain = (url.indexOf("://") !== -1 ?
                          url.split("://")[1] : url);

            // Obtain the "root" of the url - the domain-subdomain
            domain = domain.split("/")[0];
            console.info(AUX_ENV.logCategory + "domain=" + domain);

            return domain;
        };

        /**
         * @brief   A function to configure
         *          the "domain" element of the DOM.
         * @param   {string} domain - The website domain.
         * @param   {string} savedDomain - The saved domain,
         *          (if overridden, and if exists).
         * @return  {string} The extracted domain name.
         */
        var configureDomain = function(domain,
                                       savedDomain) {
            var domainBox = document.getElementById(DOM.elements.domain);
            domainBox.value = (savedDomain ? savedDomain : domain);
            return domainBox.value;
        }

        /**
         * @summary A function to configure the "iterations" element
         *          of the DOM.
         * @param   {int} defaultIterations - The default number of
         *          PBKDF2 iterations.
         * @param   {int} savedIterations - An overridden number
         *          of PBKDF2 iterations stored for this domain (if exist).
         * @return  {int} The number of PBKDF2 iterations.
         */
        var configureIterations = function(defaultIterations,
                                           savedIterations) {
            var iterationBox =
                document.getElementById(DOM.elements.iterations);
            var iterations = savedIterations;
            if (!iterations) {
                iterations = defaultIterations;
            }
            if (!iterations) {
                iterations = AUX_ENV.defaultIterations;
            }
            console.info(AUX_ENV.logCategory + "iterations=" + iterations);
            iterationBox.value = iterations;
            return iterations;
        };

        /**
         * @summary A function to configure the "hash" element of the DOM,
         *          where the proxy password will be output.
         *          The field is disabled, until the algorithm is run.
         * @return  {undefined}
         */
        var configureHash = function() {
            // The proxy password output field
            var hashField = document.getElementById(DOM.elements.hash);
            hashField.value = "";
            hashField.disabled = true;

            // The "Show" checkbox for the proxy password
            var checkBox = document.getElementById(DOM.elements.showHash);

            // Add an event listener to the "click" action,
            // which toggles the visibility of the generated hash.
            checkBox.addEventListener(AUX_ENV.events.CHANGE,
                                      DOM.listeners.showListener);
        };

        /**
         * @summary A function to configure the "truncation" element
         *          of the DOM.
         * @param   {int} truncation - The number of truncation characters.
         *          If this number is equal to Attributes.NO_TRUNCATION,
         *          the input box will be disabled.
         * @return  {int} The number of characters to truncate 
         *          the proxy password to.
         */
        var configureTruncation = function(truncation) {
            var truncationBox =
                document.getElementById(DOM.elements.truncation);
            truncationBox.disabled =
                (Attributes.NO_TRUNCATION === truncation) ? true : false;
            truncationBox.value = truncation;

            // Configure the "truncate" checkbox of the DOM
            var checkBox = document.getElementById(DOM.elements.truncate);
            checkBox.checked =
                (Attributes.NO_TRUNCATION === truncation) ? false : true;

            // Add an event listener to the "click" action,
            // which toggles the editability of the truncation parameter.
            checkBox.addEventListener(AUX_ENV.events.CHANGE,
                                      DOM.listeners.truncateListener);

            return truncation;
        };

        /**
         * @summary A function to configure the "saveAttributes" checkbox
         *          of the DOM (unchecked by default), and the associated
         *          "attributesSaveSuccessLabel".
         *          By default, the label is empty.
         * @return  {undefined}
         */
        var configureSaveAttributes = function() {
            document.getElementById(
                DOM.elements.saveAttributes).checked = false;
            document.getElementById(
                DOM.elements.attributesSaveSuccessLabel).innerHTML = "";
        };

        /**
         * @summary A function to configure the "Generate" button
         *          of the DOM.
         * @param   {string} domain - The site domain.
         * @param   {string} saltKey - The key used for generating
         *          the salt from the domain name.
         * @param   {Attributes} savedAttributes - The saved attributes
         *          of the domain (default if none).
         * @param   {Attributes} proposedAttributes - The proposed
         *          attributes that would be applicable
         *          (after `337-translation etc),
         *          if the user did not make any custom changes.
         * @param   {string} savedAttributesList - The saved
         *          encoded attributes list for all domains.
         * @return  {undefined}
         */
        var configureGenerateButton = function(domain,
                                               saltKey,
                                               savedAttributes,
                                               proposedAttributes,
                                               savedAttributesList) {
            var button =
                document.getElementById(DOM.elements.generateButton);

            // Attach the other input parameters to the "button",
            // so that they may be accessed in the attached event listener
            // (cf. below).
            button.domain = domain;
            button.saltKey = saltKey;
            button.savedAttributesList = savedAttributesList;
            button.savedAttributes = savedAttributes;
            button.proposedAttributes = proposedAttributes;

            // Add an event listener to the "click" action,
            // whose purpose is to gather the values of the elements
            // when "Generate" is hit (the input to the algorithm).
            button.addEventListener(AUX_ENV.events.CLICK,
                                    DOM.listeners.generateListener);
        };

        // ----------------------------------------------------------------
        // Check if the saltKey was generated, i.e., it is not empty.
        if (!params.saltKey) {
            DOM.togglers.toggleHashField(AUX_ENV.events.ERROR,
                                         "ERROR: " +
                                         "saltKey === EMPTY");
            return;
        }

        // ----------------------------------------------------------------
        // Configure all the elements.

        console.info(AUX_ENV.logCategory + "Configuring elements...");

        // Check and extract saved site attributes, if any.
        var domain = extractDomain(params.url);

        var savedAttributesList =
            AttributesCodec.getDecodedAttributesList(
                                    params.encodedAttributesList);
        var savedAttributes =
            AttributesCodec.getSavedAttributes(domain,
                                               savedAttributesList);
        console.info(AUX_ENV.logCategory + "savedAttributes=" +
                     JSON.stringify(savedAttributes,
                                    null,
                                    AUX_ENV.indentation));

        var proposedAttributes =
            new Attributes(configureDomain(domain,
                                           savedAttributes.domain),
                           configureIterations(params.defaultIterations,
                                               savedAttributes.iterations),
                           configureTruncation(
                                         savedAttributes.truncation));

        configureHash();
        configureSaveAttributes();
        configureGenerateButton(domain,
                                params.saltKey,
                                savedAttributes,
                                proposedAttributes,
                                savedAttributesList);
    },

    /**
     * @summary Function to "deconfigure" the DOM so that it is ready for
     *          a new set of inputs.
     * @return  {undefined}
     */
    deconfigure : function() {
        console.info(AUX_ENV.logCategory + "Cleaning up resources...");
        // Remove all the event listeners
        var generateButton =
            document.getElementById(DOM.elements.generateButton);
        generateButton.removeEventListener(AUX_ENV.events.CLICK,
                                           DOM.listeners.generateListener);
        var truncateBox =
            document.getElementById(DOM.elements.truncate);
        truncateBox.removeEventListener(AUX_ENV.events.CHANGE,
                                        DOM.listeners.truncateListener);
        var showBox = document.getElementById(DOM.elements.showHash);
        showBox.removeEventListener(AUX_ENV.events.CHANGE,
                                    DOM.listeners.showListener);
    }

};  // end namespace "DOM"

// ------------------------------------------------------------------------
// The Workhorse class

/**
 * @summary A namespace for "workhorse" function, which implement the
 *          algorithm.
 */
var Workhorse           = {

    /**
     * @summary Function to generate the proxy password from all the input
     *          parameters.
     * @param   {object} params - The input parameters, which are expected
     *          to have the following properties:
     *          @prop {string} params.domain - The site domain,
     *          @prop {string} params.seedSHA - The SHA256 encoded
     *                  actual password,
     *          @prop {Attributes} params.savedAttributes The saved
     *                  attributes for the domain (default if none exist),
     *          @prop {Attributes} params.proposedAttributes - The proposed
     *                  attributes that would be applicable
     *                  if the user did not make any custom changes,
     *          @prop {Attributes} params.attributes - The current
     *                  attributes for the domain captured from the DOM
     *                  when "Generate" was hit.
     *          @prop {boolean} params.saveAttributes - A flag to determine
     *                  if the user asked to save the current attributes
     *                  for this domain,
     *          @prop {string} params.savedAttributesList - The encoded
     *                  list of
     *                  (domain, encoded Attributes) for all domains,
     *                  which will be mutated with the current
     *                  attributes if "saveAttributes" is true.
     * @return  {undefined}
     */
    generate : function(params) {
        // Clear the password field immediately
        document.getElementById(DOM.elements.password).value = "";
        // Clear the attributesSaveSuccessLabel (in case set)
        document.getElementById(
                DOM.elements.attributesSaveSuccessLabel).innerHTML = "";
        // Toggle the "show" checkbox
        document.getElementById(DOM.elements.showHash).checked = false;
        DOM.togglers.toggleHashField(AUX_ENV.events.GENERATE, "");

        console.debug(AUX_ENV.logCategory + "generateParams=" +
                      JSON.stringify(params, null, AUX_ENV.indentation));

        // Check if Web Workers are supported
        if (AUX_ENV.types.UNDEFINED === typeof(Worker)) {
            // No support for web workers;
            // do nothing,
            // since doing intensive operations here will
            // block the UI thread.
            // Modern browsers support the API, so no reason to be burdened
            // by chains of the past.
            console.error(AUX_ENV.logCategory +
                          "ERROR: No web worker support!");
            DOM.togglers.toggleHashField(AUX_ENV.events.ERROR,
                                         "ERROR: " +
                                         "browser.Type === ANCIENT");
            return;
        }

        console.info(AUX_ENV.logCategory + "Firing web worker HASHER...");
        var hasher = new Worker("hasher.js");

        hasher.onmessage = function(oEvent) {
            var logCategory = "HASHER: ";
            console.debug(logCategory +
                          JSON.stringify(oEvent.data,
                                         null,
                                         AUX_ENV.indentation));
            var eventData = oEvent.data;

            // Finalize the worker, if this is the "done" signal
            if (eventData.hasOwnProperty(AUX_ENV.events.DONE)) {
                console.info(AUX_ENV.logCategory + "Finalizing worker...");
                DOM.togglers.toggleHashField(AUX_ENV.events.DONE,
                                             eventData.password);

                var encodedAttributesList = "";
                if (params.saveAttributes) {
                    console.info(AUX_ENV.logCategory +
                                 "Generating attributes list string...");
                    encodedAttributesList =
                        AttributesCodec.getEncodedAttributesList(
                                            params.domain,
                                            params.savedAttributesList,
                                            params.savedAttributes,
                                            params.proposedAttributes,
                                            params.attributes);
                }

                // Check if a function "finalize" is defined,
                // and if so, call it.
                if (AUX_ENV.types.FUNCTION === typeof(finalize)) {
                    finalize({
                        domain                  : params.domain,
                        password                : eventData.password,
                        attributesListString    : encodedAttributesList
                    });
                }

                // Post a message to the worker to close itself.
                // DO NOT CALL "hasher.terminate()",
                // which rudely terminates the web worker
                // without a chance to clean up.
                hasher.postMessage({ done : AUX_ENV.events.DONE });
            };
        };

        hasher.onerror = function(oEvent) {
            throw new Error(oEvent.message +
                            " (" + oEvent.filename +
                            ":" + oEvent.lineno + ")");
        };

        DOM.togglers.toggleHashField(AUX_ENV.events.CLICK,
                                     "<Generating...>");
        var hasherParams = {
            saltKey         : params.saltKey,
            seedSHA         : params.seedSHA,
            attributes      : params.attributes,
        };
        hasher.postMessage(hasherParams);
    }

};  // end namespace Workhorse
