package controllers;

import com.abperf.*;
import java.util.Date;
import models.*;
import models.Domain.DomainAccess;
import play.Logger;
import play.data.validation.Validation;
import play.mvc.Util;

public class Users extends BaseController {
    public static void overview() {
        render();
    }

    public static void continueToPaypal(final Long id) {
        final Project project = getProjectByIDandSecure(id);
        render("Users/continue-to-paypal.html", project);
    }

    public static void justPaidProject(final Long id) {
        final Project project = getProjectByIDandSecure(id);
        project.paymentReceivedAt = new Date();
        project.save();

        Logger.info("Just paid project: " + id);

        overview();
    }

    public static void addProject() {
        render("Users/add-project.html");
    }

    public static void addProjectHandler(final String projectName, final String publicDomains,
            final String privateDomains, final AccountType accountType) {
        requireHttpMethod("POST");
        checkAuthenticity();

        final User user = requireAuthenticatedUser();

        final Project project = new Project(projectName, accountType, user);
        project.create();

        Domain.createAll(publicDomains, project, DomainAccess.PUBLIC);
        Domain.createAll(privateDomains, project, DomainAccess.PRIVATE);

        play.Logger.info("New project. User = %d, project = %s", user.id, project.id);

        if (project.waitingForPayment()) {
            Users.continueToPaypal(project.id);
        } else {
            Users.overview();
        }
    }

    public static void report(final Long id) {
        final Project project = getProjectByIDandSecure(id);
        new ReportGenerator(project).execute();
        redirect("/reports/" + project.reportOutputDir);
    }

    public static void reportCSS(final String testID) {
        final TestCSS test = TestCSS.findByTestId(testID);
        renderText(test.css);
    }

    public static void editProject(final Long id) {
        final Project project = getProjectByIDandSecure(id);
        render("Users/edit-project.html", project);
    }

    public static void editProjectHandler(final Long id, final String projectName,
            final String publicDomains, final String privateDomains) {
        requireHttpMethod("POST");
        checkAuthenticity();

        final Project project = getProjectByIDandSecure(id);
        if (!project.name.equals(projectName)) {
            project.name = projectName;
            project.save();
        }

        Domain.deleteAll(project);
        Domain.createAll(publicDomains, project, DomainAccess.PUBLIC);
        Domain.createAll(privateDomains, project, DomainAccess.PRIVATE);

        flash.success("Your changes have been saved");
        overview();
    }

    public static void getHTML(final Long id) {
        final Project project = getProjectByIDandSecure(id);
        render("Users/get-html.html", project);
    }

    /**
     * Form to change email.
     */
    public static void changeEmail(String forward) {
        render("Users/change-email.html");
    }

    /**
     * Save user's new email address.
     */
    public static void changeEmailHandler(String newEmail, String forward) {
        checkAuthenticity();
        requireHttpMethod("POST");

        final User user = requireAuthenticatedUser();

        Validation.required("newEmail", newEmail).message("Please enter your new email address");
        Validation.email("newEmail", newEmail).message("That is not a valid format for an email address");
        Validation.isTrue("newEmail", !user.email.equals(newEmail)).message("You are already using that email address!");
        Validation.isTrue("newEmail", User.count("email", newEmail) == 0L).message("Another user is already using that email address. If this is a problem, please contact us.");

        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            changeEmail(params.get("forward"));
        }

        // All is OK.

        // Authentication depends on the email address, so re-authenticate.
        session.put("email", newEmail);
        response.setCookie("remember", play.libs.Crypto.sign(newEmail) + "-" + newEmail, "30d");

        user.email = newEmail;
        user.emailConfirmed = false;
        user.save();

        flash.success("Your new email address has been saved.");
        redirectToForwardURL();
        Users.overview();
    }

    /**
     * Get the project and ensure security is OK.
     */
    @Util
    static Project getProjectByIDandSecure(final Long id) {
        final Project project = Project.findById(id);
        notFoundIfNull(project, "Project " + id + " not found");
        if (!project.user.equals(requireAuthenticatedUser())) {
            Logger.info(requireAuthenticatedUser() + " trying to access project " + project.id);
            forbidden();
        }
        return project;
    }
}
