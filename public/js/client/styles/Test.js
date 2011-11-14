goog.provide('abperf.styles.Test');

goog.require('goog.dom');
goog.require('goog.string');
goog.require('goog.style');

/**
 * @constructor
 * @param {string} testGroupName The TestGroup name.
 * @param {string} css The CSS rules.
 */
abperf.styles.Test = function(testGroupName, css) {
    this.testGroupName = testGroupName;
    this.css = formatCSS(css);
    // This test's ID is the pretty-print (normalized) CSS, hashed.
    this.id = hash(this.css);
    this.isInstalled = false;
}

abperf.styles.Test.prototype.install = function() {
    // Install the styles inline so they have high priority.
    // Line breaks must be removed because Chrome changes them to <br/> (a bug?).
    var installed = goog.style.installStyles(this.css.replace('\n', ' '));
    // IE doesn't allow setAttribute() on HTMLStyleElement, so set these directly.
    installed.className = 'abperf abperf-' + this.testGroupName.replace(' ', '-');
    installed.id = 'abperf-' + this.id;

    this.isInstalled = true;
}

abperf.styles.Test.prototype.uninstall = function() {
    var installed = goog.dom.$('abperf-' + this.id);
    if (installed != null) {
        goog.dom.removeNode(installed);
    }

    this.isInstalled = false;
}

/** A hash that uniquely identifies this test. @type {string} */
abperf.styles.Test.prototype.id = null;

/** Pretty-printed CSS. @type {string} */
abperf.styles.Test.prototype.css = null;

/** @type {string} */
abperf.styles.Test.prototype.testGroupName = null;

/**
 * Make the CSS pretty so it can be displayed nicely in debuging.
 *
 * @private
 */
function formatCSS(css) {
    css = goog.string.trim(css);
    css = goog.string.normalizeWhitespace(css);
    css = goog.string.normalizeSpaces(css);
    css = css.replace(/\} /, '}\n');
    return css;
}

goog.require('goog.crypt');
goog.require('goog.crypt.Sha1');

function hash(input) {
    var hash = new goog.crypt.Sha1();
    hash.update(input);
    return goog.crypt.byteArrayToHex(hash.digest());
}
