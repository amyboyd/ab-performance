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
    public User user;

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

    private String tests;

    @Transient
    public JsonObject testsJSON;

    @Transient
    public String[] testIDsWithUnknownCSS;

    public PageView(final User user, final String guid, final long time, final String url,
            final Map<String, String> tests) {
        this.user = user;
        this.url = url;
        this.time = new Date(time);
        this.guid = guid;

        this.testsJSON = new JsonObject();
        this.testIDsWithUnknownCSS = new String[tests.size()];
        int i = 0;

        for (final String testGroupName: tests.keySet()) {
            final String testId = tests.get(testGroupName);

            testsJSON.addProperty(testGroupName, testId);

            // Check if we already know the CSS for this ID. If we don't know the CSS, request it.
            if (!TestCSS.cssIsKnown(testId)) {
                testIDsWithUnknownCSS[i] = testId;
                i++;
            }
        }
    }

    @PrePersist
    @PreUpdate
    protected void setTestsStringFromJSON() {
        tests = testsJSON.toString();
    }

    @PostLoad
    protected void setTestsJSONfromString() {
        testsJSON = new JsonParser().parse(tests).getAsJsonObject();
    }
}
