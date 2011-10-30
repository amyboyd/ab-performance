package models;

import java.util.*;
import javax.persistence.*;
import play.data.validation.*;
import play.db.jpa.Model;

/**
 * Announcements can be sent to all users, or just users with certain account types.
 *
 * These are combined with {@link UserNotification}s. When a new announcement is saved,
 * user notifications are created for each user.
 */
@Entity
@Table(name = "announcement")
public class Announcement extends Model {
    @Required
    @MinSize(5)
    @MaxSize(60)
    public String title;

    /**
     * URL to a page detailing the announcement.
     */
    @Required
    @URL
    public String url;

    public boolean toBetaUsers;

    public boolean toFreeUsers;

    public boolean toPaidUsers;

    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public static List<Announcement> findApplicableToUser(final User user, final int limit) {
        if (user.accountType.equals(AccountType.BETA)) {
            return find("toBetaUsers", true).fetch(limit);
        } else if (user.accountType.equals(AccountType.FREE)) {
            return find("toFreeUsers", true).fetch(limit);
        } else if (user.accountType.paid) {
            return find("toPaidUsers", true).fetch(limit);
        } else {
            throw new IllegalStateException("Account type not expected: " + user.accountType);
        }
    }

    public boolean appliesToUser(final User user) {
        if (user.accountType.equals(AccountType.BETA)) {
            return toBetaUsers;
        } else if (user.accountType.equals(AccountType.FREE)) {
            return toFreeUsers;
        } else if (user.accountType.paid) {
            return toPaidUsers;
        } else {
            throw new IllegalStateException("Account type not expected: " + user.accountType);
        }
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    @Override
    public void _save() {
        // Notify all targetted users of the announcement.
        final List<User> users = User.all().fetch();
        for (final User user: users) {
            if (appliesToUser(user)) {
                new UserNotification(title, url, user).create();
            }
        }
        users.clear();

        super._save();
    }
}
