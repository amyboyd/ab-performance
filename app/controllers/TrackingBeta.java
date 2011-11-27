package controllers;

import java.util.*;
import models.*;
import org.apache.commons.lang.StringUtils;
import play.libs.optimization.ClosureBundle;
import play.Logger;
import play.mvc.Http.StatusCode;
import play.mvc.Util;

public class TrackingBeta extends BaseController {
    /**
     * Response code may be 200, 402 or 429.
     *
     * @param guid A unique page request ID.
     * @param time Time the page loaded at.
     * @param url
     * @param tests The map key is the test group name and the value is the test ID.
     */
    public static void start(final String guid, final long time, final String url,
            final Map<String, String> tests) {
        allowCrossDomain();
        onlyPOSTallowed();

        final Domain domain = Domain.findDomainByURL(url);
        if (domain == null) {
            response.status = StatusCode.PAYMENT_REQUIRED; // 402
            response.print("This domain is not registered to any user");
            Logger.error("URL is on an unregistered domain:" + url);
            return;
        }

        final User user = domain.user;
        if (user.hasReachedMaxPageViews()) {
            // 429 is a proposed status code. See here: http://tools.ietf.org/html/draft-nottingham-http-new-status-02#section-4
            response.status = 429;
            response.print("Page view limit has been reached");
            return;
        }

        final PageView pageView = new PageView(domain.user, guid, time, url, tests);
        pageView.save();

        user.pageViews++;
        user.save();

        response.setHeader("Content-Type", "text/plain");
        response.print(StringUtils.join(pageView.testIDsWithUnknownCSS, ','));
        ok();
    }

    /**
     * @param guid The unique page request ID.
     * @param time Time the page loaded at.
     * @param status "active" or "inactive".
     * @param css The keys are the test IDs, the values are the CSS.
     */
    public static void ping(final String guid, final long time, final String status,
            final Map<String, String> css) {
        allowCrossDomain();
        onlyPOSTallowed();

        PageView pageView = PageView.findByGUID(guid);
        pageView.pingsJSON.addProperty(String.valueOf(time), status);
        // We must call "setJSONstringsFromJSONobjects" manually so that at least one field changes.
        // If no field changes *before* JPA hooks, then no hooks are called and as a result there is
        // no need to save the object.
        pageView.setJSONstringsFromJSONobjects();
        pageView.save();

        for (final String id: css.keySet()) {
            TestCSS.setCSS(id, css.get(id));
        }

        ok();
    }

    public static void clientScripts() {
        if (com.abperf.Constants.IS_DEV) {
            clientScripts.getBundleFile().delete();
        }

        response.cacheFor("61d");
        clientScripts.applyToResponse(request, response);
    }

    private static final ClosureBundle clientScripts = createClientScriptsBundle();

    @Util
    private static ClosureBundle createClientScriptsBundle() {
        final ClosureBundle bundle = new ClosureBundle(
                "client-beta.js",
                "public/closure/closure/bin/build/closurebuilder.py",
                null,
                //com.google.javascript.jscomp.CompilationLevel.ADVANCED_OPTIMIZATIONS,
                new String[] {
                    "public/closure/closure/goog",
                    "public/closure/third_party/closure",
                    "public/js/client", },
                new String[] { "abperf", "abperf.debug" });
        bundle.setOutputWrapper("(function(){ %output% })();");
        return bundle;
    }

    /**
     * Only POST is allowed in production. In development, GET is allowed just to make debugging easier.
     */
    @Util
    private static void onlyPOSTallowed() {
        if (com.abperf.Constants.IS_PROD) {
            requireHttpMethod("POST");
        }
    }

    @Util
    private static void allowCrossDomain() {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }
}
