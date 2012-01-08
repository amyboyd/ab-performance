package controllers;

import models.*;

public class Application extends BaseController {
    public static void index() {
        render();
    }

    public static void features() {
        render();
    }

    public static void demo() {
        render();
    }

    public static void testCSS(final String id) {
        final TestCSS t = TestCSS.findByTestId(id);
        renderText(t.css);
    }

    public static void terms() {
        render();
    }

    public static void privacy() {
        render();
    }
}
