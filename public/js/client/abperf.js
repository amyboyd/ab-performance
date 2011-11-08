goog.provide('abperf');

goog.require('abperf.styles');
goog.require('abperf.reporting');
goog.require('goog.events');
goog.require('goog.events.EventType');
goog.require('onDOMContentLoaded');

// Start as soon as possible after all DOM elements are in the DOM tree.
onDOMContentLoaded(function() {
    // Styles.
    abperf.styles.start();

    // Reporting.
    abperf.reporting.start(abperf.styles.runningTests);
    setInterval(abperf.reporting.ping, 10000);
    goog.events.listen(window,
        [goog.events.EventType.MOUSEMOVE, goog.events.EventType.SCROLL, goog.events.EventType.KEYPRESS],
        abperf.reporting.interactionOccurred);
});
