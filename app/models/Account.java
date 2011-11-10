package models;

import com.abperf.BCrypt;
import com.abperf.Constants;
import java.util.*;
import javax.persistence.*;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.binding.NoBinding;
import play.data.validation.*;
import play.db.jpa.Model;
import play.libs.Codec;
import play.templates.JavaExtensions;

@Entity
@Table(name = "account")
public class Account extends Model {
    @Required
    @MaxSize(40)
    @MinSize(2)
    public String name;

    @Required
    @Email
    @MaxSize(255)
    @Column(unique = true)
    public String email;

    public boolean emailConfirmed;

    /**
     * Bcrypt'ed password. 60 characters long.
     */
    @Required
    @NoBinding
    public String password;

    public AccountType accountType = AccountType.BETA;

    @Column(name = "is_admin") // "admin" is a reserved keyword.
    public boolean admin;

    /**
     * How the user heard of the site.
     */
    @MaxSize(255)
    public String howHear;

    public int loginCount;

    public int loginFailures;

    @Temporal(TemporalType.TIMESTAMP)
    public Date lastLogin;

    @Temporal(TemporalType.TIMESTAMP)
    public Date registeredAt;

    @OneToMany(mappedBy = "account")
    public Set<Domain> domains;

    public static Account findByEmail(final String email) {
        return find("email", email).first();
    }

    public static boolean isNameUsed(final String name) {
        return count("name", name) > 0L;
    }

    /**
     * Find a user by email and password. The password is checked to be valid.
     *
     * @param email
     * @return Null if no user with the email and password.
     */
    public static Account findByEmailAndPassword(final String email, final String password) {
        final Account user = Account.find("email", email).first();
        if ((user != null) && user.checkPassword(password)) {
            return user;
        }
        return null;
    }

    public Account(final String email, final String password) {
        this.email = email;
        // Try to parse name from email, e.g. "mic.t.boyd@gmail.com" becomes "Mic T Boyd".
        this.name = JavaExtensions.capitalizeWords(email.substring(0, email.indexOf('@')).replaceAll("[.-_]", " ").
                toLowerCase());
        Logger.info("Parsed email %s to name %s", this.email, this.name);
        this.password = password;
    }

    public Account(final String email, final String password, final String name) {
        this(email, password);
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public void onLogin() {
        if (lastLogin != null) {
            new AccountNotification(
                    "Your last login was on " + JavaExtensions.format(lastLogin, "EEEE d MMMM, yyyy").toString(),
                    this).create();
        }

        lastLogin = new Date();
        loginCount += 1;
        save();

        play.Logger.info("User %s (id %d) has logged in", toString(), getId());
    }

    /**
     * Returns a code that can be used in cases of validation, e.g. in emailed links.
     * @return String with 12 alpha-numeric characters.
     */
    public String getValidationCode() {
        return StringUtils.reverse(Codec.hexSHA1(id.toString()).
                substring(0, 12)).
                toUpperCase().
                intern();
    }

    /**
     * Hash the password with BCrypt alogrithm.
     */
    public void setPassword(final String newPassword) {
        if (newPassword.equals(password) || newPassword.isEmpty()) {
            return;
        }

        Logger.info("User %s (id %d) changed password", this, this.id);

        password = BCrypt.hashpw(newPassword, BCrypt.gensalt());
    }

    /**
     * Check if a given password is equal to the one in the database.
     * If the password is incorrect, {@link #loginFailures} is incremented and the user saved.
     *
     * @return True if correct.
     */
    public boolean checkPassword(final String candidate) {
        // On DEV, the password "pass" works for all users.
        if (candidate.equalsIgnoreCase("password") && Constants.IS_DEV) {
            return true;
        }

        // Check that the unencrypted password "candidate" matches one that has previously been hashed.
        final boolean correct = BCrypt.checkpw(candidate, password);
        if (!correct) {
            Logger.info("User %s (id %d) failed password check", this, this.id);
            loginFailures += 1;
            save();
        }
        return correct;
    }

    @PrePersist
    protected void prePersist() {
        if (registeredAt == null) {
            registeredAt = new Date();
        }
    }

    @PostPersist
    protected void postPersist() {
        if (email != null) {
            controllers.Mails.welcome(this);
        }
    }
}
