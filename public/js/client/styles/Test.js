goog.provide('abperf.styles.Test');

goog.require('goog.string');
goog.require('goog.style');
goog.require('goog.crypt');
goog.require('goog.crypt.Md5');

/**
 * @constructor
 * @param {string} name The test name.
 * @param {string} css The css rules.
 */
abperf.styles.Test = function(name, css) {
    this.name = name;

    // Make the CSS pretty so it can be displayed in debuging.
    this.css = goog.string.trim(css);
    this.css = goog.string.normalizeWhitespace(this.css);
    this.css = goog.string.normalizeSpaces(this.css);

    // This test's ID is the pretty-print (normalized) CSS, hashed.
    var md5 = new goog.crypt.Md5();
    md5.update(this.css);
    this.id = goog.crypt.byteArrayToHex(md5.digest());
}

/**
 * Run this test.
 */
abperf.styles.Test.prototype.run = function() {
    // Install the styles inline at the end of the document's body, so they have the highest
    // priority.
    var installed = goog.style.installStyles(this.css, document.body);
    // IE doesn't allow setAttribute() on HTMLStyleElement.
    installed.className = 'ab-perf';
    installed.name = this.name;
}

/** A hash that uniquely identifies this test. @type {string} */
abperf.styles.Test.prototype.id = null;

/** Pretty-printed CSS. @type {string} */
abperf.styles.Test.prototype.css = null;

/** @type {string} */
abperf.styles.Test.prototype.name = null;
