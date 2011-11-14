goog.provide('abperf');

goog.require('abperf.styles');
goog.require('abperf.tracking');
goog.require('goog.events');
goog.require('goog.events.EventType');
goog.require('onDOMContentLoaded');

// Start as soon as possible after all DOM elements are in the DOM tree.
onDOMContentLoaded(function() {
    abperf.styles.start();
    abperf.tracking.start(abperf.styles.runningTests);
});
