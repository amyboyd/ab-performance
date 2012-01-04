/**
 * @fileOverview
 *
 * Persists values between page views and visits. The persistence lasts for 3 hours.
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
    if (pData === null || typeof pData === 'undefined') {
        pData = {};
    }

    if (abperf.persistence.isExpired()) {
        pData = {};
    } else {
        pData['date'] = Date.now();
    }

    abperf.persistence.save();
}

/**
 * @return {string?} The Test ID is one is persisted, else null.
 */
abperf.persistence.getTestID = function(testGroupName) {
    if (pData && pData['tests'] && pData['tests'][testGroupName]) {
        return pData['tests'][testGroupName];
    }
}

/**
 * @param {string} testGroupName
 * @param {string} testID Either a test ID or "none". Must not be null.
 */
abperf.persistence.setTestID = function(testGroupName, testID) {
    if (typeof pData['tests'] === 'undefined') {
        pData['tests'] = {};
    }
    pData['tests'][testGroupName] = testID;
    abperf.persistence.save();
}

/** @private */
abperf.persistence.isExpired = function() {
    if (typeof pData['date'] !== 'number') {
        return false;
    }

    var threeHours = 60 * 60 * 1000 * 3;
    var threeHoursAgo = Date.now() - threeHours;
    return pData['date'] < threeHoursAgo;
}

/** @private */
abperf.persistence.save = function() {
    store.set(LOCAL_STORAGE_KEY, pData);
}
