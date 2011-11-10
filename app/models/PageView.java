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
    public User account;

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

    public PageView() {
        testsJSON = new JsonObject();
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
