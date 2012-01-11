package controllers;

import com.alienmegacorp.bundles.*;
import com.google.javascript.jscomp.CompilationLevel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import play.Play;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Util;
import play.templates.*;

/**
 * Serves CSS styles and JavaScripts, merged and compiled to improve performance.
 */
public class Bundles extends Controller {
    private static final StylesheetsBundle siteStyles = createSiteStylesBundle();

    private static final ClosureBundle siteScripts = createSiteScriptsBundle();

    @Util
    private static ClosureBundle createSiteScriptsBundle() {
        final ClosureBundle bundle = new ClosureBundle(
                Play.getFile("public/bundles/site.js"),
                Play.getFile("public/js/site"));
        bundle.setClosureLibraryDir(Play.getFile("public/closure"));
        bundle.setCopyrightNotice("Copyright 2012 A/B Performance");
        bundle.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
        bundle.setEntryNamespaces(new String[] { "projects" });
        bundle.setDefineToBooleanLiteral("goog.DEBUG", Play.mode.isDev());
        ClosureBundle.setPythonExecutable(Play.configuration.getProperty("python"));

        return bundle;
    }

    @Util
    private static StylesheetsBundle createSiteStylesBundle() {
        final StylesheetsBundle bundle = new StylesheetsBundle(
                Play.getFile("public/bundles/site.css"),
                Play.getFile("public/css/html5-reset.css"),
                Play.getFile("public/css/main.css"),
                Play.getFile("public/css/site"));
        bundle.setCopyrightNotice("Copyright 2012 A/B Performance");
        bundle.setSourcePreProcessor(new FileProcessor() {
            @Override
            public String execute(final String content) {
                return TemplateLoader.loadString(content).render();
            }
        });

        return bundle;
    }

    public static void siteStyles() {
        response.cacheFor("70d");
        sendBundle(siteStyles);
    }

    public static void siteScripts() {
        response.cacheFor("70d");
        sendBundle(siteScripts);
    }

    /**
     * If the browser supports GZIP, return the GZIP file. If not supported, return the plain-text file.
     */
    @Util
    static void sendBundle(final Bundle bundle) {
        if (!bundle.getOutputFile().exists() || !bundle.getOutputFileGzip().exists()) {
            bundle.compile();
        }

        response.setHeader("Content-Type", bundle.getMimeType());

        // If the browser supports GZIP, return the GZIP file.
        final Http.Header acceptEncodingHeader = request.headers.get("accept-encoding"); // key must be lower-case.
        if (acceptEncodingHeader != null && acceptEncodingHeader.value().contains("gzip")) {
            response.setHeader("Content-Encoding", "gzip");
            response.setHeader("Content-Length", bundle.getOutputFileGzip().length() + "");

            try {
                // renderBinary() will override any caching headers.
                response.direct = new FileInputStream(bundle.getOutputFileGzip());
            } catch (final FileNotFoundException ex) {
                error(ex);
            }
        } else {
            // GZIP not supported by the browser.
            response.setHeader("Content-Length", bundle.getOutputFile().length() + "");

            try {
                // renderBinary() will override any caching headers.
                response.direct = new FileInputStream(bundle.getOutputFile());
            } catch (final FileNotFoundException ex) {
                error(ex);
            }
        }
    }
}
