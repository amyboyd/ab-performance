/** @constructor */
function TestStatCalculator(testName, testID) {
    this.testName = testName;
    this.testID = testID;

    this.calculateTotalPageViews();
    this.calculateAveragePageViewsPerUser();
    this.calculateAverateTimePerPage();
    this.calculateAverateTimeOnSite();
    this.calculateBounceRate();
}


// Getters /////////////////////////////////////////////////////

TestStatCalculator.prototype.getAveragePageViewsPerUser = function() {
    return this.averagePageViewsPerUser;
}

TestStatCalculator.prototype.getAverageTimePerPage = function() {
    return this.averageTimePerPage;
}

TestStatCalculator.prototype.getAverageTimeOnSite = function() {
    return this.averageTimeOnSite;
}

TestStatCalculator.prototype.getBounceRate = function() {
    return this.bounceRate;
}

TestStatCalculator.prototype.getTotalPageViews = function() {
    return this.totalPageViews;
}


// Calculators /////////////////////////////////////////////////

/** @private */
TestStatCalculator.prototype.calculateTotalPageViews = function() {
    this.totalPageViews = allTestNamesAndIDs[this.testName][this.testID];
}

/** @private */
TestStatCalculator.prototype.calculateAveragePageViewsPerUser = function() {
    var users = {};
    var totalPageViews = 0;

    this.forEachTest(function(pv) {
        users[pv['user']] = null; // value doesn't matter - just need the keys to be unique.
        totalPageViews++;
    });

    var totalUsers = TestStatCalculator.countObjectSize(users);
    var averagePageViewsPerUser = totalPageViews / totalUsers;
    this.averagePageViewsPerUser = TestStatCalculator.round(averagePageViewsPerUser);
}

/** @private */
TestStatCalculator.prototype.calculateAverateTimePerPage = function() {
    var totalTimeOnPagesInSeconds = 0;

    this.forEachTest(function(pv) {
        totalTimeOnPagesInSeconds += TestStatCalculator.timeOnPageView(pv);
    });

    this.averageTimePerPage = totalTimeOnPagesInSeconds / this.totalPageViews;
    this.averageTimePerPage = TestStatCalculator.round(this.averageTimePerPage);
}

/** @private */
TestStatCalculator.prototype.calculateAverateTimeOnSite = function() {
    this.averageTimeOnSite = TestStatCalculator.round(this.averageTimePerPage * this.averagePageViewsPerUser);
}

/** @private */
TestStatCalculator.prototype.calculateBounceRate = function() {
    var users = {};

    this.forEachTest(function(pv) {
        if (users[pv['user']] === undefined) {
            users[pv['user']] = 0;
        }
        users[pv['user']] += 1;
    });

    var usersWhoViewOnePage = 0;
    var usersWhoViewMultiplePages = 0;

    for (var user in users) {
        if (users[user] === 1) {
            usersWhoViewOnePage++;
        } else {
            usersWhoViewMultiplePages++;
        }
    }

    this.bounceRate = (usersWhoViewOnePage / (usersWhoViewOnePage + usersWhoViewMultiplePages)) * 100;
    this.bounceRate = TestStatCalculator.round(this.bounceRate);
}


// Misc ////////////////////////////////////////////////////////

/** @private */
TestStatCalculator.prototype.forEachTest = function(callback) {
    for (var i = 0; i < pageViews.length; i++) {
        if (pageViews[i]['tests'][this.testName] === this.testID) {
            callback(pageViews[i]);
        }
    }
}

/** @private */
TestStatCalculator.timeOnPageView = function(pageView) {
    if (TestStatCalculator.countObjectSize(pageView.pings) === 0) {
        return 10;
    }

    var lastTime = pageView.time; // the start time.
    var lastStatus = 'active';

    var timeOnPage = 0;

    for (var time in pageView.pings) {
        var status = pageView.pings[time];

        if (lastStatus === 'active' && status === 'active') {
            timeOnPage += (time - lastTime);
        } else if (lastStatus === 'active' && status === 'inactive') {
            timeOnPage += ((time - lastTime) / 2);
        }

        lastTime = time;
        lastStatus = status;
    }

    // Convert to seconds.
    timeOnPage /= 1000;

    return TestStatCalculator.round(timeOnPage);
}

/** @private */
TestStatCalculator.countObjectSize = function(obj) {
    // JS1.5 has __count__ but it has been deprecated so it raises a warning...
    // in other words do not use. Also __count__ only includes the fields on the
    // actual object and not in the prototype chain.
    var rv = 0;
    for (var key in obj) {
        rv++;
    }
    return rv;
}

/** @private */
TestStatCalculator.round = function(number) {
    return Math.round(Number(number));
}
