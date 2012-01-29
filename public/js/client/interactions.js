/**
 * @fileOverview
 *
 * Events caused by the user shows they are still interacting with the page,
 * and not away making tea or in another tab. This data is used to keep
 * temporal analytics (time spent on page, etc) accurate.
 */

goog.provide('abperf.interactions');

goog.require('goog.events');
goog.require('goog.events.EventType');

abperf.interactions.init = function() {
    goog.events.listen(
        window,
        [goog.events.EventType.MOUSEMOVE, goog.events.EventType.SCROLL, goog.events.EventType.KEYPRESS],
        abperf.interactions.onInteraction);

    setTimeout(abperf.tracking.pingRequest, 5000);
}

/**
 * Mouse-move/scroll/key-press handler.
 *
 * @private
 */
abperf.interactions.onInteraction = function() {
    abperf.interactions.lastDate = Date.now();
}

/** @type {number} */
abperf.interactions.lastDate = Date.now();
