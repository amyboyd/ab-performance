goog.provide('abperf.tracking');

goog.require('abperf.constants');
goog.require('goog.net.XhrIo');
goog.require('goog.string');
goog.require('goog.uri.utils');

/** @private @const */
var START_URL = abperf.constants.SERVER_URL + 'beta/tracking/start';

/** @private @const */
var PING_URL = abperf.constants.SERVER_URL + 'beta/tracking/ping';

/** @private @const */
var START_TIME = Date.now();

/** @private @type {number} */
var lastInteraction = Date.now();

/** @private @type {string} */
var lastPingStatus = 'active';

/** @private @type {number} */
var consecutiveActivePings = 0;

/** @private @type {string} Comma-seperated IDs. */
var cssToSupply = '';

/**
 * Start tracking interaction with the page.
 *
 * @param {object<string, Test>} installedTests
 */
abperf.tracking.start = function(installedTests) {
    var data = {
        'guid': START_TIME,
        'time': START_TIME,
        'url': window.location.toString()
    };
    for (var testName in installedTests) {
        data['tests[' + testName + ']'] = (installedTests[testName] != null ? installedTests[testName].id : 'none');
    }
    sendDataToURL(START_URL, data,
        function(evt) {
            var status = evt.target.getStatus();
            if (status === 200) {
                // Everything is OK.
                // The response text is the comma-seperated IDs of any CSS that needs to be
                // supplied to the server. These are sent to the sever in the next ping.
                cssToSupply = evt.target.getResponseText();

                setTimeout(abperf.tracking.ping, 5000);

                goog.events.listen(
                    window,
                    [goog.events.EventType.MOUSEMOVE, goog.events.EventType.SCROLL, goog.events.EventType.KEYPRESS],
                    abperf.tracking.interactionOccurred);
            } else if (status === 402 || status === 429) {
                // Domain is not registered or page view limit has been exceeded.
                if (typeof console !== 'undefined') {
                    console.log('AB Perf:', evt.target.getResponseText());
                }
            } else {
                if (goog.DEBUG) {
                    console.log('Unexpected status:', status);
                }
            }
        });
}

/**
 * Tell the server if the user is active on the page, or inactive (in another tab, AFK, etc).
 * This data is used to keep temporal analytics (time spent on page, etc) accurate.
 */
abperf.tracking.ping = function() {
    var now = Date.now();
    var timeSinceLastInteraction = now - lastInteraction;
    var status = (timeSinceLastInteraction > 18000) ? 'inactive' : 'active';

    if (goog.DEBUG) {
        console.log('pinging - status is ' + status + ' - last interaction was ' + (timeSinceLastInteraction/1000) + ' seconds ago');
    }

    if (lastPingStatus === 'inactive' && status === 'inactive') {
    // Don't need to ping because the server already knows the user is inactive.
    } else {
        lastPingStatus = status;
        consecutiveActivePings = (status === 'active' ? consecutiveActivePings + 1 : 0);

        var data = {
            'guid': START_TIME,
            'status': status,
            'time': now
        };

        if (!goog.string.isEmptySafe(cssToSupply)) {
            // Server does not know the CSS for at least one test ID, so tell the server what the CSS is.
            var idArray = cssToSupply.split(',');
            for (var i = 0; i < idArray.length; i++) {
                var id = idArray[i];
                data['css[' + id + ']'] = abperf.styles.findTestByID(id).css;
            }
            cssToSupply = null;
        }

        sendDataToURL(PING_URL, data);
    }

    // When the user has been active on the page for a long time, reduce the ping frequency.
    var pingFrequency = (consecutiveActivePings <= 10 ? 10000
        : (consecutiveActivePings <= 15 ? 20000
            : 30000));
    setTimeout(abperf.tracking.ping, pingFrequency);
}

/**
 * A browser event by the user shows they are still interacting with the page,
 * and not away making tea or fighting zombies. This data is used to keep
 * temporal analytics (time spent on page, etc) accurate.
 */
abperf.tracking.interactionOccurred = function() {
    lastInteraction = Date.now();
}

/**
 * @private
 * @param {string} url
 * @param {object<string, *>} data
 * @param {function} callback
 */
function sendDataToURL(url, data, callback) {
    if (typeof callback !== 'function') {
        callback = null;
    }
    goog.net.XhrIo.send(url, callback, 'POST', goog.uri.utils.buildQueryDataFromMap(data));
}
