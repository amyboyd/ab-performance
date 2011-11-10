package controllers.admin;

import controllers.BaseController;
import models.Account;
import play.Logger;
import play.mvc.Before;

public class Admin extends BaseController {
    @Before(priority = 6)
    public static void ensureIsAdmin() {
        Account user = requireAuthenticatedUser();
        if (!user.admin) {
            Logger.info("User trying to access admin page: " + user.id + ", " + user);
            redirect("/");
        }
    }

    public static void index() {
        render();
    }
}
