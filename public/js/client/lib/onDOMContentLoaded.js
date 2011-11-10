goog.provide('onDOMContentLoaded');

/** @private @type {boolean} */
var onDOMContentLoadedIsFired = false;

onDOMContentLoaded = function(callback) {
    var onlyRunsOnce = function(evt) {
        // Allow this function to run only once. Happens if browser supports both
        // 'DOMContentLoaded' and 'load' events.
        if (onDOMContentLoadedIsFired) {
            return;
        }
        onDOMContentLoadedIsFired = true;

        callback();
    }

    addEventListener('load', onlyRunsOnce, false);
    addEventListener('DOMContentLoaded', onlyRunsOnce, false);
}
