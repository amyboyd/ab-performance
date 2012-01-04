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
