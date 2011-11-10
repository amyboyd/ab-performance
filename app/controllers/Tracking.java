package controllers;

import java.util.*;
import models.*;
import play.mvc.Http.StatusCode;

public class Tracking extends BaseController {
    /**
     * @param guid A unique page request ID.
     * @param time Time the page loaded at.
     * @param url
     * @param tests The map key is the test group name and the value is the test ID.
     */
    public static void startBeta(String guid, long time, String url, Map<String, String> tests) {
        onlyPOSTallowed();

        Domain domain = Domain.findDomainByURL(url);
        if (domain == null) {
            response.status = StatusCode.INTERNAL_ERROR;
            return;
        }

        User account = domain.account;

        PageView pageView = new PageView();
        pageView.account = account;
        pageView.url = url;
        pageView.time = new Date(time);
        pageView.guid = guid;

        for (final String testGroupName: tests.keySet()) {
            final String testId = tests.get(testGroupName);

            pageView.testsJSON.addProperty(testGroupName, testId);

            // Check if we already know the CSS for this ID. If we don't know the CSS, request it.
            if (!TestCSS.cssIsKnown(testId)) {
                response.print(testId);
                response.print(",");
            }
        }

        response.setHeader("Content-Type", "text/plain");
        ok();
    }

    public static void pingBeta() {
        onlyPOSTallowed();
        ok();
    }

    public static void supplyCssBeta() {
        onlyPOSTallowed();
        ok();
    }

    /**
     * Only POST is allowed in production. In development, GET is allowed just to make debugging easier.
     */
    private static void onlyPOSTallowed() {
        if (com.abperf.Constants.IS_DEV) {
            requireHttpMethod("POST");
        }
    }
}
