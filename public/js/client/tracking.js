goog.provide('abperf.tracking.POST');

goog.require('goog.net.XhrIo');

/**
 * @param {string} url
 * @param {object<string, *>} data
 * @param {function} responseHandler
 */
abperf.tracking.POST = function(url, data, responseHandler) {
    goog.net.XhrIo.send(url,
        function(evt) {
            if (typeof responseHandler === 'function') {
                responseHandler(evt.target);
            }
        }, 'POST', goog.uri.utils.buildQueryDataFromMap(data));
}
