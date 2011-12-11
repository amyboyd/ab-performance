package models;

import com.google.gson.*;
import java.util.*;
import javax.persistence.*;
import play.db.jpa.*;
import play.data.validation.*;

@Entity
@Table(name = "page_view")
public class PageView extends Model {
    @Required
    @ManyToOne(optional = false)
    public Project project;

    /**
     * A unique page view ID.
     */
    @Required
    public String guid;

    @Required
    @URL
    public String url;

    @Required
    public Date time;

    @Lob
    private String tests;

    @Transient
    public JsonObject testsJSON;

    @Lob
    private String pings;

    @Transient
    public JsonObject pingsJSON;

    @Transient
    public List<String> testIDsWithUnknownCSS;

    public static PageView findByGUID(final String guid) {
        final PageView pageView = find("guid", guid).first();
        if (pageView == null) {
            throw new RuntimeException("GUID " + guid + " is not already in the database");
        }
        return pageView;
    }

    public PageView(final Project project, final String guid, final long time, final String url,
            final Map<String, String> tests) {
        this.project = project;
        this.url = url;
        this.time = new Date(time);
        this.guid = guid;

        this.testsJSON = new JsonObject();
        this.pingsJSON = new JsonObject();
        this.testIDsWithUnknownCSS = new ArrayList<String>(tests.size());

        for (final String testGroupName: tests.keySet()) {
            final String testId = tests.get(testGroupName);

            testsJSON.addProperty(testGroupName, testId);

            // Check if we already know the CSS for this ID. If we don't know the CSS,
            // request that client.js supplies it.
            if (!TestCSS.cssIsKnown(testId)) {
                testIDsWithUnknownCSS.add(testId);
            }
        }
    }

    @PrePersist
    @PreUpdate
    public void setJSONstringsFromJSONobjects() {
        tests = testsJSON.toString();
        pings = pingsJSON.toString();
    }

    @PostLoad
    protected void setJSONobjectsFromJSONstrings() {
        final JsonParser parser = new JsonParser();
        testsJSON = parser.parse(tests).getAsJsonObject();
        pingsJSON = parser.parse(pings).getAsJsonObject();
    }
}
