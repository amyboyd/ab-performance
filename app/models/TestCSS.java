package models;

import javax.persistence.*;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.*;

/**
 * Maps a test ID to the related CSS.
 */
@Entity
@Table(name = "test_css")
public class TestCSS extends GenericModel {
    @Id
    @Required
    @MinSize(32)
    @MaxSize(32)
    public String testId;

    @Required
    public String css;

    public static boolean cssIsKnown(String testId) {
        if (testId.equals("none")) {
            return true;
        }

        String cacheKey = "cssIsKnown-" + testId;
        String isKnown = play.cache.Cache.get(cacheKey, String.class);

        if (isKnown == null) {
            TestCSS css = findByTestId(testId);
            if (css == null || css.css == null) {
                play.cache.Cache.set(cacheKey, "0");
                return false;
            } else {
                play.cache.Cache.set(cacheKey, "1");
                return true;
            }
        } else {
            return "1".equals(isKnown);
        }
    }

    public static TestCSS findByTestId(String testId) {
        idMustNotBeNone(testId);
        return find("testId", testId).first();
    }

    public TestCSS(String testId) {
        setTestId(testId);
    }

    public void setTestId(String testId) {
        idMustNotBeNone(testId);
        if (testId == null) {
            throw new IllegalArgumentException("testId must not be null");
        }
        this.testId = testId;
    }

    public void setCss(String css) {
        if (css == null) {
            throw new IllegalArgumentException("css must not be null");
        }
        this.css = css;
    }

    @Override
    public Object _key() {
        return testId;
    }

    private static void idMustNotBeNone(String testId) {
        if ("none".equals(testId)) {
            throw new IllegalArgumentException("Do not use StyleTest for ID = none");
        }
    }
}
