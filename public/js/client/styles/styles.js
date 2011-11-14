goog.provide('abperf.styles');

goog.require('abperf.constants');
goog.require('abperf.styles.Test');
goog.require('abperf.styles.TestGroup');

abperf.styles.start = function() {
    var elements = abperf.styles.getStyleElements();
    abperf.styles.tests = abperf.styles.createTests(elements);
    abperf.styles.testGroups = abperf.styles.createTestGroups(abperf.styles.tests);

    if (goog.DEBUG) {
        console.log('All test groups:', abperf.styles.testGroups);
    }

    abperf.styles.runTestGroups(abperf.styles.testGroups);
}

/**
 * @return {array<HTMLStyleElement>} The <style> elements containing test CSS.
 */
abperf.styles.getStyleElements = function() {
    var allStyles = document.getElementsByTagName('style');
    var abStyles = [];
    for (var i = 0; i < allStyles.length; i++) {
        var tag = allStyles[i]; /** @type {HTMLStyleElement} */ 

        if (tag.getAttribute('type') === 'abperf') {
            abStyles.push(tag);
        }
    }
    return abStyles;
}

/**
 * @param {array<HTMLStyleElement>} elements
 * @return {array<abperf.styles.Test>}
 */
abperf.styles.createTests = function(elements) {
    var tests = [];
    for (var i = 0; i < elements.length; i++) {
        var groupName = elements[i].getAttribute('name') || 'EMPTY NAME';
        var css = elements[i].textContent;
        var test = new abperf.styles.Test(groupName, css);
        tests.push(test);
        elements[i].parentNode.removeChild(elements[i]);
    }
    return tests;
}

/**
 * @param {array<abperf.styles.Test>} tests
 * @return {object<string, abperf.styles.TestGroup>}
 */
abperf.styles.createTestGroups = function(tests) {
    var groups = {};
    for (var i = 0; i < tests.length; i++) {
        var groupName = tests[i].testGroupName;
        if (typeof groups[groupName] === 'undefined') {
            groups[groupName] = new abperf.styles.TestGroup(groupName);
        }
        groups[groupName].addTest(tests[i]);
    }
    return groups;
}

/**
 * @param {object<string, abperf.styles.TestGroup>} testGroups
 */
abperf.styles.runTestGroups = function(testGroups) {
    for (var key in testGroups) {
        testGroups[key].chooseTestAndInstallIt();
    }
}

/** @type {array<abperf.styles.Test>} */
abperf.styles.tests = [];

/** @type {object<string, abperf.styles.TestGroup>} */
abperf.styles.testGroups = {};

/** @type {object<string, abperf.styles.Test>} The key is the test group name */
abperf.styles.runningTests = {};

/**
 * @param {string} id
 * @return {abperf.styles.Test}
 */
abperf.styles.findRunningTestByID = function(id) {
    for (var key in abperf.styles.runningTests) {
        var test = abperf.styles.runningTests[key];
        if (test !== null && test.id === id) {
            return test;
        }
    }
    return null;
}

/**
 * @param {string} id
 * @return {abperf.styles.Test}
 */
abperf.styles.findTestByID = function(id) {
    for (var i = 0; i < abperf.styles.tests.length; i++) {
        var test = abperf.styles.tests[i];
        if (test.id === id) {
            return test;
        }
    }
    return null;
}
