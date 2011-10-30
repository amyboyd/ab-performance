package com.abperf;

import java.io.File;
import java.sql.SQLException;
import models.*;
import play.Logger;
import play.Play;
import play.db.DB;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.libs.IO;
import play.test.Fixtures;

/**
 * Start the application.
 */
@OnApplicationStart
public class Bootstrap extends Job {
    /**
     * If the bootstrap job is currently running (started but not finished).
     */
    public static boolean isRunning = true;

    @Override
    public void doJob() {
        if (!Play.id.equals("dev") && !Play.id.equals("prod")) {
            Logger.info("Play ID must be dev/prod. Got " + Play.id);
            Play.stop();
        } else {
            Logger.info("Bootstrap started. Play ID is '%s'.", Play.id);
        }

        isRunning = true;

        if (Constants.IS_DEV && User.count() == 0L) {
            loadFixtures();
        }

        File bundlesDir = Play.getFile("public/bundles");
        if (!bundlesDir.exists()) {
            bundlesDir.mkdirs();
        }

        Logger.info("Finished bootstrap");
        isRunning = false;
    }

    private static void loadFixtures() {
        Fixtures.deleteAllModels();

        try {
            for (int i = 1; i <= 16; i++) {
                DB.execute(IO.readContentAsString(Play.getFile("conf/fixtures/IPAddress/" + i + ".sql")));
            }
            DB.getConnection().commit();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }

        Fixtures.loadModels("fixtures/fixtures.yml");
    }
}
