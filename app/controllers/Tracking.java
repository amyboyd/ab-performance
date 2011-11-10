package controllers;

import java.util.*;
import models.*;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Http.StatusCode;
import play.mvc.Util;

public class Tracking extends BaseController {
    /**
     * @param guid A unique page request ID.
     * @param time Time the page loaded at.
     * @param url
     * @param tests The map key is the test group name and the value is the test ID.
     */
    public static void startBeta(final String guid, final long time, final String url,
            final Map<String, String> tests) {
        onlyPOSTallowed();

        final Domain domain = Domain.findDomainByURL(url);
        if (domain == null) {
            response.status = StatusCode.INTERNAL_ERROR;
            Logger.error("URL is on an unregistered domain:" + url);
            return;
        }

        final PageView pageView = new PageView(domain.user, guid, time, url, tests);
        pageView.save();

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
    public static void pingBeta(final String guid, final long time, final String status,
            final Map<String, String> css) {
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

    /**
     * Only POST is allowed in production. In development, GET is allowed just to make debugging easier.
     */
    @Util
    private static void onlyPOSTallowed() {
        if (com.abperf.Constants.IS_PROD) {
            requireHttpMethod("POST");
        }
    }
}
