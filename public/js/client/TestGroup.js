goog.provide('abperf.styles.TestGroup');

goog.require('abperf.persistence');
goog.require('goog.array');

/**
 * Groups tests with the same name.
 *
 * @constructor
 * @param {string} name Must not be null or empty.
 */
abperf.styles.TestGroup = function(name) {
    this.name = name;
    this.tests = [];
}

/**
 * @param {abperf.styles.Test} test
 */
abperf.styles.TestGroup.prototype.addTest = function(test) {
    this.tests.push(test);
}

/**
 * Randomly choose a test to install, or none. If one is chosen, install it immediately.
 */
abperf.styles.TestGroup.prototype.chooseTestAndInstallIt = function() {
    var test = this.chooseTest();
    if (test !== null) {
        test.install();
    }
    abperf.styles.installedTests[this.name] = test;
}

/**
 * Choose which test to install, or none. If this has already been chosen before for this user, the
 * same test as before is returned for a consistent design for the user. If this is the first time
 * a style is being chosen for this user, it is remembered for next time.
 *
 * @private
 * @return {abperf.styles.Test|null}
 */
abperf.styles.TestGroup.prototype.chooseTest = function() {
    // See if there is a test ID already remembered for this user/test-name combo.
    var persistedID = abperf.persistence.getTestID(this.name);

    if (persistedID) {
        // persistedID is an ID or "none". It is not null.
        if (goog.DEBUG) {
            console.log(this.name + ': chose ' + persistedID + ' (from persistence)');
        }

        // Return the Test with the remembered ID, or null if the ID is "none".
        return goog.array.find(this.tests, function(test) {
            return (persistedID === test.id);
        });
    }

    // Choose a test randomly.
    var chosen;
    if (this.tests.length == 0) {
        chosen = null;
    } else if (this.tests.length == 1) {
        chosen = (Math.random() < 0.5 ? null : this.tests[0]);
    } else {
        var testsClone = goog.array.clone(this.tests);
        testsClone.push(null);
        goog.array.shuffle(testsClone, Math.random);
        chosen = testsClone[0];
    }

    abperf.persistence.setTestID(this.name, chosen === null ? 'none' : chosen.id);

    if (goog.DEBUG) {
        console.log(this.name + ': chose ' + (chosen === null ? 'none' : chosen.id));
    }

    return chosen;
}
