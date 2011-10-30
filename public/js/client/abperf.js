goog.provide('abperf');

goog.require('abperf.TestGroup');
goog.require('abperf.Test');
goog.require('goog.dom');
goog.require('goog.array');

abperf.init = function() {
    var elements = abperf.getStyleElements();
    var testGroups = abperf.createTestGroups(elements);

    if (goog.DEBUG) {
        console.log('All test groups:', testGroups);
    }

    abperf.runTestGroups(testGroups);
}

/**
 * @return {array<HTMLStyleElement>} The <style> elements containing test CSS.
 */
abperf.getStyleElements = function() {
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
abperf.createTestGroups = function(elements) {
    var tests = {};
    for (var i = 0; i < elements.length; i++) {
        var name = elements[i].getAttribute('name') || 'EMPTY NAME';
        var css = elements[i].textContent;

        if (typeof tests[name] === 'undefined') {
            tests[name] = new abperf.TestGroup(name);
        }

        tests[name].addTest(new abperf.Test(name, css));

        goog.dom.removeNode(elements[i]);
    }
    return tests;
}

/**
 * @param {object<string, abperf.TestGroup>} testGroups
 */
abperf.runTestGroups = function(testGroups) {
    for (var key in testGroups) {
        testGroups[key].chooseTestAndRunIt();
    }
}

goog.require('onDOMContentLoaded');
onDOMContentLoaded(abperf.init);
