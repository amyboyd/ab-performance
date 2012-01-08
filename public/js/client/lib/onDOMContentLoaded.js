/**
 * @fileOverview
 *
 * A function to add a DOMContentLoaded event listener.
 */

goog.provide('onDOMContentLoaded');

onDOMContentLoaded = function(callback) {
    function onlyRunsOnce() {
        // This function will be called twice if the browser supports both 'DOMContentLoaded'
        // and 'load' events. Stop it executing the second time.
        if (onDOMContentLoaded.fired) {
            return;
        }
        onDOMContentLoaded.fired = true;

        callback();
    }

    // It is impossible to detect if a browser supports the 'DOMContentLoaded' event, so we must
    // set that and 'load'.
    addEventListener('DOMContentLoaded', onlyRunsOnce, false);
    addEventListener('load', onlyRunsOnce, false);
}
