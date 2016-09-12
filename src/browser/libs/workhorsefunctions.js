/**
 * @module      workhorsefunctions
 * @overview    A suite of functions to implement the logic of interacting
 *              with the UI/DOM as well as the mechanism of interpreting
 *              and saving user preferences. This is the main "workhorse"
 *              responsible for implementing the algorithm.
 *              This module is BROWSER-AGNOSTIC. Browser-specific APIs
 *              do not belong in here. All incoming data is abstracted
 *              out into a common interface, and all outgoing data is
 *              adapted back into the browser-specific layer.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Nov 19, 2014
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2014
 */

"use strict";

// ========================================================================
// GLOBAL CONSTANTS

/**
 * @namespace
 * @summary A global namespace for miscellaneous "environment" variables.
 */
var AUX                             = {

    /**
     * @summary A "category" to log with, to identify which component
     *          the log is coming from; for instance,
     *          if a log is coming from within the main thread or
     *          a worker thread.
     */
    logCategory                     : "WORKHORSE: "

};  // end namepace AUX

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
 * @param   {boolean} specialCharsFlag - Whether to allow special characters
 *          (represented as (0, 1) for encoding/decoding purposes;
 *          default 1).
 */
var Attributes = function(domain, iterations, truncation, specialCharsFlag) {
    this.domain     = (ENV.types.UNDEFINED === typeof(domain) ?
                       null : domain);
    this.iterations = (ENV.types.UNDEFINED === typeof(iterations) ?
                       null : iterations);
    this.truncation = (ENV.types.UNDEFINED === typeof(truncation) ?
                       Attributes.NO_TRUNCATION : truncation);
    this.specialCharsFlag = (ENV.types.UNDEFINED === typeof(specialCharsFlag) ?
                       1 : specialCharsFlag);
};

/**
 * @summary A static value which indicates that no truncation
 *          will be performed on the result (since the number of
 *          characters to truncate to cannot be negative).
 */
Attributes.NO_TRUNCATION = -1;

/**
 * @summary A function to check if an object of the Attributes class
 *          has been "set", or has the default values.
 * @return  {boolean} "true" or "false"
 */
Attributes.prototype.attributesExist = function() {
    return (null !== this.domain ||
            null !== this.iterations ||
            Attributes.NO_TRUNCATION !== this.truncation ||
            1 !== this.specialCharsFlag);
};

// ------------------------------------------------------------------------
// The AttributesCodec class

/**
 * @namespace AttributesCodec
 * @summary A class to code/decode Attributes objects/lists to/from
 *          the format they are stored in the browser preferences system.
 */
