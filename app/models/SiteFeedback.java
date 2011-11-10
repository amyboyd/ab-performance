package models;

import java.util.*;
import javax.persistence.*;
import play.db.jpa.*;
import play.data.validation.*;

/**
 * Also see {@link SiteFeedbacks}
 */
@Entity
@Table(name = "site_feedback")
public class SiteFeedback extends Model {
    @Required
    @Lob
    public String message;

    public String ipAddress;

    @ManyToOne
    public Account user;

    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public SiteFeedback(String message, String ipAddress, Account user) {
        this.message = message;
        this.ipAddress = ipAddress;
        this.user = user;
        this.createdAt = new Date();
    }
}
