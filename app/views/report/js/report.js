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
                        <th>Pages per visit</th>\
                        <th>Time per page</th>\
                        <th>Time on site</th>\
                        <th>Bounce rate</th>\
                        <th>Page views tracked</th>\
                    </tr>\
                </thead>\
                <tbody>';

        for (var testID in allTestNamesAndIDs[testName]) {
            html += '<tr>';

            if (testID === 'none') {
                html += '<th>default page style</th> <th></th>';
            } else {
                html += '<th>' + testID + '</th> <th><a href="/test-css?id=' + testID + '" class="show-css" data-test-id="' + testID + '">show CSS</a></th>';
            }

            html += '<td>' + averagePageViewsPerUser(testName, testID) + '</td>\
                    <td>' + averageTimePerPage(testName, testID) + ' secs</td>\
                    <td>' + averageTimeOnSite(testName, testID) + ' secs</td>\
                    <td>' + bounceRate(testName, testID) + '%</td>\
                    <td>' + allTestNamesAndIDs[testName][testID] + '</td>\
                </tr>';
        }

        html += '</tbody>\
                </table>';
        $('#content')[0].innerHTML += html;
    }

    $('#status').text('');

    $('.show-css').click(function(evt) {
        evt.preventDefault();
        openLightbox();
        $('#lightbox-content').load(this.getAttribute('href'));
    });
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
    forEachPageView(testName, testID, function(pv) {
        // @todo
        });

    return Math.random() * 100;
}

/**
 * @return Average time a user is on the site, in seconds.
 */
function averageTimeOnSite(testName, testID) {
    forEachPageView(testName, testID, function(pv) {
        // @todo
        });

    return Math.random() * 100;
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
}

function waitUntilReady() {
    if (ready === 2) {
        render();
    } else {
        setTimeout(waitUntilReady, 1000);
    }
}

waitUntilReady();
