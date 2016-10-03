# @file	        Makefile
# @brief        GNU Makefile for the 'Gobbledygook' project.
#               This Makefile facilitates the assembly of
#               the various components of each target plugin from
#               the source directories into the build directories.
#
# @author       Manjul Apratim (manjul.apratim@gmail.com)
# @date         Dec 14, 2014
#
# @license      GNU General Public License v3 or Later
# @copyright    Manjul Apratim, 2014

# =========================================================================
# VARIABLES

# Source locations
ROOT = ../..

LIB = ./libs
HTML = ./html
ICON = ./gobbledygook/icon
SJCL = $(ROOT)/sjcl

CHROME = ./gobbledygook/chrome
FIREFOX = ./gobbledygook/firefox

# -------------------------------------------------------------------------
# BUILD ARTEFACTS

BUILDDIR = ./build
BASEDIR = ./build/base
SJCL_MEGALITH = $(BUILDDIR)/sjcl_megalith.js

# -------------------------------------------------------------------------
# BUILD TAG

# Addons are versioned as major[.minor[.patch[.build]]]
# where "major" and "patch" are integers and "minor" and "build" are strings.
# In our case:
#	* the major is 2 (until we change it)
#	* the minor is the date YYYYMMDD
#	* the patch is always "0" (unnecessary with the minor being the date)
#	* the build is the git commit SHA of the repository (and branch)

MAJOR=2
DATE=`date +'%Y%m%d'`
BRANCH=`git rev-parse --abbrev-ref HEAD`
SHA=`git rev-parse --short HEAD`
COMMITS=`git rev-list --count HEAD`

BUILDTAG=$(MAJOR).$(DATE).0.$(BRANCH)\@$(SHA)
BUILD_REGEX="s/\@BUILDTAG/$(BUILDTAG)/"

# Firefox extensions follow the "toolkit version format",
# which consists of one or more version parts, separated by dots.
# Each version part itself is interpreted as a sequence of four parts
# <number><string><number><string>, where strings are strictly non-numeric.
# However, it seems with the latest revision of the extension linter,
# the last part MUST have a string suffix, followed by an optional "-<string>",
# which can mix numbers and letters:
# /^(\d+\.?){1,3}\.(\d+([A-z]+(\-?[\dA-z]+)?))$/
# https://github.com/mozilla/addons-linter/blob/master/src/schema/formats.js
# Therefore, we'll concoct the version using the number of commits,
# the name of the branch, and the git SHA.
FIREFOX_VERSION=$(MAJOR).$(DATE).$(COMMITS)$(BRANCH)-$(SHA)
FIREFOX_VERSION_REGEX="s/\@BUILDVER/$(FIREFOX_VERSION)/"

# Chrome extensions are versioned with x.y.w.z
# where x, y, w, z are non-negative integers less than 65535.
# This convention is problematic for us, but luckily,
# chrome also has a "version_name" attribute which is a string and is used for
# display purposes, which shall be set to the BUILDTAG.
# Since neither the date nor the SHA can be used in the version,
# we shall use the number of commits in the master branch
# to create a version for the chrome extension.
CHROME_VERSION=$(MAJOR).$(COMMITS).0
CHROME_VERSION_REGEX="s/\@BUILDVER/$(CHROME_VERSION)/"

# =========================================================================
# TARGETS

# Declare all targets to be 'PHONY',
# since the recipes are merely a series of commands to be executed.

.PHONY: all firefox chrome builddir clean clean-firefox-build clean-chrome-build

# -------------------------------------------------------------------------
# 'BUILD' TARGETS

all: chrome firefox

chrome: BUILD=$(BUILDDIR)/chrome
chrome: clean-chrome-build $(SJCL_MEGALITH) base
	cp -r $(CHROME) $(BUILDDIR)/
	perl -p -i -e $(subst " ", ,$(CHROME_VERSION_REGEX)) \
		$(BUILD)/manifest.json
	perl -p -i -e $(subst " ", ,$(BUILD_REGEX)) \
		$(BUILD)/manifest.json
	mv $(BASEDIR)/* $(BUILD)/
	echo "Success!"

firefox: BUILD=$(BUILDDIR)/firefox
firefox: clean-firefox-build $(SJCL_MEGALITH) base
	cp -r $(FIREFOX) ./build/
	perl -p -i -e $(subst " ", ,$(FIREFOX_VERSION_REGEX)) \
		$(BUILD)/manifest.json
	mv $(BASEDIR)/* $(BUILD)/
	echo "Success!"

# The target name is the same as the filename,
# so that we can touch the file and inform "make"
# that this step is complete.
$(SJCL_MEGALITH): builddir
	cat $(LIB)/sjcl_megalith_header.js \
		$(SJCL)/core/sjcl.js \
		$(SJCL)/core/aes.js \
		$(SJCL)/core/bitArray.js \
		$(SJCL)/core/codecString.js \
		$(SJCL)/core/codecHex.js \
		$(SJCL)/core/codecBase64.js \
		$(SJCL)/core/codecZ85.js \
		$(SJCL)/core/sha256.js \
		$(SJCL)/core/ccm.js \
		$(SJCL)/core/ocb2.js \
		$(SJCL)/core/gcm.js \
		$(SJCL)/core/hmac.js \
		$(SJCL)/core/pbkdf2.js \
		$(SJCL)/core/random.js \
		$(SJCL)/core/convenience.js > $(SJCL_MEGALITH)
	touch $@

base: basedir
	cp $(ROOT)/README.md $(BASEDIR)/
	cp $(ROOT)/LICENSE $(BASEDIR)/
	cp -r $(HTML)/{*.html,android} $(BASEDIR)/
	cp $(LIB)/{env,eventpage,hasher,keygen,options,profile,workhorse,workhorsefunctions}.js $(BASEDIR)/
	mkdir $(BASEDIR)/{icon,sjcl}
	cp $(ICON)/icon-{16,19,32,38,48,64,128}.png $(BASEDIR)/icon/
	cp -r $(SJCL)/README{,.md} $(BASEDIR)/sjcl/
	cp $(SJCL_MEGALITH) $(BASEDIR)/sjcl/

builddir:
	if [ ! -d $(BUILDDIR) ]; then mkdir $(BUILDDIR); fi

basedir:
	if [ ! -d $(BASEDIR) ]; then mkdir $(BASEDIR); fi

# -------------------------------------------------------------------------
# 'CLEAN' TARGETS

clean: clean-firefox-build clean-chrome-build

clean-chrome-build: BUILD=$(BUILDDIR)/chrome
clean-chrome-build: clean-base clean-sjcl-megalith
	if [ -d $(BUILD) ]; then rm -r $(BUILD); fi

clean-firefox-build: BUILD=$(BUILDDIR)/firefox
clean-firefox-build: clean-base clean-sjcl-megalith
	if [ -d $(BUILD) ]; then rm -r $(BUILD); fi

clean-base:
	if [ -d $(BASEDIR) ]; then rm -r $(BASEDIR); fi

clean-sjcl-megalith:
	rm -f $(SJCL_MEGALITH)
