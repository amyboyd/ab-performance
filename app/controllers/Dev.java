package controllers;

import com.abperf.ReportGenerator;
import models.Project;

/**
 * Development-only actions.
 */
public class Dev extends BaseController {
    public static void closureDepsJS() {
        ok();
    }

    @SuppressWarnings("unchecked")
    public static void generateReport(Long projectId) {
        final Project project = Users.getProjectByIDandSecure(projectId);
        notFoundIfNull(project, "Project not found");

        new ReportGenerator(project).execute();

        renderText(project.reportOutputDir);
    }
}
