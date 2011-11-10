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
@Table(name = "account_notification")
public class AccountNotification extends Model {
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
    public Account user;

    public static List<AccountNotification> findByUser(final Account user, final int limit) {
        return find("user", user).fetch(limit);
    }

    public AccountNotification(String text, ActionDefinition url, Account user) {
        this.text = text;
        this.user = user;

        url.absolute();
        this.url = url.url;
    }

    public AccountNotification(final String text, final String url, final Account user) {
        this.text = text;
        this.url = url;
        this.user = user;
    }

    public AccountNotification(final String text, final Account user) {
        this.text = text;
        this.user = user;
    }
}
