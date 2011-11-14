goog.provide('abperf.styles.TestGroup');

goog.require('abperf.styles.RememberedTestChoice');
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
 * Randomly choose a test to run, or none. If one is chosen, run it immediately.
 */
abperf.styles.TestGroup.prototype.chooseTestAndInstallIt = function() {
    var test = this.chooseTest();
    if (test !== null) {
        test.install();
    }
    abperf.styles.runningTests[this.name] = test;
}

/**
 * Choose which test to run, or none. If this has already been chosen before for this user, the
 * same test as before is returned for a consistent design for the user. If this is the first time
 * a style is being chosen for this user, it is remembered for next time.
 *
 * @private
 * @return {abperf.styles.Test|null}
 */
abperf.styles.TestGroup.prototype.chooseTest = function() {
    // See if there is a test ID already remembered for this user/test-name combo.
    var remembered = new abperf.styles.RememberedTestChoice(this.name);

    if (remembered.isValid()) {
        if (goog.DEBUG) {
            console.log(this.name + ': running ' + remembered.id + ' (remembered)');
        }

        // Update last page view time.
        remembered.save();
        
        // This will (correctly) return null if remembered.id is 'none'.
        return goog.array.find(this.tests, function(test) {
            return (remembered.id === test.id);
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

    // Remember it.
    remembered.id = (chosen !== null ? chosen.id : null);
    remembered.save();

    if (goog.DEBUG) {
        console.log(this.name + ': running ' + remembered.id);
    }

    return chosen;
}
