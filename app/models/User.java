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

@Entity
@Table(name = "user")
public class User extends Model {
    @Required
    @Email
    @MaxSize(255)
    @Column(unique = true)
    public String email;

    // Not used
    public boolean emailConfirmed;

    /**
     * Bcrypt'ed password. 60 characters long.
     */
    @Required
    @NoBinding
    public String password;

    @Column(name = "is_admin") // "admin" is a reserved keyword.
    public boolean admin;

    public int loginCount;

    public int loginFailures;

    @Temporal(TemporalType.TIMESTAMP)
    public Date lastLogin;

    @Temporal(TemporalType.TIMESTAMP)
    public Date registeredAt;

    @OneToMany(mappedBy = "user")
    public Set<Project> projects;

    public static User findByEmail(final String email) {
        return find("email", email).first();
    }

    /**
     * Find a user by email and password. The password is checked to be valid.
     *
     * @param email
     * @return Null if no user with the email and password.
     */
    public static User findByEmailAndPassword(final String email, final String password) {
        final User user = User.find("email", email).first();
        if ((user != null) && user.checkPassword(password)) {
            return user;
        }
        return null;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.registeredAt = new Date();
    }

    @Override
    public String toString() {
        return email;
    }

    public void onLogin() {
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

        if (id != null) {
            Logger.info("User %s (id %d) changed password", this, this.id);
        }

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
}
