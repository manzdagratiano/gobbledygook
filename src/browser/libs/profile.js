/**
 * @module      profile
 * @overview    Auxiliary methods for managing profile settings.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Jun 20, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

// ========================================================================
// METHODS

var Profile                         = {

    schema                          : {
        profiles                    : "profiles",
        name                        : "name",
        settings                    : "settings",
        saltKey                     : "saltKey",
        defaultIterations           : "defaultIterations",
        customOverrides             : "customOverrides"
    },

    defaultProfileName              : "root",

    /**
     * @brief   A method to validate the schema for the settings file.
     * @return  {boolean} True or False
     */
    validateAndReturnPreferences    : function(settings) {
        // Sanity check
        if (!settings.hasOwnProperty(Profile.schema.profiles)) {
            return false;
        }

        // Extract the profile
        // (at this time, "there can be only one").
        var profileList = settings[Profile.schema.profiles];
        if (1 !== profileList.length) {
            return false;
        }
        var defaultProfile = profileList[0];

        // Extract the profile settings
        if (!(defaultProfile.hasOwnProperty(Profile.schema.name) &&
              defaultProfile.hasOwnProperty(Profile.schema.settings) &&
              (2 == Object.keys(defaultProfile).length))) {
            return false;
        }
        var profileSettings = defaultProfile[Profile.schema.settings];

        // Sanity check
        if (!(profileSettings.hasOwnProperty(
                                    Profile.schema.saltKey) &&
              profileSettings.hasOwnProperty(
                                    Profile.schema.defaultIterations) &&
              profileSettings.hasOwnProperty(
                                    Profile.schema.customOverrides) &&
              (3 === Object.keys(profileSettings).length))) {
            return false;
        }

        return profileSettings;
    },

    /**
     * @brief   A method to construct a settings object to export as JSON.
     * @param   {Object} profileSettings
     *          @prop {string} saltKey - The salt key
     *          @prop {int} defaultIterations - The default #
     *                of PBKDF2 iterations.
     *          @prop {string} customOverrides - The list of
     *                custom overridden attributes.
     * @return  {Object} The settings object for export
     */
    makeSettings                    : function(profileSettings) {
        // Construct the settings object as holding an array of profiles
        // with a single profile in it.
        var defaultProfile = {};
        defaultProfile[Profile.schema.name] = Profile.defaultProfileName;
        defaultProfile[Profile.schema.settings] = profileSettings;

        var settings = {};
        settings[Profile.schema.profiles] = [defaultProfile];

        return settings;
    }
}
