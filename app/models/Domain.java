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

    public boolean isPublic;

    @Required
    @ManyToOne(optional = false)
    public User user;

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
            String domain = new URL(url).getHost();
            domain = removeWWW(domain);

            return find("domain", domain).first();
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Malformed URL: " + url);
        }
    }

    public static Set<Domain> toDomains(String publicDomains) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setDomain(final String domain) {
        this.domain = removeWWW(domain);
    }

    @Override
    public Object _key() {
        return domain;
    }

    private static String removeWWW(final String domain) {
        return (domain.startsWith("www.") ? domain.substring(4) : domain);
    }
}
