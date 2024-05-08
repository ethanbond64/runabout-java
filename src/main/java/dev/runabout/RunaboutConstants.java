package dev.runabout;

class RunaboutConstants {

    private RunaboutConstants() {
        // Static access only.
    }

    public static final String JSON_CONTRACT_VERSION = "1.0.0";

    public static final String VERSION_KEY = "version";
    public static final String METHOD_KEY = "method";
    public static final String TYPE_KEY = "type";
    public static final String EVAL_KEY = "eval";
    public static final String DEPENDENCIES_KEY = "dependencies";
    public static final String INPUTS_KEY = "inputs";
    public static final String DATETIME_KEY = "datetime";
    public static final String EVENT_ID_KEY = "event_id";
    public static final String PROJECT_NAME_KEY = "project_name";
    public static final String PROPERTIES_KEY = "properties";
    public static final String SCENARIO_KEY = "scenario";

    public static final String INGEST_URL = "https://api.runabout.dev/ingest";
}
