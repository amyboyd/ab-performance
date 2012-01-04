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
                "public/css/site/tags.css",
                "public/css/site/index.css",
                "public/css/site/features.css",
                "public/css/site/projects.css",
                "public/css/site/auth.css", });

    private static final ClosureBundle siteScripts = new ClosureBundle(
                "site.js",
                "public/closure/closure/bin/build/closurebuilder.py",
                com.google.javascript.jscomp.CompilationLevel.ADVANCED_OPTIMIZATIONS,
                new String[] {
                    "public/closure/closure/goog",
                    "public/closure/third_party/closure",
                    "public/js/site", },
                new String[] { "projects" });

    public static void siteStyles() {
        //siteStyles.getBundleFile().delete();
        //siteStyles.getBundleFileGzip().delete();

        response.cacheFor("70d");
        siteStyles.applyToResponse(request, response);
    }

    public static void siteScripts() {
        //siteScripts.getBundleFile().delete();
        //siteScripts.getBundleFileGzip().delete();

        response.cacheFor("70d");
        siteScripts.applyToResponse(request, response);
    }
}
