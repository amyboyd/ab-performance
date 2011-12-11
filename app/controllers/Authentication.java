package controllers;

import models.Domain;
import models.User;
import models.AccountType;
import models.Project;
import play.Logger;
import play.data.validation.*;
import play.libs.Crypto;
import play.mvc.*;

public class Authentication extends BaseController {
    private static final String LOGIN_SESSION = "login";

    private static final String REMEMBER_COOKIE = "remember";

    @Util
    public static User findLoggedInUser() {
        // Already logged in through the active session?
        if (session.contains(LOGIN_SESSION)) {
            User sessionUser = User.find("email", session.get(LOGIN_SESSION)).first();
            if (sessionUser != null) {
                return sessionUser;
            } else {
                Logger.info("User had email (%s) in session, but doesn't exist in DB", session.get(LOGIN_SESSION));

                // User was deleted from the database?
                session.remove(LOGIN_SESSION);
            }
        }

        // Login using the "remember me" cookie, if there is one.
        Http.Cookie remember = request.cookies.get(REMEMBER_COOKIE);
        if ((remember != null) && (remember.value.indexOf("-") > 0)) {
            // Cookie value is "[sign]-[email]", where [sign] is [email] encrypted with
            // this application's private key.
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String email = remember.value.substring(remember.value.indexOf("-") + 1);
            if (Crypto.sign(email).equals(sign)) {
                User cookieUser = User.findByEmail(email);
                if (cookieUser != null) {
                    Logger.info("Login cookie is signed correctly. Logging in as: " + email);
                    session.put(LOGIN_SESSION, email);
                    return cookieUser;
                } else {
                    Logger.info("Failed to login through cookie. User doesn't exist anymore. Email %s", email);
                    response.removeCookie(REMEMBER_COOKIE);
                }
            } else {
                Logger.info("Cookie is not signed correctly. Sign = %s. Email = %s", sign, email);
            }
        }

        return null;
    }

    public static void register(String forward) {
        if (isAuth()) {
            Users.overview();
        }
        render("Authentication/register.html", forward);
    }

    /**
     * Receive and validate the register form.
     * If an already-registered email address is submitted and the password is correct, user is logged in.
     */
    public static void registerHandler(String forward, String projectName, String publicDomains,
            String privateDomains, AccountType accountType, String email, String password) {
        if (isAuth()) {
            Users.overview();
        }

        checkAuthenticity();

        String errorMessage = null;

        final User existingUserByEmail = User.findByEmail(email);
        if (existingUserByEmail != null) {
            if (existingUserByEmail.checkPassword(password)) {
                // Login (even though this is the register action).
                session.put(LOGIN_SESSION, email);
                flash.success("You are now logged in, " + email + ".");
                response.setCookie(REMEMBER_COOKIE, Crypto.sign(email) + "-" + email, "30d");
                existingUserByEmail.onLogin();
                Users.overview();
            } else {
                Logger.info("Email (%s) matches a user, password (%s) does not", email, password);
                errorMessage = "That email address is already registered by a user, and you didn't enter the correct password.";
            }
        }

        if (!validation.email(email).ok) {
            errorMessage = "Please enter a valid email address.";
        } else if (!validation.required(projectName).ok) {
            errorMessage = "Please enter a company or project name.";
        } else if (!validation.required(password).ok) {
            errorMessage = "Please enter a password.";
        }

        if (errorMessage != null) {
            // Errors -- go back to the form.
            Logger.info("At least one error in registration form. Email = %s, password = %s, name = %s, publicDomains = %s, privateDomains = %s, accountType = %s", email, password, projectName, publicDomains, privateDomains, accountType);
            flash.put("userRegisterError", errorMessage);
            params.flash();
            Validation.keep();

            if (request.headers.containsKey("referer")) {
                redirect(request.headers.get("referer").value());
            } else {
                Authentication.register(forward);
            }
        }

        // Everything is OK, register.
        User user = new User(email, password);
        user.create();
        Project project = new Project(projectName, accountType, user);
        project.create();
        Domain.createAll(Domain.toPublicDomains(publicDomains, project));
        Domain.createAll(Domain.toPrivateDomains(privateDomains, project));

        // Login.
        session.put(LOGIN_SESSION, email);
        response.setCookie(REMEMBER_COOKIE, Crypto.sign(email) + "-" + email, "30d");

        play.Logger.info("User has registered. User = %d, project = %s", user.id, project.id);

        Users.overview();
    }

    /**
     * These params can be put in the query string:
     * - forward - although this exists for all pages, it is most relevant here.
     * - overrideMessage - overrides the "flash" message on the page.
     */
    public static void login(String forward, String overrideMessage) {
        if (isAuth()) {
            Users.overview();
        }
        render("Authentication/login.html", forward, overrideMessage);
    }

