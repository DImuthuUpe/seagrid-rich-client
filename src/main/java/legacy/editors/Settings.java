package legacy.editors;

import java.io.File;

public class Settings {
    private static final  boolean LOCALDEBUG = false;
    public static final int ONE_SECOND = 1000;
    public static final String APP_NAME_GAUSSIAN = "GAUSSIAN";
    public static final String APP_NAME_GAMESS = "GAMES";
    public static final String APP_NAME_NWCHEM = "NWCHEM";
    public static final String APP_NAME_MOLPRO = "MOLPRO";
    private static Settings ourInstance = new Settings();
    public static boolean authenticated = true;
    public static String username = "master";
    public static String defaultDirStr = "";
    public static String fileSeparator = File.separator;
    private static String applicationDataDir = LOCALDEBUG
            ? Settings.class.getResource("/legacy.editors").getPath() : "legacy.editors";
    public static String jobDir = LOCALDEBUG
            ? Settings.class.getResource("/legacy.editors").getPath() : "legacy.editors";
    public static String httpsGateway = "https://ccg-mw1.ncsa.uiuc.edu/cgi-bin/";

    public static Settings getInstance() {
        return ourInstance;
    }

    private Settings() {
    }

    public static String getApplicationDataDir() {
        return applicationDataDir;
    }
}
