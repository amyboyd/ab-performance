goog.provide('abperf.styles');

goog.require('abperf.constants');
goog.require('abperf.styles.Test');
goog.require('abperf.styles.TestGroup');
goog.require('goog.array');
goog.require('goog.dom');

abperf.styles.runningTests = {};

abperf.styles.start = function() {
    var elements = abperf.styles.getStyleElements();
    var testGroups = abperf.styles.createTestGroups(elements);

    if (goog.DEBUG) {
        console.log('All test groups:', testGroups);
    }

    abperf.styles.runTestGroups(testGroups);
}

/**
 * @return {array<HTMLStyleElement>} The <style> elements containing test CSS.
 */
abperf.styles.getStyleElements = function() {
    var allStyles = goog.dom.$$('style', null);
    var abStyles = [];
    for (var i = 0; i < allStyles.length; i++) {
        var tag = allStyles[i]; /** @type {HTMLStyleElement} */ 

        if (tag.getAttribute('type') === 'ab-perf') {
            abStyles.push(tag);
        }
    }
    return abStyles;
}

/**
 * @param {array<HTMLStyleElement>} elements
 * @return {object<string, abperf.TestGroup>}
 */
abperf.styles.createTestGroups = function(elements) {
    var tests = {};
    for (var i = 0; i < elements.length; i++) {
        var name = elements[i].getAttribute('name') || 'EMPTY NAME';
        var css = elements[i].textContent;

        if (typeof tests[name] === 'undefined') {
            tests[name] = new abperf.styles.TestGroup(name);
        }

        tests[name].addTest(new abperf.styles.Test(name, css));

        goog.dom.removeNode(elements[i]);
    }
    return tests;
}

/**
 * @param {object<string, abperf.TestGroup>} testGroups
 */
abperf.styles.runTestGroups = function(testGroups) {
    for (var key in testGroups) {
        testGroups[key].chooseTestAndRunIt();
    }
}
