package models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
     * These do not have to be unique, because "localhost" etc is a common private domain.
     */
    @Required
    @Match(value = "([a-zA-Z0-9-]+\\.)+[a-zA-Z]+", message = "That isn't a valid domain format")
    public String domain;

    @Enumerated(EnumType.STRING)
    public DomainAccess access;

    @Required
    @ManyToOne(optional = false)
    public Project project;

    @Required
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public static Domain findByProjectAndURL(final Project project, final String url) {
        try {
            String domain = new URL(url).getHost();
            domain = removeWWW(domain);

            return find("project = ? and domain = ?", project, domain).first();
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Malformed URL: " + url, ex);
        }
    }

    public static void deleteAll(final Project project) {
        delete("project = ?", project);
    }

    public static void createAll(final String domains, final Project project, final DomainAccess access) {
        for (final Domain domain: toDomains(domains, project, access)) {
            domain.create();
        }
    }

    private static Set<Domain> toDomains(final String domains, final Project project, final DomainAccess access) {
        final Set<Domain> domainsSet = new HashSet<Domain>(5);
        for (String domain: domains.split("[\n, ]")) {
            domain = domain.trim();
            domain = removeWWW(domain);

            if (domain.isEmpty()) {
                continue;
            } else {
                domainsSet.add(new Domain(domain, access, project));
            }
        }
        return domainsSet;
    }

    private static String removeWWW(final String domain) {
        return (domain.startsWith("www.") ? domain.substring(4) : domain);
    }

    public Domain(final String domain, final DomainAccess access, final Project project) {
        this.domain = domain;
        this.access = access;
        this.project = project;
    }

    public void setDomain(final String domain) {
        this.domain = removeWWW(domain);
    }

    public JsonObject toJSONobject() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("domain", domain);
        return json;
    }

    public static JsonArray toJSONarray(Collection<Domain> domains) {
        JsonArray json = new JsonArray();
        for (Domain aDomain: domains) {
            json.add(aDomain.toJSONobject());
        }
        return json;
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    public enum DomainAccess {
        PUBLIC,
        PRIVATE;

    }
}
