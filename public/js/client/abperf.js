/**
 * @fileOverview
 *
 * The entry-point for execution. The Big Bang. Everything starts here.
 */

goog.provide('abperf');

goog.require('abperf.persistence');
goog.require('abperf.styles');
goog.require('abperf.tracking.startRequest');
goog.require('abperf.interactions');
goog.require('onDOMContentLoaded');

// Start as soon as possible after all DOM elements are in the DOM tree.
onDOMContentLoaded(function() {
    abperf.persistence.init();
    abperf.styles.init();
    abperf.tracking.startRequest(abperf.styles.installedTests);
    abperf.interactions.init();

    // These can be safely deleted to reduce memory usage.
    delete abperf.persistence;
    delete abperf.styles.init;
});
