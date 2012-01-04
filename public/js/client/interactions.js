goog.provide('abperf.interactions');

goog.require('goog.events');
goog.require('goog.events.EventType');

abperf.interactions.init = function() {
    goog.events.listen(
        window,
        [goog.events.EventType.MOUSEMOVE, goog.events.EventType.SCROLL, goog.events.EventType.KEYPRESS],
        abperf.interactions.onInteraction);
}

/**
 * A browser event by the user shows they are still interacting with the page,
 * and not away making tea or fighting zombies. This data is used to keep
 * temporal analytics (time spent on page, etc) accurate.
 */
abperf.interactions.onInteraction = function() {
    abperf.interactions.lastDate = Date.now();
}

/** @type {number} */
abperf.interactions.lastDate = Date.now();
