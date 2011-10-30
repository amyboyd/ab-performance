goog.provide('onDOMContentLoaded');

onDOMContentLoaded = function(callback) {
    var onlyRunsOnce = function(evt) {
        // Allow this function to run only once. Happens if browser supports both
        // 'DOMContentLoaded' and 'load' events.
        if (this.fired) {
            return;
        }
        this.fired = true;

        callback();
    }

    addEventListener('load', onlyRunsOnce, false);
    addEventListener('DOMContentLoaded', onlyRunsOnce, false);
}
