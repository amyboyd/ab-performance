package com.abperf;

/**
 * Determines which browser and version number is being used.
 */
public class UserDevice {
    public boolean ie;

    public int ieVersion;

    public boolean firefox;

    public int firefoxVersion;

    public boolean chrome;

    public int chromeVersion;

    public boolean safari;

    public int safariVersion;

    public boolean opera;

    public int operaVersion;

    public UserDevice(final String userAgent) {
        if (userAgent.contains("MSIE")) {
            ie(userAgent);
        } else if (userAgent.contains("Chrome/")) {
            chrome(userAgent);
        } else if (userAgent.contains("Firefox/")) {
            firefox(userAgent);
        } else if (userAgent.contains("Safari/")) {
            safari(userAgent);
        } else if (userAgent.contains("Opera/")) {
            opera(userAgent);
        }
    }

    /**
     * @param userAgent Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; BOIE9;ENGB)
     */
    private void ie(final String userAgent) {
        this.ie = true;
        this.ieVersion = getVersionNumberAfterVersionPrefix(userAgent, "MSIE ");
    }

    /**
     * @param userAgent Mozilla/5.0 (Windows NT 6.0; rv:2.0.1) Gecko/20100101 Firefox/4.0.1
     */
    private void firefox(final String userAgent) {
        this.firefox = true;
        this.firefoxVersion = getVersionNumberAfterVersionPrefix(userAgent, "Firefox/");
    }

    /**
     * Google Chrome and Chromium both user "Chrome" in the user agent.
     * @param userAgent Mozilla/5.0 (Windows NT 6.0) AppleWebKit/534.24 (KHTML, like Gecko) Chrome/11.0.696.71 Safari/534.24
     */
    private void chrome(final String userAgent) {
        this.chrome = true;
        this.chromeVersion = getVersionNumberAfterVersionPrefix(userAgent, "Chrome/");
    }

    /**
     * Safari and iOS.
     * Version is after "Version/".
     * @param userAgent Mozilla/5.0 (Windows; U; Windows NT 6.1; tr-TR) AppleWebKit/533.20.25 (KHTML, like Gecko) Version/5.0.4 Safari/533.20.27
     */
    private void safari(final String userAgent) {
        this.safari = true;
        this.safariVersion = getVersionNumberAfterVersionPrefix(userAgent, "Version/");
    }

    /**
     * Version is after "Version/".
     * @param userAgent Opera/9.80 (Windows NT 6.0; U; en) Presto/2.8.99 Version/11.10
     */
    private void opera(final String userAgent) {
        this.opera = true;
        this.operaVersion = getVersionNumberAfterVersionPrefix(userAgent, "Version/");
    }

    private static int getVersionNumberAfterVersionPrefix(final String userAgent, final String afterVersionPrefix) {
        final int indexAfterPrefix = userAgent.indexOf(afterVersionPrefix) + afterVersionPrefix.length();
        final int indexBeforeDot = userAgent.indexOf('.', indexAfterPrefix);
        String version = userAgent.substring(indexAfterPrefix, indexBeforeDot);
        if (version.startsWith("/")) {
            version = version.substring(1);
        }
        if (version.endsWith("/")) {
            version = version.substring(0, version.length() - 1);
        }

        try {
            return Integer.parseInt(version);
        } catch (final NumberFormatException ex) {
            play.Logger.error("Version number format exception in user agent. Version: %s, UA: %s", version, userAgent);
            return 0;
        }
    }
}
