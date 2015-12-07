# @file	        Makefile
# @brief        GNU Makefile for the 'Gobbledygook' project.
#               This Makefile facilitates the assembly of
#               the various components of each target plugin from
#               the source directories into the target directories.
#
# @author       Manjul Apratim (manjul.apratim@gmail.com)
# @date         Dec 14, 2014
#
# @license      GNU General Public License v3 or Later
# @copyright    Manjul Apratim, 2014

# =========================================================================
# VARIABLES

# Locations for components
ROOT = ../..
LIB = ./libs
HTML = ./html
ANDROID = $(HTML)/android
ICON = ./gobbledygook/icon
CHROME_GOBBLEDYGOOK = ./gobbledygook/chrome
FIREFOX_GOBBLEDYGOOK = ./gobbledygook/firefox
TARGET_DIR = ./target
SJCL = $(ROOT)/sjcl
SJCL_MEGALITH = $(TARGET_DIR)/sjcl_megalith.js

# -------------------------------------------------------------------------
# BUILD TAG

# Firefox addons are versioned as major[.minor[.patch[.build]]]
# where "major" and "patch" are integers and "minor" and "build" are strings.
# In our case:
#	* the major is 1 (until we change it),
#	* the minor is the date YYYYMMDD
#	* the patch is always 0 (no need based on the minor convention)
#	* the build is the git commit SHA of the repository

# Chrome extensions are versioned with x.y.w.z
# where x, y, w, z are non-negative integers less than 65535.
# This convention is problematic for us, but luckily,
# chrome also has a "version_name" attribute which is a string and is used for
# display purposes, so we shall use the same string as Firefox.

BUILDTAG = 1.`date +'%Y%m%d'`.0.`git rev-parse --short HEAD`
BUILD_REGEX = "s/\@BUILDTAG/$(BUILDTAG)/"

# We shall use the number of commits in the master branch
# to create a version for the chrome extension.
CHROME_VERSION = 1.`git rev-list --count HEAD`.0
CHROME_VERSION_REGEX = "s/\@BUILDVER/$(CHROME_VERSION)/"

# -------------------------------------------------------------------------
# MISCELLANEOUS

# The chrome version of "gobbledygook.html" requires that
# the JS scripts be included inside the html using the "script" directive
# (unlike firefox, where passing down contentScriptFiles handles scope).
# To maintain a common version of the HTML, for the chrome target,
# we will directly inject the "script" directives into the HTML
# once it is copied over to the target directory.
CHROME_HTML_REGEX = "s/<\/footer>/<\/footer>\n\n"
CHROME_HTML_REGEX += "   <script src=\"workhorse.js\"><\/script>\n"
CHROME_HTML_REGEX += "   <script src=\"workhorsefunctions.js\"><\/script>\n"
CHROME_HTML_REGEX += "   <script src=\"hasher.js\"><\/script>\n"
CHROME_HTML_REGEX += "   <script src=\"keygen.js\"><\/script>\n"
CHROME_HTML_REGEX += "   <script src=\"sjcl\/sjcl_megalith.js\"><\/script>/"

# =========================================================================
# TARGETS

# Declare all targets to be 'PHONY',
# since the recipes are merely a series of commands to be executed.

.PHONY: all firefox chrome target-dir clean firefox-clean chrome-clean

# -------------------------------------------------------------------------
# 'BUILD' TARGETS

all: chrome firefox

chrome: TARGET = $(TARGET_DIR)/chrome
chrome: chrome-clean sjcl
	cp -r $(CHROME_GOBBLEDYGOOK) $(TARGET_DIR)/
	perl -p -i -e $(subst " ", ,$(BUILD_REGEX)) \
		$(TARGET)/manifest.json
	perl -p -i -e $(subst " ", ,$(CHROME_VERSION_REGEX)) \
		$(TARGET)/manifest.json
	cp $(ROOT)/README.md $(TARGET)/
	cp $(HTML)/* $(TARGET)/
	cp $(ANDROID)/* $(TARGET)/
	perl -p -i -e $(subst " ", ,$(CHROME_HTML_REGEX)) \
		$(TARGET)/gobbledygook.html
	cp $(LIB)/{hasher,keygen,workhorsefunctions}.js $(TARGET)/
	mkdir $(TARGET)/{icon,sjcl}
	cp $(ICON)/icon-{16,19,32,38,48,64,128}.png $(TARGET)/icon/
	cp -r $(SJCL)/README{,.md} $(TARGET)/sjcl/
	cp $(SJCL_MEGALITH) $(TARGET)/sjcl/
	echo "Success!"

firefox: TARGET = $(TARGET_DIR)/firefox
firefox: firefox-clean sjcl
	cp -r $(FIREFOX_GOBBLEDYGOOK) ./target/
	perl -p -i -e $(subst " ", ,$(BUILD_REGEX)) \
		$(TARGET)/package.json
	cp $(ROOT)/README.md $(TARGET)/
	cp $(HTML)/*.html $(TARGET)/data/
	cp $(ANDROID)/* $(TARGET)/data/
	cp $(LIB)/{hasher,keygen,workhorsefunctions}.js $(TARGET)/data/
	mkdir $(TARGET)/data/{icon,sjcl}
	cp $(ICON)/icon-{16,32,48,64}.png $(TARGET)/data/icon/
	cp -r $(SJCL)/README{,.md} $(TARGET)/data/sjcl/
	cp $(SJCL_MEGALITH) $(TARGET)/data/sjcl/
	echo "Success!"

sjcl: target-dir
	cat $(LIB)/sjcl_megalith_header.js \
		$(SJCL)/core/sjcl.js \
		$(SJCL)/core/aes.js \
		$(SJCL)/core/bitArray.js \
		$(SJCL)/core/codecString.js \
		$(SJCL)/core/codecHex.js \
		$(SJCL)/core/codecBase64.js \
		$(SJCL)/core/sha256.js \
		$(SJCL)/core/ccm.js \
		$(SJCL)/core/ocb2.js \
		$(SJCL)/core/gcm.js \
		$(SJCL)/core/hmac.js \
		$(SJCL)/core/pbkdf2.js \
		$(SJCL)/core/random.js \
		$(SJCL)/core/convenience.js > $(SJCL_MEGALITH)

target-dir:
	if [ ! -d $(TARGET_DIR) ]; then mkdir $(TARGET_DIR); fi

# -------------------------------------------------------------------------
# 'CLEAN' TARGETS

clean: firefox-clean chrome-clean

chrome-clean: TARGET = $(TARGET_DIR)/chrome
chrome-clean:
	if [ -d $(TARGET) ]; then rm -r $(TARGET); fi

firefox-clean: TARGET = $(TARGET_DIR)/firefox
firefox-clean:
	if [ -d $(TARGET) ]; then rm -r $(TARGET); fi
