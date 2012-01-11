var ui = {
    render: function() {
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
                    html += '<span class="fake-link show-css" data-test-id="' + testID + '">CSS</span>';
                }
                html += '</th>';

                // Link to show JSON data.
                html += '<th>'
                    + '<span class="fake-link show-data" data-test-name="' + testName + '" data-test-id="' + testID + '">data</span>'
                    + '</th>';

                $('#status').text('Calculating... ' + testName);

                var calculator = new TestStatCalculator(testName, testID);

                html += '<td>' + calculator.getAveragePageViewsPerUser() + '</td>\
                        <td>' + calculator.getAverageTimePerPage() + ' secs</td>\
                        <td>' + calculator.getAverageTimeOnSite() + ' secs</td>\
                        <td>' + calculator.getBounceRate() + '%</td>\
                        <td>' + calculator.getTotalPageViews() + '</td>\
                    </tr>';
            }

            html += '</tbody>\
                    </table>';
            $('#content')[0].innerHTML += html;
        }

        $('.show-css').click(ui.showCSS);
        $('.show-data').click(ui.showData);
        $('#status').text('');
    },

    showCSS: function(evt) {
        evt.preventDefault();
        ui.openLightbox();

        var testID = this.getAttribute('data-test-id');

        $.get('/reports/getcss?testID=' + testID, function(css) {
            $('#lightbox-content').html('<pre>' + css + '</pre>');
        });
    },

    showData: function(evt) {
        evt.preventDefault();
        ui.openLightbox();

        var testName = this.getAttribute('data-test-name');
        var testID = this.getAttribute('data-test-id');

        var json = '';
        for (var i = 0; i < pageViews.length; i++) {
            if (pageViews[i]['tests'][testName] === testID) {
                if (json !== '') {
                    json += ',';
                }
                json += JSON.stringify(pageViews[i]);
            }
        }

        $('#lightbox-content').text('[' + json + ']');
    },

    openLightbox: function() {
        document.getElementById('lightbox-white').style.display = 'block';
        document.getElementById('lightbox-fade').style.display = 'block';
    },

    closeLightbox: function () {
        document.getElementById('lightbox-white').style.display = 'none';
        document.getElementById('lightbox-fade').style.display = 'none';
        document.getElementById('lightbox-content').innerHTML = null;
    }
};
