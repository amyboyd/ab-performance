package controllers;

import models.SiteFeedback;
import models.Account;

/**
 * Also see {@link SiteFeedback}
 */
public class SiteFeedbacks extends BaseController {
    /**
     * May be called via AJAX.
     */
    public static void form(String forward) {
        if (forward == null && request.headers.containsKey("referer")) {
            forward = request.headers.get("referer").value();
        }
        render(forward);
    }

    /**
     * Save form from {@link #sendFeedback()}.
     * May be called via AJAX.
     */
    public static void formHandler(String forward, String message) {
        if (message == null || message.length() == 0) {
            flash.error("Your feedback message was empty. Please type it in the text box below.");
            form(forward);
        }

        new SiteFeedback(message, request.remoteAddress, (Account) request.args.get("currentUser")).create();

        if (request.isAjax()) {
            ok();
        } else {
            flash.success("Thanks, your feedback has been sent, and is much appreciated.");
            redirectToForwardURL();
        }
    }
}
