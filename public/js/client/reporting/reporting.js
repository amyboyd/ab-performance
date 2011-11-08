goog.provide('abperf.reporting');

goog.require('abperf.constants');
goog.require('goog.events');
goog.require('goog.events.EventType');
goog.require('goog.net.XhrIo');
goog.require('goog.uri.utils');

/** @private @const */
var START_URL = abperf.constants.SERVER_URL + 'beta/report/start';

/** @private @const */
var PING_URL = abperf.constants.SERVER_URL + 'beta/report/ping';

/** @private @const */
var START_TIME = Date.now();

/** @private @type {number} */
var lastInteraction = Date.now();

/** @private @type {string} */
var lastPingStatus = 'active';

/** @private @type {number} */
var consecutiveActivePings = 0;

/**
 * Start tracking interaction with the page.
 * 
 * @param {object<string, Test>} runningTests
 */
abperf.reporting.start = function(runningTests) {
    var data = {
        guid: START_TIME,
        time: START_TIME,
        pageURL: window.location.toString()
    };
    for (var testName in runningTests) {
        data['tests[' + testName + ']'] = (runningTests[testName] != null ? runningTests[testName].id : 'none');
    }

    abperf.reporting.reportDataToURL_(START_URL, data);
}

/**
 * Tell the server if the user is active on the page, or inactive (in another tab, AFK, etc).
 * This data is used to keep temporal analytics (time spent on page, etc) accurate.
 */
abperf.reporting.ping = function() {
    var now = Date.now();
    var timeSinceLastInteraction = now - lastInteraction;
    var status = (timeSinceLastInteraction > 18000) ? 'inactive' : 'active';

    if (goog.DEBUG) {
        console.log('pinging - status is ' + status + ' - last interaction was ' + (timeSinceLastInteraction/1000) + ' seconds ago');
    }

    if (lastPingStatus === 'inactive' && status === 'inactive') {
        // No need to ping because the server already knows the user is inactive.
    } else {
        lastPingStatus = status;
        consecutiveActivePings = (status === 'active' ? consecutiveActivePings + 1 : 0);

        abperf.reporting.reportDataToURL_(PING_URL, {
            guid: START_TIME,
            status: status,
            time: now
        });
    }

    // When the user has been active on the page for a long time, reduce the ping frequency.
    var pingFrequency = (consecutiveActivePings <= 10 ? 10000
        : (consecutiveActivePings <= 15 ? 20000
        : 30000));
    setTimeout(abperf.reporting.ping, pingFrequency);
}

/**
 * A browser event by the user shows they are still interacting with the page,
 * and not away making tea or fighting zombies. This data is used to keep
 * temporal analytics (time spent on page, etc) accurate.
 */
abperf.reporting.interactionOccurred = function() {
    lastInteraction = Date.now();
}

/**
 * @private
 */
abperf.reporting.reportDataToURL_ = function(url, data) {
    url = url + '?' + goog.uri.utils.buildQueryDataFromMap(data);
    goog.net.XhrIo.send(url, null, 'HEAD');
}
