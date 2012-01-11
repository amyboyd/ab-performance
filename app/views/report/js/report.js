var pageViews;
var ready = 0;
var FULLY_READY_NUMBER = 1; // change if more steps are added to the startup process.
var allTestNamesAndIDs = {};

$.getJSON('page-views.json', function(data) {
    $('#status').text('Doing calculations...');

    for (var i = 0; i < data.length; i++) {
        // Create a structure of { Test Name: { Test ID: count } }
        var tests = data[i]['tests'];

        for (var testName in tests) {
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

    pageViews = data;

    ready++;
});

function waitUntilReady() {
    if (ready === FULLY_READY_NUMBER) {
        ui.render();
    } else {
        setTimeout(waitUntilReady, 200);
    }
}

waitUntilReady();

// Domain data is not used at the moment.
//var domains;
//$.getJSON('public-domains.json', function(data) {
//    domains = data;
//    ready++;
//});
