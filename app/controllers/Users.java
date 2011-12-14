package controllers;

import models.AccountType;
import models.Domain;
import models.Project;
import models.User;
import play.Logger;
import play.data.validation.Validation;

public class Users extends BaseController {
    public static void overview() {
        render();
    }

    public static void continueToPaypal(Long id) {
        Project project = Project.findById(id);
        render("Users/continue-to-paypal.html", project);
    }

    public static void justPaidProject(Long id) {
        Logger.info("Just paid project: " + id);
        overview();
    }

    public static void addProject() {
        render("Users/add-project.html");
    }

    public static void addProjectHandler(String projectName, String publicDomains,
            String privateDomains, AccountType accountType) {
        requireHttpMethod("POST");
        checkAuthenticity();

        User user = requireAuthenticatedUser();

        Project project = new Project(projectName, accountType, user);
        project.create();
        Domain.createAll(Domain.toPublicDomains(publicDomains, project));
        Domain.createAll(Domain.toPrivateDomains(privateDomains, project));

        play.Logger.info("New project. User = %d, project = %s", user.id, project.id);

        if (project.waitingForPayment()) {
            Users.continueToPaypal(project.id);
        } else {
            Users.overview();
        }
    }
//
//    /**
//     * Form to editPublicProfile profile.
//     * Show subscription information (if Pro), and links to upgrade, downgrade and close shop.
//     */
//    public static void editDetails(String forward) {
//        requireAuthenticatedUser();
//        render("Sell/profile.html");
//    }
//
//    /**
//     * Save profile details. Avatar and images are not submitted to this action.
//     */
//    public static void editDetailsHandler(String forward) {
//        requireHttpMethod("POST");
//        checkAuthenticity();
//
//        final User user = requireAuthenticatedUser();
//        user.edit("user", params.all());
//
//        validation.valid(user);
//        if (Validation.hasErrors()) {
//            Validation.keep();
//            params.flash();
//            flash.error(com.abperf.Constants.FORM_HAD_ERRORS_MESSAGE);
//            editDetails(forward);
//        }
//
//        user.save();
//
//        flash.success("Your changes have been saved");
//        redirectToForwardURL();
//    }

    /**
     * Form to change email.
     */
    public static void changeEmail(String forward) {
        render("Users/change-email.html");
    }

    /**
     * Save user's new email defaultDeliveryAddress. No confirmation email is sent.
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

        // Authentication depends on the email defaultDeliveryAddress, so re-authenticate.
        session.put("email", newEmail);
        response.setCookie("remember", play.libs.Crypto.sign(newEmail) + "-" + newEmail, "30d");

        user.email = newEmail;
        user.emailConfirmed = false;
        user.save();

        Mails.confirmEmailChange(user);

        flash.success("Please check your new email address' inbox. We have sent you a confirmation email.");
        redirectToForwardURL();
        Application.index();
    }

    public static void confirmEmail(final Long id, final String code) {
        final User user = User.findById(id);
        if (user == null) {
            error("No user with ID: " + id);
        } else if (user.getValidationCode().equals(code)) {
            user.emailConfirmed = true;
            user.save();
            Logger.info("User (%s) confirmed email address (%s)", user.id, user.email);
            flash.success("Your registration and email address have been confirmed.");
            controllers.Application.index();
        } else {
            error("Wrong code (" + code + ") for ID (" + id + ")");
        }
    }

    public static void resendConfirmationEmail(String forward) {
        final User user = requireAuthenticatedUser();
        if (user.emailConfirmed) {
            flash.error("Your email address is already confirmed");
            redirectToForwardURL();
        } else if (user.email == null) {
            flash.error("You have not set an address address yet. You can do that just now, below.");
            changeEmail(forward);
        } else {
            Logger.info("Resending email address confirmation email to %s", user);
            controllers.Mails.welcome(user);
            flash.success("We have sent another email. Please check your inbox (and spam folder)");
            redirectToForwardURL();
        }
    }

    /**
     * Return plain-text describing the registration status of an email address.
     */
    public static void checkEmail(final String email) {
        if (email == null || email.isEmpty()) {
            renderText("");
        }
        if (!validation.email(email).ok) {
            renderText("This is not a valid email address.");
        }
        final User user = User.find("email = ?", email).first();
        if (user != null) {
            renderText("This email is registered to a user.");
        } else {
            renderText("This email has not been registered before.");
        }
    }
}
