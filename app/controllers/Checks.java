package controllers;

import models.Account;
import java.util.Map;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;

/**
 * Enforce {@link Check} annotations on controller methods.
 */
public class Checks extends Controller {
    /**
     * Before any action, enforce {@link Check} annotations.
     *
     * @throws Throwable
     */
    @Before(priority = 2)
    public static void check() throws Throwable {
        // Checks applied directly to the method.
        Check check = getActionAnnotation(Check.class);
        if (check != null) {
            check(check);
        }

        // Checks inherited from a class.
        check = getControllerInheritedAnnotation(Check.class);
        if (check != null) {
            check(check);
        }
    }

    /**
     * Ensure all conditions of a @Check annotation have been met.
     */
    private static void check(Check check) {
        for (final Condition condition: check.value()) {
            if (!check(condition)) {
                fail(condition, check);
            }
        }
    }

    /**
     * Check that a condition has been met.
     */
    private static boolean check(Condition condition) {
        Map<String, Object> httpParams = play.mvc.Http.Request.current().args;

        boolean auth = httpParams.containsKey("isAuth") && ((Boolean) httpParams.get("isAuth")).booleanValue();

        switch (condition) {
            case LOGGED_IN:
                return auth;
            case NOT_LOGGED_IN:
                return !auth;
            case IS_ADMIN:
                Account user = (Account) httpParams.get("currentUser");
                boolean isAdmin = auth && user.admin;

                if (!isAdmin) {
                    Logger.info("Failed admin check. User %s, IP address %s", user, request.remoteAddress);
                }

                return isAdmin;
        }
        throw new RuntimeException("Unexpected condition: " + condition);
    }

    /**
     * A check has not succeeded.
     *
     * @throws RuntimeException if the FailAction is not known.
     */
    private static void fail(Condition condition, Check check) {
        final play.mvc.Router.ActionDefinition ad;

        switch (check.onFail()) {
            case NOT_FOUND:
                notFound("Failed condition: " + condition);
                break;
            case LOG_IN:
                session.put("forward", request.url);
                ad = play.mvc.Router.reverse("users.Authentication.login");
                ad.absolute();
                redirect(ad.url);
                break;
            case GO_TO_INDEX:
                ad = play.mvc.Router.reverse("buy.Application.index");
                ad.absolute();
                redirect(ad.url);
                break;
        }
        throw new RuntimeException("Unexpected fail actionL " + check.onFail());
    }

    public enum Condition {
        LOGGED_IN,
        NOT_LOGGED_IN,
        IS_ADMIN;

    }

    public enum FailAction {
        NOT_FOUND,
        LOG_IN,
        GO_TO_INDEX;

    }
}
