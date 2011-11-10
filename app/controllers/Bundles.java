package controllers;

import play.libs.optimization.*;
import play.mvc.Controller;

/**
 * Serves CSS styles and JavaScripts, merged and compiled to improve performance.
 */
public class Bundles extends Controller {
    private static final Bundle siteStyles = new StylesheetsBundle(
            "site.css",
            new String[] {
                "public/css/html5-reset.css",
                "public/css/main.css",
                "public/css/tags.css",
                "public/css/users.css", });

    public static void siteStyles() {
        response.cacheFor("70d");
        siteStyles.applyToResponse(request, response);
    }
}
