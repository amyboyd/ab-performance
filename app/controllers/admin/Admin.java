package controllers.admin;

import controllers.BaseController;
import java.util.List;
import models.PageView;
import models.Project;
import models.User;
import play.Logger;
import play.Play;
import play.libs.IO;
import play.mvc.Before;

public class Admin extends BaseController {
    @Before(priority = 6)
    public static void ensureIsAdmin() {
        User user = requireAuthenticatedUser();
        if (!user.admin) {
            Logger.info("User trying to access admin page: " + user.id + ", " + user);
            redirect("/");
        }
    }

    public static void index() {
        final long usersCount = User.count();
        final long projectsCount = Project.count();
        final long pageViewsCount = PageView.count();
        render(usersCount, projectsCount, pageViewsCount);
    }

    public static void users() {
        final List<User> users = User.find("order by id desc").fetch();
        render(users);
    }

    public static void projects() {
        final List<Project> projects = Project.find("order by id desc").fetch();
        render(projects);
    }

    public static void applicationLogFile() {
        final String content = IO.readContentAsString(Play.getFile("logs/application.log"));
        render(content);
    }
}
