/**
 * @module      gobbledygook_tests_crypto.js
 * @overview    Unit tests for the Attributes and AttributesCodec classes
 *              in Gobbledygook.
 *              Requires the QUnit framework.
 *
 * @author      Manjul Apratim (manjul.apratim@gmail.com)
 * @date        Sep 05, 2016
 *
 * @license     GNU General Public License v3 or Later
 * @copyright   Manjul Apratim, 2016
 */

"use strict";

// ========================================================================

QUnit.module("Attributes");

QUnit.test("TestAttributesDon__tExist", function(assert) {
    var attributes = new Attributes();
    assert.ok(true !== attributes.attributesExist(),
              "AttributesDon__tExist.QED!");
});

QUnit.test("TestAttributesExist_Domain", function(assert) {
    var attributes = new Attributes();
    attributes.domain = "google.com";
    assert.ok(true === attributes.attributesExist(),
              "AttributesExist_Domain.QED!");
});

QUnit.test("TestAttributesExist_Iterations", function(assert) {
    var attributes = new Attributes();
    attributes.iterations = 10000;
    assert.ok(true === attributes.attributesExist(),
              "AttributesExist_Iterations.QED!");
});

QUnit.test("TestAttributesExist_Truncation", function(assert) {
    var attributes = new Attributes();
    attributes.truncation = 20;
    assert.ok(true === attributes.attributesExist(),
              "AttributesExist_Truncation.QED!");
});

QUnit.test("TestAttributesExist_NoSpecialChars", function(assert) {
    var attributes = new Attributes();
    attributes.specialCharsFlag = 0;
    assert.ok(true === attributes.attributesExist(),
              "AttributesExist_NoSpecialChars.QED!");
});

QUnit.test("TestOverridesToSave_Don__tExist", function(assert) {
    var savedOverrides = new Attributes();
    var proposedAttributes = new Attributes("google.com", 10000, -1, 1);
    var attributes = new Attributes("google.com", 10000, -1, 1);
    assert.ok(!(AttributesCodec.getOverridesToSave(attributes,
                                                   proposedAttributes,
                                                   savedOverrides)
                    .attributesExist()),
              "OverridesToSave_Don__tExist.QED!");
});

QUnit.test("TestOverridesToSave_Don__tExistReprise", function(assert) {
    var savedOverrides = new Attributes(null, 10003, -1, 1);
    var proposedAttributes = new Attributes("google.com", 10003, -1, 1);
    var attributes = new Attributes("google.com", 10003, -1, 1);
    assert.ok(!(AttributesCodec.getOverridesToSave(attributes,
                                                   proposedAttributes,
                                                   savedOverrides)
                    .attributesExist()),
              "OverridesToSave_Don__tExistReprise.QED!");
});

QUnit.test("TestOverridesToSave_Exist", function(assert) {
    var savedOverrides = new Attributes();
    var proposedAttributes = new Attributes("google.com", 10000, -1, 1);
    var attributes = new Attributes("google.com", 10003, -1, 0);
    var overridesToSave = new Attributes(null, 10003, -1, 0);
    assert.ok(JSON.stringify(
                AttributesCodec.getOverridesToSave(attributes,
                                                   proposedAttributes,
                                                   savedOverrides)) ===
              JSON.stringify(overridesToSave),
              "OverridesToSave_Exist.QED!");
});

QUnit.test("TestOverridesToSave_ExistReprise", function(assert) {
    var savedOverrides = new Attributes(null, 10003, -1, 0);
    var proposedAttributes = new Attributes("google.com", 10003, -1, 0);
    var attributes = new Attributes("google.com", 10005, -1, 0);
    var overridesToSave = new Attributes(null, 10005, -1, 0);
    assert.ok(JSON.stringify(
                AttributesCodec.getOverridesToSave(attributes,
                                                   proposedAttributes,
                                                   savedOverrides)) ===
              JSON.stringify(overridesToSave),
              "OverridesToSave_ExistReprise.QED!");
});
