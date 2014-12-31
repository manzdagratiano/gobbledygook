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
ICON = ./icon
SRC = ./src
SJCL = ./sjcl
SJCL_MEGALITH = ./target/sjcl_megalith.js
TARGET_DIR = ./target

# The chrome version of "gobbledygook.html" requires that
# the JS scripts be included inside the html using the "script" directive
# (unlike firefox, where passing down contentScriptFiles handles scope).
# To maintain a common version of the HTML, for the chrome target,
# we will directly inject the "script" directives into the HTML
# once it is copied over to the target directory.
CHROME_HTML_REGEX = "s/<\/footer>/<\/footer>\n\n"
CHROME_HTML_REGEX += "   <script src=\"workhorse.js\"><\/script>\n"
CHROME_HTML_REGEX += "   <script src=\"workhorsefunctions.js\"><\/script>\n"
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
	cp -r $(SRC)/chrome ./target/
	cp $(SRC)/gobbledygook.html $(TARGET)/
	perl -p -i -e $(subst " ", ,$(CHROME_HTML_REGEX)) \
		$(TARGET)/gobbledygook.html
	cp $(SRC)/hasher.js $(TARGET)/
	cp $(SRC)/keygen.js $(TARGET)/
	cp $(SRC)/workhorsefunctions.js $(TARGET)/
	mkdir $(TARGET)/sjcl
	cp -r $(SJCL)/README{,.md} $(TARGET)/sjcl/
	cp $(SJCL_MEGALITH) $(TARGET)/sjcl/
	mkdir $(TARGET)/icon
	cp $(ICON)/icon-{16,19,32,38,48,64,128}.png $(TARGET)/icon/
	echo "Success!"

firefox: TARGET = $(TARGET_DIR)/firefox
firefox: firefox-clean sjcl
	cp -r $(SRC)/firefox ./target/
	cp ./README.md $(TARGET)/
	cp $(SRC)/gobbledygook.html $(TARGET)/data/
	cp $(SRC)/hasher.js $(TARGET)/data/
	cp $(SRC)/keygen.js $(TARGET)/data/
	cp $(SRC)/workhorsefunctions.js $(TARGET)/data/
	mkdir $(TARGET)/data/sjcl
	cp -r $(SJCL)/README{,.md} $(TARGET)/data/sjcl/
	cp $(SJCL_MEGALITH) $(TARGET)/data/sjcl/
	mkdir $(TARGET)/data/icon
	cp $(ICON)/icon-{16,32,48,64}.png $(TARGET)/data/icon/
	echo "Success!"

sjcl: target-dir
	cat $(SRC)/sjcl_megalith_header.js \
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
