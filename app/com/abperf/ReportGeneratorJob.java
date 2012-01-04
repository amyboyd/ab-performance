package com.abperf;

import java.util.*;
import models.Project;
import play.jobs.Every;
import play.jobs.Job;

@Every("1h")
public class ReportGeneratorJob extends Job {
    @Override
    public void doJob() throws Exception {
        final List<Project> projects = Project.find("(pageViews - reportPageViews) >= 1000").fetch();
        for (final Project project: projects) {
            new ReportGenerator(project).execute();
        }
    }
}
