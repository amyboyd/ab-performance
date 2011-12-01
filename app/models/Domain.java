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
public class Domain extends Model {
    /**
     * Public domains must be unique, and this is enforced in {@link #createAll(java.util.Set)}.
     * Private domains do not have to be unique, because "localhost" etc is a common private domain.
     */
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

    public static Set<Domain> toPublicDomains(String domains, User user) {
        return toDomains(domains, user, true);
    }

    public static Set<Domain> toPrivateDomains(String domains, User user) {
        return toDomains(domains, user, false);
    }

    private static Set<Domain> toDomains(String domains, User user, boolean isPublic) {
        Set<Domain> domainsSet = new HashSet<Domain>(5);
        for (String domain: domains.split("[\n, ]")) {
            domain = domain.trim();
            domain = removeWWW(domain);

            if (domain.isEmpty()) {
                continue;
            } else {
                domainsSet.add(new Domain(domain, isPublic, user));
            }
        }
        return domainsSet;
    }

    private static String removeWWW(final String domain) {
        return (domain.startsWith("www.") ? domain.substring(4) : domain);
    }

    public static void createAll(Set<Domain> domains) {
        for (Domain domain: domains) {
            // Public domains must be unique. Private domains do not have to be unique.
            if (!domain.isPublic || Domain.count("domain = ? and isPublic = true", domain.domain) == 0L) {
                domain.create();
            }
        }
    }

    public Domain(String domain, boolean isPublic, User user) {
        this.domain = domain;
        this.isPublic = isPublic;
        this.user = user;
    }

    public void setDomain(final String domain) {
        this.domain = removeWWW(domain);
    }
}
