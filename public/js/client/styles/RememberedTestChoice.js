goog.provide('abperf.styles.RememberedTestChoice');

goog.require('store');

/**
 * @constructor
 */
abperf.styles.RememberedTestChoice = function(testName) {
    this.testName = testName;

    var obj = store.get('test-' + testName);
    if (obj != null) {
        this.id = obj.id;
        this.date = obj.date;
    } else {
        this.id = null;
        this.date = 0;
    }
}

/**
 * @return {boolean} True is the choice hasn't been used in over one hour.
 */
abperf.styles.RememberedTestChoice.prototype.isValid = function() {
    return this.date >= (Date.now() - 3600000) && this.id !== null;
}

/**
 * Updates the date and saves to storage.
 */
abperf.styles.RememberedTestChoice.prototype.save = function() {
    this.id = (this.id === null ? 'none' : this.id);
    this.date = Date.now();

    store.set('test-' + this.testName, {
        id: this.id, 
        date: this.date
    });
}
