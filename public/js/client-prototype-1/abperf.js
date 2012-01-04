goog.provide('abperf');

goog.require('abperf.styles');
goog.require('abperf.tracking');
    goog.require('goog.dom');
goog.require('goog.events');
goog.require('goog.events.EventType');
goog.require('onDOMContentLoaded');

// Start as soon as possible after all DOM elements are in the DOM tree.
onDOMContentLoaded(function() {
    abperf.styles.start();
    abperf.tracking.start(abperf.styles.installedTests);
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
