goog.provide('abperf');

goog.require('abperf.persistence');
goog.require('abperf.styles');
goog.require('abperf.tracking.startRequest');
goog.require('goog.dom');
goog.require('goog.net.XhrIo');
goog.require('goog.uri.utils');
goog.require('onDOMContentLoaded');

// Start as soon as possible after all DOM elements are in the DOM tree.
onDOMContentLoaded(function() {
    abperf.persistence.init();
    abperf.styles.init();
    abperf.tracking.startRequest(abperf.styles.installedTests);
    abperf.interactions.init();
});

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
