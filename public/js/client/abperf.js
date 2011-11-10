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

    setTimeout(abperf.tracking.ping, 5000);

    goog.events.listen(window,
        [goog.events.EventType.MOUSEMOVE, goog.events.EventType.SCROLL, goog.events.EventType.KEYPRESS],
        abperf.tracking.interactionOccurred);
});
