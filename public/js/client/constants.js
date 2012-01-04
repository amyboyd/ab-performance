goog.provide('abperf.constants');

/** @const */
abperf.constants.SERVER_URL = (goog.DEBUG ? 'http://dev.abperf.com/' : 'http://abperf.com/');

/** Time the page was loaded. */
abperf.constants.START_TIME = Date.now();

/** @type {string} Comma-seperated IDs of tests that the server requires the CSS for. */
abperf.constants.cssToSupply = '';
