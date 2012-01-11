goog.provide("projects");

goog.require("goog.dom");
goog.require("goog.ui.ProgressBar");
goog.require("goog.ui.ProgressBar.Orientation");

goog.exportSymbol("projects.overviewPage", projects.overviewPage);

projects.overviewPage = function() {
    var progressBarElements = goog.dom.$$('div', 'page-views');
    for (var i = 0; i < progressBarElements.length; i++) {
        var el = progressBarElements[i];
        var pb = new goog.ui.ProgressBar();
        pb.setOrientation(goog.ui.ProgressBar.Orientation.HORIZONTAL);
        pb.setValue(el.getAttribute('data-value'));
        pb.setMinimum(el.getAttribute('data-min'));
        pb.setMaximum(el.getAttribute('data-max'));
        pb.decorate(el);
    }

    var showHTMLlinks = goog.dom.$$('a', 'show-html');
    for (i = 0; i < showHTMLlinks.length; i++) {
        var link = showHTMLlinks[i];
        goog.events.listen(link, goog.events.EventType.CLICK, function(evt) {
            evt.preventDefault();
            var projectID = evt.target.getAttribute('data-project-id');
            goog.dom.$('embed-html-' + projectID).style.display = 'block';
        });
    }
}
