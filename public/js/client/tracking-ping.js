goog.provide('abperf.tracking.pingRequest');
goog.provide('abperf.tracking.pingResponse');

goog.require('abperf.tracking');
goog.require('abperf.constants');

/** @private @const */
var PING_URL = abperf.constants.SERVER_URL + 'beta/tracking/ping';

/**
 * Tell the server if the user is active on the page, or inactive (in another tab, AFK, etc).
 * This data is used to keep temporal analytics (time spent on page, etc) accurate.
 */
abperf.tracking.pingRequest = function() {
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
