/**
 * @fileOverview
 *
 * Start page view tracking.
 */

goog.provide('abperf.tracking.startRequest');

goog.require('abperf.globals');
goog.require('abperf.httpPostRequest');
goog.require('abperf.tracking.pingRequest');
goog.require('abperf.persistence');

/** @private @const */
var START_URL = abperf.globals.SERVER_URL + 'beta/tracking/start';

/**
 * Start tracking the page view.
 *
 * @param {object<string, Test>} installedTests
 */
abperf.tracking.startRequest = function(installedTests) {
    var data = {
        'time': abperf.globals.startTime,
        'url': window.location.toString(),
        'user': abperf.persistence.getUserID()
    };
    for (var testName in installedTests) {
        data['tests[' + testName + ']'] = (installedTests[testName] != null ? installedTests[testName].id : 'none');
    }
    abperf.httpPostRequest(START_URL, data, abperf.tracking.startResponse);
}

/**
 * @private
 * @param {goog.net.XhrIo} response
 */
abperf.tracking.startResponse = function(response) {
    var status = response.getStatus();
    if (status === 200) {
        var parts = response.getResponseText().split('\n');
        for (var i = 0; i < parts.length; i++) {
            var key = parts[i].split("=")[0],
            value = parts[i].split("=")[1];

            if (key === 'user') {
                abperf.persistence.setUserID(value);
            } else if (key === 'css') {
                // Comma-seperated IDs of any CSS that needs to be supplied to the server.
                // These are sent to the sever in the next ping.
                abperf.globals.cssToSupply = value;
            } else if (key === 'pv') {
                abperf.globals.pageViewID = value;
            }
        }

        setTimeout(abperf.tracking.pingRequest, 5000);
    } else if (status === 402 || status === 429) {
        // Domain is not registered or page view limit has been exceeded.
        if (typeof console !== 'undefined') {
            console.log('AB Perf:', response.getResponseText());
        }
    } else if (goog.DEBUG) {
        console.log('Unexpected status:', status);
    }
}
