package controllers;

import play.cache.Cache;
import play.libs.Images;

public class Application extends BaseController {
    public static void index() {
        render();
    }

    public static void demo() {
        render();
    }

    public static void about() {
        render();
    }

    public static void terms() {
        render();
    }

    public static void privacy() {
        render();
    }

    public static void help() {
        render();
    }

    /**
     * Display a captcha image and put the code in cache for retrieval later.
     */
    public static void captcha(final String id) {
        final Images.Captcha captcha = Images.captcha();
        final String code = captcha.getText("#000000", 5, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");

        // Don't put the captcha code in the session, because then the client can easily
        // read it. Store the captcha server side.
        Cache.set("captcha-" + id, code, "30mn");

        renderBinary(captcha);
    }
}
