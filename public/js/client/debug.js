goog.provide('abperf.debug');

goog.require('abperf.styles');
goog.require('goog.dom');
goog.require('goog.events');
goog.require('goog.events.EventType');
goog.require('goog.style');

goog.exportSymbol('abperf.debug', abperf.debug);

/** @private @type {boolean} */
var debugHasStarted = false;

abperf.debug = function() {
    // Don't run more than once.
    if (debugHasStarted === true) {
        console.log('The debug interface is already open.');
        return;
    }
    debugHasStarted = true;
    console.log('Opening the debug interface.');

    addStyles();
    var container = addContainer();
    for (var testGroupName in abperf.styles.testGroups) {
        addTestGroup(abperf.styles.testGroups[testGroupName], container);
    }
}

/**
 * @private
 */
function addStyles() {
    goog.style.installStyles('\
#abperf-debug { font-family: sans-serif; position: fixed; bottom: 0; left: 100px; right: 100px; background-color: black; padding: 10px; color: beige; } \
#abperf-debug .group { float: left; margin-right: 10px; } \
#abperf-debug .group h6 { color: beige; font-size: 15px; font-weight: bold; margin: 0 0 0.5em; padding: 0; } \
#abperf-debug .group ol { margin: 0; padding: 0 0 0 11px; } \
#abperf-debug .group li { color: beige; font-size: 12px; cursor: pointer; margin: 0.5em 0 0.5em 1em; } \
#abperf-debug .group li:hover { color: orange; } \
#abperf-debug .group li.running { color: orange; text-decoration: underline; } \
')
}

/**
 * @private
 * @return {HTMLDivElement}
 */
function addContainer() {
    var container = goog.dom.createDom('div', {
        'id': 'abperf-debug'
    });
    goog.dom.appendChild(document.body, container);
    return container;
}

/**
 * @private
 * @param {abperf.styles.TestGroup} testGroup
 * @param {HTMLDivElement} debugContainer
 */
function addTestGroup(testGroup, debugContainer) {
    var testGroupContainer = goog.dom.createDom('div', {
        'class': 'group'
    })
    goog.dom.appendChild(debugContainer, testGroupContainer);

    var header = goog.dom.createDom('h6');
    goog.dom.setTextContent(header, testGroup.name);
    goog.dom.appendChild(testGroupContainer, header);

    var list = goog.dom.createDom('ol');
    goog.dom.appendChild(testGroupContainer, list);

    addNoTest(testGroup, list);

    for (var i = 0; i < testGroup.tests.length; i++) {
        addTest(testGroup.tests[i], list);
    }
}

/**
 * @private
 * @param {abperf.styles.TestGroup} testGroup
 * @param {HTMLListElement} list
 */
function addNoTest(testGroup, list) {
    var listItem = goog.dom.createDom('li', {
        'class': (abperf.styles.runningTests[testGroup.name] == null ? 'running' : '')
    });

    goog.dom.setTextContent(listItem, 'none');
    goog.dom.appendChild(list, listItem);

    goog.events.listen(listItem, goog.events.EventType.CLICK, changeTest);
}

/**
 * @private
 * @param {abperf.styles.Test} test
 * @param {HTMLListElement} list
 */
function addTest(test, list) {
    var listItem = goog.dom.createDom('li', {
        'class': (test.isRunning ? 'running' : '')
    });
    goog.dom.setTextContent(listItem, test.id);
    goog.dom.appendChild(list, listItem);

    goog.events.listen(listItem, goog.events.EventType.CLICK, changeTest);
}

/**
 * @private
 */
function changeTest(evt) {
    var newTestEl = evt.target;
    var newTest = abperf.styles.findTestByID(newTestEl.textContent);
    var oldTestEl = goog.dom.$$('li', 'running', newTestEl.parentNode)[0];
    var oldTest = abperf.styles.findTestByID(oldTestEl.textContent);
    var testGroupName = goog.dom.$$('h6', null, newTestEl.parentNode.parentNode)[0].textContent;

    // Uninstall the old test.
    oldTestEl.setAttribute('class', null);
    if (oldTest != null) {
        oldTest.uninstall();
    }

    // Install the new test.
    newTestEl.setAttribute('class', 'running');
    if (newTest != null) {
        newTest.install();
    }

    console.log('Changing ' + testGroupName + ' from ' + (oldTest != null ? oldTest.id : 'none') + ' to ' + (newTest != null ? newTest.id : 'none'));
}
