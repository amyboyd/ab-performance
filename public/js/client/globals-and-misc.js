/**
 * @fileOverview
 *
 * This file contains troubled young loners with no friends they can sit with.
 */

goog.provide('abperf.globals');
goog.provide('abperf.httpPostRequest');
goog.provide('abperf.getProjectID');

goog.require('goog.net.XhrIo');

/** @const */
abperf.globals.SERVER_URL = (goog.DEBUG ? 'http://dev.abperf.com/' : 'http://abperf.com/');

/** Time the page was loaded. */
// Don't declare this as a constant, because Closure Compiler will inline Date.now()!
abperf.globals.startTime = Date.now();

/** Comma-seperated IDs of tests that the server requires the CSS for. */
abperf.globals.cssToSupply = '';

/**
 * @param {string} url
 * @param {object<string, *>} data
 * @param {function} responseHandler
 */
abperf.httpPostRequest = function(url, data, responseHandler) {
    goog.net.XhrIo.send(url,
        function(evt) {
            if (typeof responseHandler === 'function') {
                responseHandler(evt.target);
            }
        }, 'POST', goog.uri.utils.buildQueryDataFromMap(data));
}

/**
 * @return {number} The project ID.
 */
abperf.getProjectID = function() {
    if (abperf.projectID_ !== null) {
        return abperf.projectID_;
    }

    // Project ID is included in the script URL, e.g. http://abperf.com/beta/client.js?id=123
    var scripts = goog.dom.$$('script', null, document.head);
    for (var i = 0; i < scripts.length; i++) {
        var src = scripts[i].getAttribute('src');
        if (src && src.indexOf('abperf.com/') > 0 && src.indexOf('client.js') > 0 && src.indexOf('?id=') > 0) {
            // This is definitely the correct script. Get the ID and cache it.
            abperf.projectID_ = src.split('?id=')[1];
            return abperf.projectID_;
        }
    }
}

/** @private Cached value. See getProjectID(). */
abperf.projectID_ = null;
