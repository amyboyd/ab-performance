package models;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.persistence.*;
import play.data.validation.Match;
import play.data.validation.Required;
import play.db.jpa.*;

@Entity
@Table(name = "domain")
public class Domain extends GenericModel {
    @Id
    @Required
    @Match(value = "([a-zA-Z0-9-]+\\.)+[a-zA-Z]+", message = "That isn't a valid domain format")
    public String domain;

    @Required
    @ManyToOne(optional = false)
    public User account;

    @Required
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    public static Domain findDomainByURL(final String url) {
        try {
            return find("domain", new URL(url).getHost()).first();
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Malformed URL: " + url);
        }
    }

    @Override
    public Object _key() {
        return domain;
    }
}
