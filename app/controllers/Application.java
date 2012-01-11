package controllers;

import com.abperf.ReportGenerator;
import models.Project;
import models.TestCSS;

public class Application extends BaseController {
    public static void index() {
        render();
    }

    public static void features() {
        render();
    }

    public static void demo() {
        render();
    }

    public static void generateReport(final Long projectID) {
        final Project project = Users.getProjectByIDandSecure(projectID);
        notFoundIfNull(project, "Project not found");

        new ReportGenerator(project).execute();

        redirect("/reports/" + project.reportOutputDir);
    }

    public static void reportCSS(final String testID) {
        final TestCSS t = TestCSS.findByTestId(testID);
        renderText(t.css);
    }

    public static void terms() {
        render();
    }

    public static void privacy() {
        render();
    }
}
