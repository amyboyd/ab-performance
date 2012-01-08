/**
 * @fileOverview
 *
 * Persists values between page views and visits. The persistence lasts for 3 hours.
 *
 * Uses local storage (see store.js). Be careful to not erase any local storage set by other
 * websites/applications!
 */

goog.provide('abperf.persistence');

goog.require('abperf.getProjectID');
goog.require('store');

/** @const @private */
var LOCAL_STORAGE_KEY = 'abperf-' + abperf.getProjectID();

/**
 * Members are:
 * date - milliseconds since Epoch.
 * tests - object<testGroupName, testID>
 *
 * @private
 * @type {object}
 */
var pData = store.get(LOCAL_STORAGE_KEY);

abperf.persistence.init = function() {
    if (abperf.persistence.isExpired()) {
        pData = {};
    }

    pData['date'] = Date.now();
    abperf.persistence.save();
}

/**
 * @return {string?} The Test ID if one is persisted, else null.
 */
abperf.persistence.getTestID = function(testGroupName) {
    if (pData && pData['tests'] && pData['tests'][testGroupName]) {
        return pData['tests'][testGroupName];
    }
}

/**
 * @param {string} testGroupName
 * @param {string} testID Either a Test ID or "none". Must not be null.
 */
abperf.persistence.setTestID = function(testGroupName, testID) {
    if (typeof pData['tests'] === 'undefined') {
        pData['tests'] = {};
    }
    pData['tests'][testGroupName] = testID;
    abperf.persistence.save();
}

/**
 * @return {string?} The user ID if one is persisted, else null.
 */
abperf.persistence.getUserID = function() {
    if (pData && pData['user']) {
        return pData['user'];
    }
}

/**
 * @param {string} userID Must not be null.
 */
abperf.persistence.setUserID = function(userID) {
    pData['user'] = userID;
    abperf.persistence.save();
}

/** @private */
abperf.persistence.isExpired = function() {
    if (pData === null || pData === undefined || pData['date'] === undefined) {
        return true;
    }

    var threeHours = 60 * 60 * 1000 * 3;
    var threeHoursAgo = Date.now() - threeHours;
    return pData['date'] < threeHoursAgo;
}

/** @private */
abperf.persistence.save = function() {
    store.set(LOCAL_STORAGE_KEY, pData);
}
