package controllers;

import com.abperf.Bootstrap;
import models.Account;
import play.mvc.Mailer;
import static com.abperf.Constants.SITE_NAME;

/**
 * All outgoing emails are sent from here. The templates are located in "app/views/Mails/"
 * and there should be a HTML and plain-text template for most user-facing emails.
 */
public class Mails extends Mailer {
    private static final String OUTBOUND_ADDRESS = String.format("%s <%s>",
            SITE_NAME,
            play.Play.configuration.getProperty("application.outboundEmailAddress"));

    private static final String TEAM_ADDRESS = String.format("%s <%s>",
            SITE_NAME,
            play.Play.configuration.getProperty("application.teamEmailAddress"));

    /**
     * After registering, send a welcome email, which also has a confirmation link.
     * @param user The new user.
     */
    public static void welcome(final Account user) {
        setSubject("Your new %s account", SITE_NAME);
        addRecipient(formatAddress(user));
        setFrom(OUTBOUND_ADDRESS);
        setReplyTo(TEAM_ADDRESS);

        send(user);
    }

    public static void forgotPassword(final Account user) {
        setSubject("Forgot your password on " + SITE_NAME + "? Create a new password here");
        addRecipient(formatAddress(user));
        setFrom(OUTBOUND_ADDRESS);
        setReplyTo(TEAM_ADDRESS);

        send(user);
    }

    public static void confirmEmailChange(final Account user) {
        setSubject("Confirm your new email address");
        addRecipient(user.email);
        setFrom(OUTBOUND_ADDRESS);
        setReplyTo(TEAM_ADDRESS);

        send(user);
    }

    private static String formatAddress(final Account user) {
        if (user.email == null) {
            if (Bootstrap.isRunning) {
                return play.Play.configuration.getProperty("application.testEmailAddress");
            }
            throw new IllegalStateException("User has no email: " + user);
        }
        return String.format("%s <%s>", user.toString(), user.email);
    }
}
