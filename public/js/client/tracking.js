goog.provide('abperf.tracking');

goog.require('abperf.constants');
goog.require('goog.net.XhrIo');
goog.require('goog.string');
goog.require('goog.uri.utils');

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
 * @param {function} responseHandler
 */
abperf.tracking.POST = function(url, data, responseHandler) {
    goog.net.XhrIo.send(url,
        function(evt) {
            if (typeof responseHandler === 'function') {
                responseHandler(evt);
            }
        }, 'POST', goog.uri.utils.buildQueryDataFromMap(data));
}
