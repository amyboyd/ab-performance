package models;

import java.util.List;
import javax.persistence.*;
import play.data.validation.*;
import play.db.jpa.Model;
import play.mvc.Router.ActionDefinition;

/**
 * Displayed at the top of the page for an individual user.
 * For example: "Read this [link] [delete]".
 */
@Entity
@Table(name = "user_notification")
public class UserNotification extends Model {
    @Required
    @MinSize(10)
    @MaxSize(255)
    public String text;

    /**
     * May be null.
     */
    @URL
    public String url;

    @Required
    @ManyToOne
    public User user;

    public static List<UserNotification> findByUser(final User user, final int limit) {
        return find("user", user).fetch(limit);
    }

    public UserNotification(String text, ActionDefinition url, User user) {
        this.text = text;
        this.user = user;

        url.absolute();
        this.url = url.url;
    }

    public UserNotification(final String text, final String url, final User user) {
        this.text = text;
        this.url = url;
        this.user = user;
    }

    public UserNotification(final String text, final User user) {
        this.text = text;
        this.user = user;
    }
}