var AttributesCodec = {

    DELIMITER : "|",

    /**
     * @summary Method to encode an Attributes object.
     * @param   {Attributes} attributes - An attributes object.
     * @return  {String} The encoded attributes string;
     *          attributes same as the defaults are skipped.
     */
    encode : function(attributes) {
        if (!attributes.attributesExist()) {
            return "";
        }

        return (((null != attributes.domain) ?
                 attributes.domain : "") +
                AttributesCodec.DELIMITER +
                ((null != attributes.iterations) ?
                 attributes.iterations : "") +
                AttributesCodec.DELIMITER +
                ((Attributes.NO_TRUNCATION != attributes.truncation) ?
                 attributes.truncation : "") +
                AttributesCodec.DELIMITER +
                ((1 != attributes.specialCharsFlag) ?
                 0 : ""));
    },

    /**
     * @summary Method to decode an encoded Attributes string.
     * @param   {string} encodedOverrides - The encoded attribute string,
     *          of the form <salt|iterations|truncation|specialCharsFlag>,
     *          where any of the four may be empty.
     *          This format is chosen to eliminate storing redundant
     *          information for a domain when a cetain attribute is
     *          identical to the one produced by the default algorithm.
     * @return  {Attributes} The decoded object.
     *          If decoding fails,
     *          a default initialized object is returned.
     */
    decode : function(encodedAttributes) {
        // Create a "default-initialized" object of type "Attributes"
        var attributes = new Attributes();

        if (ENV.types.UNDEFINED !== typeof(encodedAttributes)) {
            var attributesArray =
                encodedAttributes.split(AttributesCodec.DELIMITER);
            // Sanity check
            if (4 !== attributesArray.length) {
                console.error(AUX.logCategory +
                              "ERROR: Malformed Attributes! Expected "+
                              "<salt|iterations|truncation|specialCharsFlag>");
                return attributes;
            }

            // Check if the salt is non-empty
            if (attributesArray[0] !== "") {
                attributes.domain = attributesArray[0];
            }

            // Check if the number of iterations is non-empty
            if (attributesArray[1] !== "") {
                attributes.iterations = parseInt(attributesArray[1]);
            }

            // Check if the truncation parameter is non-empty
            if (attributesArray[2] !== "") {
                attributes.truncation = parseInt(attributesArray[2]);
            }

            // Check if special characters were disabled
            if (attributesArray[3] !== "") {
                attributes.specialCharsFlag = 0;
            }
        }

        return attributes;
    },

    /**
     * @summary A function to parse the string representing
     *          the list of site attributes stored in the browser
     *          preferences system.
     * @param   {string} encodedOverridesList - The encoded string
     *          containing a stringified version
     *          of the JSON object representing
     *          the (domain, encoded Attributes) pair.
     * @return  {object} A JSON of key-value pairs with site domain as key
     *          and site attributes (in compact string form) as value.
     *          Values are of the form <salt|iterations|truncation>,
     *          where at most two of those may be empty.
     */
    getEncodedOverridesMap : function(encodedOverridesList) {
        console.debug(AUX.logCategory +
                      "encodedOverridesList=" + encodedOverridesList);

        var encodedOverridesMap = {};

        // JSON.parse will fail if the input string is empty;
        // catch any exceptions and leave it to the user to proceed or not
        // (Empty attributes will be returned in this case).
        try {
            encodedOverridesMap = JSON.parse(encodedOverridesList);
        } catch (e) {
            console.info(AUX.logCategory +
                         "WARN Failed to parse encodedOverridesMap" +
                         ", errorMsg=\"" + e + "\"");
            return {};
        }

        console.debug(AUX.logCategory + "encodedOverridesMap=" +
                      JSON.stringify(encodedOverridesMap,
                                     null,
                                     ENV.indentation));
        return encodedOverridesMap;
    },

    /**
     * @summary A function to obtain the saved "Attributes" object
     *          corresonding to a site domain.
     * @param   {string} domain - The site domain to obtain
     *          the saved attributes for.
     * @param   {object} encodedOverridesMap - The JSON object containing
     *          (domain, encodedOverrides) pairs.
     * @return  {Attributes} The saved overrides. If no saved overrides
     *          exist, a default initialized object is returned.
     */
    getDomainOverrides : function(domain, encodedOverridesMap) {
        var domainOverrides =
            (encodedOverridesMap.hasOwnProperty(domain) ?
             encodedOverridesMap[domain] : undefined);
        return AttributesCodec.decode(domainOverrides);
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
     * @param   {Attributes} attributes - The actual attributes
     *          that were captured from the UI
     *          when the user hit "Generate",
     *          which will encompass any custom changes the user made.
     * @param   {Attributes} proposedAttributes - The proposed attributes
     *          following the application of the algorithm
     *          on the saved/default attributes.
     * @param   {Attributes} savedOverrides - The existing
     *          saved attributes for a domain (default if none existed).
     * @return  {Attributes} The overrides to save, ready for encoding.
     */
    getOverridesToSave : function(attributes,
                                  proposedAttributes,
                                  savedOverrides) {
        var overridesToSave = new Attributes();

        if (savedOverrides.attributesExist()) {
            overridesToSave.domain =
                ((proposedAttributes.domain !== attributes.domain) ?
                 attributes.domain : savedOverrides.domain);
            overridesToSave.iterations =
                ((proposedAttributes.iterations !== attributes.iterations) ?
                 attributes.iterations : savedOverrides.iterations);
            overridesToSave.truncation =
                ((proposedAttributes.truncation !== attributes.truncation) ?
                 attributes.truncation : savedOverrides.truncation);
            overridesToSave.specialCharsFlag =
                ((proposedAttributes.specialCharsFlag !==
                  attributes.specialCharsFlag) ?
                 attributes.specialCharsFlag :
                 savedOverrides.specialCharsFlag);
        } else {
            overridesToSave.domain =
                ((proposedAttributes.domain !== attributes.domain) ?
                 attributes.domain : null);
            overridesToSave.iterations =
                ((proposedAttributes.iterations !== attributes.iterations) ?
                 attributes.iterations : null);
            overridesToSave.truncation =
                ((proposedAttributes.truncation !== attributes.truncation) ?
                 attributes.truncation : Attributes.NO_TRUNCATION);
            overridesToSave.specialCharsFlag =
                ((proposedAttributes.specialCharsFlag !==
                  attributes.specialCharsFlag) ?
                 attributes.specialCharsFlag : 1);
        }

        return overridesToSave;
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
        domainBox                   : "domain",
        passwordBox                 : "password",
        hashBox                     : "hash",
        showHashCheckBox            : "showHash",
        iterationsBox               : "iterations",
        truncateCheckBox            : "truncate",
        truncationBox               : "truncation",
        noSpecialCharsCheckBox      : "noSpecialChars",
        saveOverridesCheckBox       : "saveOverrides",
        overridesSaveSuccessLabel   : "overridesSaveSuccessLabel",
        showAdvancedCheckBox        : "showAdvanced",
        advancedConfigContainer     : "advancedConfig",
        generateButton              : "generateButton",
        helpIcon                    : "helpIcon",
        showSettingsIcon            : "showSettingsIcon",
        exportSettingsIcon          : "exportSettingsIcon",
        importSettingsIcon          : "importSettingsIcon"
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
        showHashListener : function() {
            var checkBox =
                document.getElementById(DOM.elements.showHashCheckBox);
            var hashField = document.getElementById(DOM.elements.hashBox);
            if (checkBox.checked) {
                hashField.type = ENV.inputType.TEXT;
            } else {
                hashField.type = ENV.inputType.PASSWORD;
            }
        },

        /**
         * @summary Event listener for the checkbox to show
         *          "Advanced" settings
         * @return  {undefined}
         */
        showAdvancedListener : function() {
            var checkBox =
                document.getElementById(DOM.elements.showAdvancedCheckBox);
            var advancedConfigContainer =
                document.getElementById(DOM.elements.advancedConfigContainer);
            if (checkBox.checked) {
                advancedConfigContainer.style.display = ENV.display.BLOCK; 
            } else {
                advancedConfigContainer.style.display = ENV.display.NONE; 
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
            var checkBox =
                document.getElementById(DOM.elements.truncateCheckBox);
            var truncationBox =
                document.getElementById(DOM.elements.truncationBox);
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
                                    DOM.elements.domainBox).value,
                               parseInt(document.getElementById(
                                    DOM.elements.iterationsBox).value),
                               parseInt(document.getElementById(
                                    DOM.elements.truncationBox).value),
                               (document.getElementById(
                                 DOM.elements.noSpecialCharsCheckBox).checked ?
                                0 : 1));
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
                                            DOM.elements.passwordBox).value)),
                attributes          : attributes,
                proposedAttributes  : button.proposedAttributes,
                savedOverrides      : button.savedOverrides,
                saveOverrides       : document.getElementById(
                                      DOM.elements.saveOverridesCheckBox)
                                        .checked,
                encodedOverridesMap : button.encodedOverridesMap
            });
        },

        /**
         * @summary Event listener for the "help" event.
         *          Opens the github page with useful deets.
         * @return  {undefined}
         */
        helpListener : function() {
            console.info(AUX.logCategory +
                         "help() handler called...");
            chrome.tabs.create({
                url : "https://manzdagratiano.github.io/gobbledygook/"
            });
        },

        /**
         * @summary Event listener for the "Settings" icon
         * @return  {undefined}
         */
        showSettingsListener        : function() {
            console.info(AUX.logCategory +
                         "showSettings() handler called...");
            if (ENV.types.FUNCTION === typeof(onShowSettings)) {
                onShowSettings();
            }
        },

        /**
         * @summary Event listener for the "exportSettings" icon
         * @return  {undefined}
         */
        exportSettingsListener      : function() {
            console.info(AUX.logCategory +
                         "exportSettings() handler called...");
            if (ENV.types.FUNCTION === typeof(onExportSettings)) {
                onExportSettings();
            }
        },

        /**
         * @summary Event listener for the "importSettings" icon
         * @return  {undefined}
         */
        importSettingsListener      : function() {
            console.info(AUX.logCategory +
                         "importSettings() handler called...");
            if (ENV.types.FUNCTION === typeof(onImportSettings)) {
                onImportSettings();
            }
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
            var hashField = document.getElementById(DOM.elements.hashBox);
            if (eventType === ENV.events.CLICK) {
                hashField.disabled = false;
                hashField.type = ENV.inputType.TEXT;
            } else if (eventType === ENV.events.DONE) {
                hashField.disabled = false;
                hashField.type = ENV.inputType.PASSWORD;
            } else if (eventType === ENV.events.ERROR) {
                hashField.disabled = false;
                hashField.type = ENV.inputType.TEXT;
            } else if (eventType === ENV.events.GENERATE) {
                hashField.disabled = true;
            }

            if (ENV.types.STRING === typeof(message)) {
                hashField.value = message;
            }
        },

        /**
         * @summary A function to toggle the "overridesSaveSuccessLabel"
         *          of the DOM based on whether the operation of
         *          saving site attributes, when asked, was a success.
         *          A "true" input shows a check mark,
         *          while a "false" input shows a cross.
         * @return  {undefined}
         */
        toggleOverridesSaveSuccess : function(success) {
            document.getElementById(
                DOM.elements.overridesSaveSuccessLabel).textContent =
                (success ?  ENV.successString.SUCCESS :
                            ENV.successString.FAILURE);
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
     *          @prop   {string} params.encodedOverridesList - The saved
     *                  attributes list for all sites.
     * @return  {undefined}
     */
    configure : function(options, params) {
        console.info(AUX.logCategory +
                     "workhorseOptions=" +
                     JSON.stringify(options, null, ENV.indentation) +
                     ",\nworkhorseParams=" +
                     JSON.stringify(params, null, ENV.indentation));

        // ----------------------------------------------------------------
        // Private Methods

        /**
         * @summary A function to obtain the domain name of the url
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

            // For compatibility with mobile pages, also remove
            // the starting "www*." or "m."
            // The new JavaScript "startsWith" method (ECMAScript 2015)
            // does not allow regexes, so we check for "www" or "m."
            // and then remove everything upto the first ".", so that
            // prefixes like "www2." etc (load balancing) are also covered.
            // Assumption: No website name is abusing the "www" prefix,
            // i.e., there is never going to be a name like
            // wwwimadethisstupidnameforfun.org
            if (domain.startsWith("www") || domain.startsWith("m.")) {
                var chopIndex = domain.indexOf(".");
                if (-1 !== chopIndex) {
                    // Take everything after the first "."
                    domain = domain.substring(chopIndex + 1);
                } else {
                    // Sanity check; something wild is this happens!
                    console.info(AUX.logCategory + "domain=" + domain +
                                 ", Something wild! Nothing to chop off");
                }
            }
            console.info(AUX.logCategory + "domain=" + domain);

            return domain;
        };

        /**
         * @summary A function to configure
         *          the "domain" element of the DOM.
         * @param   {string} domain - The website domain.
         * @param   {string} savedDomain - The saved domain,
         *          (if overridden, and if exists).
         * @return  {string} The extracted domain name.
         */
        var configureDomain = function(domain,
                                       savedDomain) {
            var domainBox = document.getElementById(DOM.elements.domainBox);
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
                document.getElementById(DOM.elements.iterationsBox);
            var iterations = savedIterations;
            if (!iterations) {
                iterations = defaultIterations;
            }
            if (!iterations) {
                iterations = ENV.defaultIterations;
            }
            console.info(AUX.logCategory + "iterations=" + iterations);
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
            var hashField = document.getElementById(DOM.elements.hashBox);
            hashField.value = "";
            hashField.disabled = true;

            // The "Show" checkbox for the proxy password
            var checkBox =
                document.getElementById(DOM.elements.showHashCheckBox);

            // Add an event listener to the "click" action,
            // which toggles the visibility of the generated hash.
            checkBox.addEventListener(ENV.events.CHANGE,
                                      DOM.listeners.showHashListener);
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
                document.getElementById(DOM.elements.truncationBox);
            truncationBox.disabled =
                (Attributes.NO_TRUNCATION === truncation) ? true : false;
            truncationBox.value = truncation;

            // Configure the "truncate" checkbox of the DOM
            var checkBox =
                document.getElementById(DOM.elements.truncateCheckBox);
            checkBox.checked =
                (Attributes.NO_TRUNCATION === truncation) ? false : true;

            // Add an event listener to the "click" action,
            // which toggles the editability of the truncation parameter.
            checkBox.addEventListener(ENV.events.CHANGE,
                                      DOM.listeners.truncateListener);

            return truncation;
        };

        /**
         * @summary A method to configure the "noSpecialChars" checkbox
         *          of the DOM (unchecked by default).
         * @return  {undefined}
         */
        var configureNoSpecialChars = function(specialCharsFlag) {
            var noSpecialCharsCheckBox =
                document.getElementById(DOM.elements.noSpecialCharsCheckBox);
            noSpecialCharsCheckBox.checked = (0 === specialCharsFlag);
        };

        /**
         * @summary A function to configure the "saveOverrides" checkbox
         *          of the DOM (unchecked by default), and the associated
         *          "overridesSaveSuccessLabel".
         *          By default, the label is empty.
         * @return  {undefined}
         */
        var configureSaveOverrides = function() {
            document.getElementById(
                DOM.elements.saveOverridesCheckBox).checked = false;
            document.getElementById(
                DOM.elements.overridesSaveSuccessLabel).textContent = "";
        };

        /**
         * @summary A function to configure the checkbox to show/hide
         *          "Advanced" settings (iterations, truncation, save custom),
         *          which the user would typically not modify for every run.
         *          By default, the advanced section is hidden.
         * @return  {undefined}
         */
        var configureShowAdvanced = function() {
            var checkBox =
                document.getElementById(DOM.elements.showAdvancedCheckBox);
            var advancedConfigContainer =
                document.getElementById(DOM.elements.advancedConfigContainer);
            // Uncheck the checkbox and hide the "Advanced" panel
            checkBox.checked = false;
            advancedConfigContainer.style.display = ENV.display.NONE; 
            // Add an event listener to the "click" action,
            // which toggles the editability of the truncation parameter.
            checkBox.addEventListener(ENV.events.CHANGE,
                                      DOM.listeners.showAdvancedListener);
        }

        /**
         * @summary A function to configure the "Generate" button
         *          of the DOM.
         * @param   {string} domain - The site domain.
         * @param   {string} saltKey - The key used for generating
         *          the salt from the domain name.
         * @param   {Attributes} proposedAttributes - The proposed
         *          attributes that would be applicable
         *          (after `337-translation etc),
         *          if the user did not make any custom changes.
         * @param   {Attributes} savedOverrides - The saved attributes
         *          of the domain (default if none).
         * @param   {Object} encodedOverridesMap - The saved
         *          custom (domain => encodedOverride) map for all domains.
         * @return  {undefined}
         */
        var configureGenerateButton = function(domain,
                                               saltKey,
                                               proposedAttributes,
                                               savedOverrides,
                                               encodedOverridesMap) {
            var button =
                document.getElementById(DOM.elements.generateButton);

            // Attach the other input parameters to the "button",
            // so that they may be accessed in the attached event listener
            // (cf. below).
            button.domain = domain;
            button.saltKey = saltKey;
            button.proposedAttributes = proposedAttributes;
            button.savedOverrides = savedOverrides;
            button.encodedOverridesMap = encodedOverridesMap;

            // Add an event listener to the "click" action,
            // whose purpose is to gather the values of the elements
            // when "Generate" is hit (the input to the algorithm).
            button.addEventListener(ENV.events.CLICK,
                                    DOM.listeners.generateListener);
        };

        /**
         * @summary Method to configure the settings icons
         * @return  {undefined}
         */
        var configureSettingsIcons = function() {
                document.getElementById(DOM.elements.helpIcon).
                    addEventListener(ENV.events.CLICK,
                                     DOM.listeners.helpListener);
                document.getElementById(DOM.elements.showSettingsIcon).
                    addEventListener(ENV.events.CLICK,
                                      DOM.listeners.showSettingsListener);
                document.getElementById(DOM.elements.exportSettingsIcon).
                    addEventListener(ENV.events.CLICK,
                                     DOM.listeners.exportSettingsListener);
                document.getElementById(DOM.elements.importSettingsIcon).
                    addEventListener(ENV.events.CLICK,
                                     DOM.listeners.importSettingsListener);
        };

        // ----------------------------------------------------------------
        // Check if the saltKey exists;
        // if not, generate a new one (e.g., on first run).
        // This allows taking the generation onus away from the user
        // and makes the addon out-of-the-box ready.
        if (!params.saltKey) {
            console.info(AUX.logCategory + "No saltKey found. " +
                         "Generating a new one...");
            // Generate a new key
            params.key = Keygen.generate();
            // Send an event to set the salt key to SimplePrefs.
            // We do not need to wait for returning from this
            // asynchronous operation, as this is only for future usage.
            if (ENV.types.FUNCTION === typeof(onGenerateSaltKey)) {
                onGenerateSaltKey(params.key);
            }
        }

        // ----------------------------------------------------------------
        // Configure all the elements.

        console.info(AUX.logCategory + "Configuring elements...");

        // Check and extract saved site attributes, if any.
        var domain = extractDomain(params.url);

        var encodedOverridesMap =
            AttributesCodec.getEncodedOverridesMap(
                                    params.encodedOverridesList);
        var savedOverrides =
            AttributesCodec.getDomainOverrides(domain,
                                               encodedOverridesMap);
        console.info(AUX.logCategory + "savedOverrides=" +
                     JSON.stringify(savedOverrides,
                                    null,
                                    ENV.indentation));

        var proposedAttributes =
            new Attributes(configureDomain(domain,
                                           savedOverrides.domain),
                           configureIterations(params.defaultIterations,
                                               savedOverrides.iterations),
                           configureTruncation(
                                         savedOverrides.truncation),
                           configureNoSpecialChars(
                                         savedOverrides.specialCharsFlag));

        configureHash();
        configureSaveOverrides();
        configureShowAdvanced();
        configureGenerateButton(domain,
                                params.saltKey,
                                proposedAttributes,
                                savedOverrides,
                                encodedOverridesMap);
        configureSettingsIcons();
    },

    /**
     * @summary Function to "deconfigure" the DOM so that it is ready for
     *          a new set of inputs.
     * @return  {undefined}
     */
    deconfigure : function() {
        console.info(AUX.logCategory + "Cleaning up resources...");
        // Remove all the event listeners
        document.getElementById(DOM.elements.generateButton).
            removeEventListener(ENV.events.CLICK,
                                DOM.listeners.generateListener);
        document.getElementById(DOM.elements.truncateCheckBox).
            removeEventListener(ENV.events.CHANGE,
                                DOM.listeners.truncateListener);
        document.getElementById(DOM.elements.showHashCheckBox).
            removeEventListener(ENV.events.CHANGE,
                                DOM.listeners.showHashListener);
        // Advanced settings
        document.getElementById(DOM.elements.showAdvancedCheckBox).
            removeEventListener(ENV.events.CHANGE,
                                DOM.listeners.showAdvancedListener);
        // Settings icons
        document.getElementById(DOM.elements.helpIcon).
            removeEventListener(ENV.events.CLICK,
                                DOM.listeners.helpListener);
        document.getElementById(DOM.elements.showSettingsIcon).
            removeEventListener(ENV.events.CLICK,
                                DOM.listeners.showSettingsListener);
        document.getElementById(DOM.elements.exportSettingsIcon).
            removeEventListener(ENV.events.CLICK,
                                DOM.listeners.exportSettingsListener);
        document.getElementById(DOM.elements.importSettingsIcon).
            removeEventListener(ENV.events.CLICK,
                                DOM.listeners.importSettingsListener);
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
     *          @prop {Attributes} params.attributes - The current
     *                  attributes for the domain captured from the DOM
     *                  when "Generate" was hit.
     *          @prop {Attributes} params.proposedAttributes - The proposed
     *                  attributes that would be applicable
     *                  if the user did not make any custom changes,
     *          @prop {boolean} params.saveOverrides - A flag to determine
     *                  if the user asked to save the current overrides
     *                  for this domain,
     *          @prop {Attributes} params.savedOverrides The saved
     *                  overrides for the domain (default if none exist),
     *          @prop {Object} params.encodedOverridesMap - The saved
     *                  (domain => encodedOverride) map for all domains,
     *                  which will be mutated with the current
     *                  attributes if "saveOverrides" is true.
     * @return  {undefined}
     */
    generate : function(params) {
        // Clear the password field immediately
        document.getElementById(DOM.elements.passwordBox).value = "";
        // Clear the overridesSaveSuccessLabel (in case set)
        document.getElementById(
                DOM.elements.overridesSaveSuccessLabel).textContent = "";
        // Toggle the "show" checkbox
        document.getElementById(DOM.elements.showHashCheckBox).checked =
            false;
        DOM.togglers.toggleHashField(ENV.events.GENERATE, "");

        console.debug(AUX.logCategory + "generateParams=" +
                      JSON.stringify(params, null, ENV.indentation));

        // Check if Web Workers are supported
        if (ENV.types.UNDEFINED === typeof(Worker)) {
            // No support for web workers;
            // do nothing,
            // since doing intensive operations here will
            // block the UI thread.
            // Modern browsers support the API, so no reason to be burdened
            // by chains of the past.
            console.error(AUX.logCategory +
                          "ERROR: No web worker support!");
            DOM.togglers.toggleHashField(ENV.events.ERROR,
                                         "ERROR: " +
                                         "browser.Type === ANCIENT");
            return;
        }

        console.info(AUX.logCategory + "Firing web worker HASHER...");
        var hasher = new Worker("hasher.js");

        hasher.onmessage = function(oEvent) {
            var logCategory = "HASHER: ";
            console.debug(logCategory +
                          JSON.stringify(oEvent.data,
                                         null,
                                         ENV.indentation));
            var eventData = oEvent.data;

            // Finalize the worker, if this is the "done" signal
            if (eventData.hasOwnProperty(ENV.events.DONE)) {
                console.info(AUX.logCategory + "Finalizing worker...");
                DOM.togglers.toggleHashField(ENV.events.DONE,
                                             eventData.password);

                var encodedOverridesMap = params.encodedOverridesMap;
                var modifiedEncodedOverrides = "";
                if (params.saveOverrides) {
                    console.info(AUX.logCategory +
                                 "Generating custom overrides list...");

                    var overridesToSave =
                        AttributesCodec.getOverridesToSave(
                                            params.attributes,
                                            params.proposedAttributes,
                                            params.savedOverrides);
                    console.info(AUX.logCategory, "overridesToSave=" +
                                 JSON.stringify(overridesToSave,
                                                null,
                                                ENV.indentation));
                    if (overridesToSave.attributesExist()) {
                        // Create new, or update existing
                        encodedOverridesMap[params.domain] =
                            AttributesCodec.encode(overridesToSave);
                        console.debug(AUX.logCategory +
                                      "Saving customOverrides=" +
                                      JSON.stringify(encodedOverridesMap,
                                                     null,
                                                     ENV.indentation));
                        modifiedEncodedOverrides =
                            JSON.stringify(encodedOverridesMap);
                    } else {
                        console.info(AUX.logCategory +
                                     "No custom changes to save");
                    }
                }

                // Check if a function "finalize" is defined,
                // and if so, call it.
                if (ENV.types.FUNCTION === typeof(finalize)) {
                    finalize({
                        domain                      : params.domain,
                        password                    : eventData.password,
                        modifiedEncodedOverrides    : modifiedEncodedOverrides
                    });
                }

                // Post a message to the worker to close itself.
                // DO NOT CALL "hasher.terminate()",
                // which rudely terminates the web worker
                // without a chance to clean up.
                hasher.postMessage({ done : ENV.events.DONE });
            };
        };

        hasher.onerror = function(oEvent) {
            throw new Error(oEvent.message +
                            " (" + oEvent.filename +
                            ":" + oEvent.lineno + ")");
        };

        DOM.togglers.toggleHashField(ENV.events.CLICK,
                                     "<Generating...>");
        var hasherParams = {
            saltKey         : params.saltKey,
            seedSHA         : params.seedSHA,
            attributes      : params.attributes,
        };
        hasher.postMessage(hasherParams);
    }

};  // end namespace Workhorse
