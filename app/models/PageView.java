package models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.*;
import javax.persistence.*;
import play.data.validation.Required;
import play.data.validation.URL;
import play.db.jpa.Model;

@Entity
@Table(name = "page_view")
public class PageView extends Model {
    @Required
    @ManyToOne(optional = false)
    public Project project;

    @Required
    @URL
    public String url;

    @Required
    public Date time;

    @Required
    public String user;

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

    public PageView(final Project project, final long time, final String url,
            final Map<String, String> tests, final String user) {
        this.project = project;
        this.url = url;
        this.time = new Date(time);
        this.user = user;

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

    public JsonObject toJSONobject() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("url", url);
        json.addProperty("time", Long.valueOf(time.getTime()));
        json.addProperty("user", user);
        json.add("pings", pingsJSON);
        json.add("tests", testsJSON);
        return json;
    }

    public static JsonArray toJSONarray(final Collection<PageView> pvs) {
        final JsonArray json = new JsonArray();
        for (final PageView pv: pvs) {
            json.add(pv.toJSONobject());
        }
        return json;
    }
}
