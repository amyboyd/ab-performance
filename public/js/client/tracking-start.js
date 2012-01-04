goog.provide('abperf.tracking.startRequest');
goog.provide('abperf.tracking.startResponse');

goog.require('abperf.tracking.POST');
goog.require('abperf.constants');
goog.require('abperf.tracking.pingRequest');

/** @private @const */
var START_URL = abperf.constants.SERVER_URL + 'beta/tracking/start';

/**
 * Start tracking the page view.
 *
 * @param {object<string, Test>} installedTests
 */
abperf.tracking.startRequest = function(installedTests) {
    var data = {
        'guid': abperf.constants.START_TIME,
        'time': abperf.constants.START_TIME,
        'url': window.location.toString()
    };
    for (var testName in installedTests) {
        data['tests[' + testName + ']'] = (installedTests[testName] != null ? installedTests[testName].id : 'none');
    }
    abperf.tracking.POST(START_URL, data, abperf.tracking.startResponse);
}

/**
 * @param {goog.net.XhrIo} response
 */
abperf.tracking.startResponse = function(response) {
    var status = response.getStatus();
    if (status === 200) {
        // Everything is OK.
        // The response text is the comma-seperated IDs of any CSS that needs to be
        // supplied to the server. These are sent to the sever in the next ping.
        abperf.constants.cssToSupply = response.getResponseText();

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