    /**
     * Receive and validate the login form.
     */
    public static void loginHandler(String forward, String email, String password) {
        if (isAuth()) {
            Users.overview();
        }

        checkAuthenticity();

        if (email.isEmpty()) {
            flash.put("userLoginError", "Please enter your email address first.");
            redirect(request.headers.get("referer").value());
        }

        User user = User.findByEmailAndPassword(email, password);
        if (user instanceof User) {
            // Correct email/password. Set cookie and session data so the user is logged in for 90 days.
            session.put(LOGIN_SESSION, user.email);
            flash.success("You are now logged in.");
            response.setCookie(REMEMBER_COOKIE, Crypto.sign(user.email) + "-" + user.email, "90d");
            user.onLogin();
            Users.overview();
        } else {
            flash.put("userLoginError", "Incorrect email/password combination.");
            params.flash();
            redirect(request.headers.get("referer").value());
        }
    }

    /**
     * Logout and clear cookie/session.
     */
    @Check(value = Checks.Condition.LOGGED_IN, onFail = Checks.FailAction.GO_TO_INDEX)
    public static void logout() {
        session.clear();
        response.setCookie(REMEMBER_COOKIE, "", "30d");
        flash.success("You have been logged out.");
        redirect("/");
    }

    @Check(value = Checks.Condition.NOT_LOGGED_IN, onFail = Checks.FailAction.GO_TO_INDEX)
    public static void forgotPassword() {
        render("Authentication/forgot-password.html");
    }

    @Check(value = Checks.Condition.NOT_LOGGED_IN, onFail = Checks.FailAction.GO_TO_INDEX)
    public static void forgotPasswordHandler(final String email) {
        checkAuthenticity();

        if (email.isEmpty()) {
            flash.error("Please enter your email address first.");
            forgotPassword();
        }

        User user = User.findByEmail(email);
        if (user != null) {
            Mails.forgotPassword(user);
            flash.success("We have sent an email to your registered email address. Please open the email and follow the instructions inside. You will be able to reset your password.");
            Logger.info("User forgot password. Emailed reset instructions to %s", email);
            controllers.Application.index();
        } else {
            flash.error("That email address is not registered to any user.");
            params.flash();
            Logger.info("User forgot password. Email being tried is not registered: %s", email);
            forgotPassword();
        }
    }

    /**
     * @param u User ID.
     * @param vc Must match user's validation code.
     */
    @Check(value = Checks.Condition.NOT_LOGGED_IN, onFail = Checks.FailAction.GO_TO_INDEX)
    public static void resetPassword(final Long u, final String vc) {
        Logger.info("On reset password page, user ID %d, validation code %s, IP address %s", u, vc, request.remoteAddress);

        final User user = User.findById(u);
        if (user == null) {
            error("No user with ID: " + u);
        } else if (user.getValidationCode().equals(vc)) {
            render("Authentication/reset-password.html", user);
        } else {
            error("Wrong code for user " + user.id);
        }
    }

    @Check(value = Checks.Condition.NOT_LOGGED_IN, onFail = Checks.FailAction.GO_TO_INDEX)
    public static void resetPasswordHandler(final Long u, final String vc,
            final String password, final String password2) {
        Logger.info("On reset password handler, user ID %d, validation code %s, IP address %s", u, vc, request.remoteAddress);

        checkAuthenticity();

        final User user = User.findById(u);
        if (user == null) {
            error("No user with ID: " + u);
        } else if (!password.equals(password2)) {
            flash.error("The two passwords you entered do not match.");
            resetPassword(u, vc);
        } else if (user.getValidationCode().equals(vc)) {
            // Everything is OK - change password.
            user.setPassword(password);
            user.save();
            play.Logger.info("User %s has reset their password", user.id);

            // Login.
            session.put(LOGIN_SESSION, user.email);
            response.setCookie(REMEMBER_COOKIE, Crypto.sign(user.email) + "-" + user.email, "30d");
            user.onLogin();

            flash.success("Your password has been reset and you are now logged in.");
            controllers.Application.index();
        } else {
            error("Wrong code for user " + user.id);
        }
    }

    /**
     * Form to change password.
     */
    public static void changePassword(String forward) {
        render("Authentication/change-password.html", forward);
    }

    /**
     * Save new password. The user must enter hir old password correctly, then
     * a new password twice. No confirmation email is sent.
     */
    public static void changePasswordHandler(String forward,
            String oldPassword, String newPassword1, String newPassword2) {
        checkAuthenticity();
        requireHttpMethod("POST");

        final User user = requireAuthenticatedUser();

        Validation.isTrue("oldPassword", user.checkPassword(oldPassword)).message("The old password you entered is not correct");
        Validation.required("newPassword1", newPassword1).message("Please enter your new password");
        Validation.required("newPassword2", newPassword2).message("Please enter your new password again. This is to ensure you didn't make a type.");
        Validation.equals("newPassword1", newPassword1, "newPassword2", newPassword2).message("Your new password was typed differently in each field, but must be the same");

        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            changePassword(params.get("forward"));
        }

        // All is OK.
        user.password = newPassword1;
        user.save();
        flash.success("Your password has been changed");
        redirectToForwardURL();
    }
}
