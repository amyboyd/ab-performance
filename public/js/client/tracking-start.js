goog.provide('abperf.tracking.startRequest');
goog.provide('abperf.tracking.startResponse');

goog.require('abperf.tracking');
goog.require('abperf.constants');

/** @private @const */
var START_URL = abperf.constants.SERVER_URL + 'beta/tracking/start';

/**
 * Start tracking interaction with the page.
 *
 * @param {object<string, Test>} installedTests
 */
abperf.tracking.startRequest = function(installedTests) {
    var data = {
        'guid': START_TIME,
        'time': START_TIME,
        'url': window.location.toString()
    };
    for (var testName in installedTests) {
        data['tests[' + testName + ']'] = (installedTests[testName] != null ? installedTests[testName].id : 'none');
    }
    abperf.tracking.POST(START_URL, data, abperf.tracking.startResponse);
}

/**
 * @param {XMLHttpResponse} response
 */
abperf.tracking.startResponse = function(response) {
    console.log(response);

    var status = response.getStatus();
    if (status === 200) {
        // Everything is OK.
        // The response text is the comma-seperated IDs of any CSS that needs to be
        // supplied to the server. These are sent to the sever in the next ping.
        cssToSupply = response.getResponseText();

        setTimeout(abperf.tracking.ping, 5000);

        goog.events.listen(
            window,
            [goog.events.EventType.MOUSEMOVE, goog.events.EventType.SCROLL, goog.events.EventType.KEYPRESS],
            abperf.tracking.interactionOccurred);
    } else if (status === 402 || status === 429) {
        // Domain is not registered or page view limit has been exceeded.
        if (typeof console !== 'undefined') {
            console.log('AB Perf:', response.getResponseText());
        }
    } else {
        if (goog.DEBUG) {
            console.log('Unexpected status:', status);
        }
    }
}
