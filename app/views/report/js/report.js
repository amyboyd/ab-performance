var domains,
pageViews,
ready = 0,
allTestNamesAndIDs = {};

$.getJSON('public-domains.json', function(data) {
    domains = data;
    ready++;
});

$.getJSON('page-views.json', function(data) {
    $('#status').text('Doing calculations...');

    for (var i = 0; i < data.length; i++) {
        doCalculationsForPageView(data[i]);
    }

    pageViews = data;

    ready++;
});

function doCalculationsForPageView(pageView) {
    var tests = pageView['tests'];

    for (testName in tests) {
        if (allTestNamesAndIDs[testName] === undefined) {
            allTestNamesAndIDs[testName] = {};
        }
        if (allTestNamesAndIDs[testName][tests[testName]] === undefined) {
            allTestNamesAndIDs[testName][tests[testName]] = 1;
        } else {
            allTestNamesAndIDs[testName][tests[testName]] += 1;
        }
    }
}

function render() {
    for (var testName in allTestNamesAndIDs) {
        var html = '<h2>' + testName + '</h2>\
            <table>\
                <thead>\
                    <tr>\
                        <th></th>\
                        <th></th>\
                        <th></th>\
                        <th>Pages per visit</th>\
                        <th>Time per page</th>\
                        <th>Time on site</th>\
                        <th>Bounce rate</th>\
                        <th>Page views tracked</th>\
                    </tr>\
                </thead>\
                <tbody>';

        for (var testID in allTestNamesAndIDs[testName]) {
            html += '<tr>'
                + '<th>' + (testID === 'none' ? 'default page style' : testID) + '</th>';

            // Link to show CSS.
            html += '<th>';
            if (testID !== 'none') {
                html += '<a href="/test-css?id=' + testID + '" class="show-css" data-test-id="' + testID + '">CSS</a>';
            }
            html += '</th>';

            // Link to show JSON data.
            html += '<th>'
                + '<span class="fake-link show-data" data-test-name="' + testName + '" data-test-id="' + testID + '">data</span>'
                + '</th>';

            html += '<td>' + averagePageViewsPerUser(testName, testID) + '</td>\
                    <td>' + averageTimePerPage(testName, testID) + ' secs</td>\
                    <td>' + averageTimeOnSite(testName, testID) + ' secs</td>\
                    <td>' + bounceRate(testName, testID) + '%</td>\
                    <td>' + countPageViews(testName, testID) + '</td>\
                </tr>';
        }

        html += '</tbody>\
                </table>';
        $('#content')[0].innerHTML += html;
    }

    $('.show-css').click(showCSS);
    $('.show-data').click(showData);
    $('#status').text('');
}

function showCSS(evt) {
    evt.preventDefault();
    openLightbox();

    var testID = this.getAttribute('data-test-id');

    $('#lightbox-content').load('/test-css?id=' + testID);
}

function showData(evt) {
    evt.preventDefault();
    openLightbox();

    var testName = this.getAttribute('data-test-name');
    var testID = this.getAttribute('data-test-id');

    var json = '';
    for (var i = 0; i < pageViews.length; i++) {
        if (pageViews[i]['test-' + testName] === testID) {
            if (json !== '') {
                json += ',';
            }
            json += JSON.stringify(pageViews[i]);
        }
    }

    $('#lightbox-content').text('[' + json + ']');
}

function countPageViews(testName, testID) {
    return allTestNamesAndIDs[testName][testID];
}

/**
 * @return The average page views per user.
 */
function averagePageViewsPerUser(testName, testID) {
    var users = {};
    var totalPageViews = 0;

    forEachPageView(testName, testID, function(pv) {
        users[pv['user']] = null; // value doesn't matter - just need the keys to be unique.
        totalPageViews++;
    });

    var totalUsers = countObjectSize(users);

    return totalPageViews / totalUsers;
}

/**
 * @return Average time per page, in seconds.
 */
function averageTimePerPage(testName, testID) {
    var totalTimeOnPagesInSeconds = 0;

    forEachPageView(testName, testID, function(pv) {
        totalTimeOnPagesInSeconds += timeOnPage(pv);
    });

    var average = totalTimeOnPagesInSeconds / countPageViews(testName, testID);

    // Round it.
    average = average.toFixed(0);

    return average;
}

/**
 * @return Average time a user is on the site, in seconds.
 */
function averageTimeOnSite(testName, testID) {
    return averageTimePerPage(testName, testID) * averagePageViewsPerUser(testName, testID);
}

/**
 * @return The approximate length of time spent on the page, in seconds.
 */
function timeOnPage(pageView) {
    if (countObjectSize(pageView.pings) === 0) {
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

    // Convert to seconds and round it.
    timeOnPage /= 1000;
    timeOnPage = timeOnPage.toFixed(0);

    return timeOnPage;
}

/**
 * @return Percentage of users who only view a single page.
 */
function bounceRate(testName, testID) {
    var users = {};

    forEachPageView(testName, testID, function(pv) {
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

    return (usersWhoViewOnePage / (usersWhoViewOnePage + usersWhoViewMultiplePages)) * 100;
}

function forEachPageView(testName, testID, callback) {
    for (var i = 0; i < pageViews.length; i++) {
        if (pageViews[i]['test-' + testName] === testID) {
            callback(pageViews[i]);
        }
    }
}

function countObjectSize(obj) {
    // JS1.5 has __count__ but it has been deprecated so it raises a warning...
    // in other words do not use. Also __count__ only includes the fields on the
    // actual object and not in the prototype chain.
    var rv = 0;
    for (var key in obj) {
        rv++;
    }
    return rv;
}

function openLightbox() {
    document.getElementById('lightbox-white').style.display = 'block';
    document.getElementById('lightbox-fade').style.display = 'block';
}

function closeLightbox() {
    document.getElementById('lightbox-white').style.display = 'none';
    document.getElementById('lightbox-fade').style.display = 'none';
    document.getElementById('lightbox-content').innerHTML = null;
}

function waitUntilReady() {
    if (ready === 2) {
        render();
    } else {
        setTimeout(waitUntilReady, 1000);
    }
}

waitUntilReady();
