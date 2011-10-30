/*
From the constructor of Test:

    // Create a TestRulesRule for each selector/styles combo.
    this.rules = [];
    var rules = css.split('}');
    for (var i = 0; i < rules.length; i++) {
        var rule = rules[i];

        if (goog.string.isEmpty(rule)) {
            continue;
        }

        var selector = rule.substring(0, rule.indexOf('{'));
        selector = goog.string.trim(selector);

        var styles = rule.substring(rule.indexOf('{') + 1, rule.length);
        styles = goog.style.parseStyleAttribute(styles);
        // `styles` is now an object. Keys are the style names. Values are the style values.

        this.rules.push(new abperf.TestRule(selector, styles));
    }

*/

///**
// * @constructor
// * @param {string} selector
// * @param {object<string, string>} styles
// */
//abperf.TestRule = function(selector, styles) {
//    this.selector = selector;
//    this.styles = styles;
//}
//
///** @type {string} */
//abperf.TestRule.prototype.selector = null;
//
///** @type {object<string, string>} */
//abperf.TestRule.prototype.styles = null;
