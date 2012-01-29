/**
 * @fileOverview
 *
 * Start page view tracking.
 */

goog.provide('abperf.tracking.startRequest');

goog.require('abperf.getProjectID');
goog.require('abperf.globals');
goog.require('abperf.httpPostRequest');
goog.require('abperf.persistence');
goog.require('abperf.tracking.pingRequest');

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
        'user': abperf.persistence.getUserID(),
        'proj': abperf.getProjectID()
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
    var text = response.getResponseText();

    if (status === 200) {
        var parts = text.split('\n');
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

        abperf.interactions.init();
    } else if (status === 403 && console !== undefined) {
        // Something (not an error) is preventing tracking. That could be lack of payment,
        // being on an unregistered or private domain, etc.
        console.log('AB Perf:', text);
        abperf.persistence.clear();
    } else if (console !== undefined) {
        if (status === 0) {
            console.log('AB Perf: unexpected status 0. Maybe compiled DEV instead of PROD or vice versa.');
        } else {
            console.log('AB Perf: unexpected status', status, text);
        }
    }
}
