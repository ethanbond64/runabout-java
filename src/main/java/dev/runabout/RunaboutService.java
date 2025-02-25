package dev.runabout;

import dev.runabout.annotations.Nullable;

/**
 * Runabout interface for converting runtime java objects to JSON outputs that can be
 * used for replay debugging in <a href="https://www.runabout.dev">Runabout</a>.
 * The service has a generic type parameter that allows for the user to specify the type of JSON object to be used.
 * By default, the service uses the built-in lightweight {@link JsonObject} interface. To use a different type, use
 * {@link RunaboutServiceBuilder} to create a custom service.
 */
public interface RunaboutService {

    /**
     * Gets the default RunaboutService which uses the built-in {@link JsonObject} type.
     *
     * @param projectName The name of the project to log scenarios under.
     * @param apiToken    The API token for the Runabout project.
     * @return The default implementation of RunaboutService.
     */
    static RunaboutService getService(final String projectName, final String apiToken) {
        final RunaboutApi runaboutApi = new RunaboutApiBuilder(() -> apiToken).build();
        return new RunaboutServiceBuilder(projectName)
                .setRunaboutApi(runaboutApi)
                .build();
    }

    /**
     * Converts an object to a RunaboutInput.
     *
     * @param object The object to serialize.
     * @return A RunaboutInput representing the serialized object.
     */
    RunaboutInput serialize(final Object object);

    /**
     * Converts a method and the given objects to a Runabout JSON object.
     * This JSON format is known by Runabout and can be parsed by the Runabout web application and IDE plugin.
     * The format is as follows:
     * <code>
     * {
     * <br>    "version": "0.0.0", // The version of the Runabout JSON format.
     * <br>    "caller": "com.example.ClassName.methodName", // The caller method, from which the objects are arguments.
     * <br>    "instances": [] // The runabout instances (as JSON) from the objects passed in.
     * <br>}
     * </code>
     * <br>
     * The format of the instances is as follows:
     * <code>
     * {
     * <br>    "type": "com.example.Value", // The fully qualified class of the instance.
     * <br>    "value": "new Value()" // A String which is a Java expression, which evaluates to an object.
     * <br>    "dependencies": [] // The fully qualified class names for all dependencies of the instance.
     * <br>}
     * </code>
     * @param eventId    Nullable String eventId for tracking scenarios that occurred in the same request.
     * @param properties Nullable JsonObject contextual data for adding additional info to scenarios.
     * @param objects    The objects to convert to Runabout inputs in JSON.
     * @return A JSON object.
     */
    RunaboutScenario createScenario(@Nullable final String eventId, @Nullable final JsonObject properties,
                                    final Object... objects);

    /**
     * Emit a scenario with eventId and contextual data to the runabout ingest API.
     * This method is intended to be non-blocking and implementations should enqueue the data
     * to be sent on another thread.
     *
     * @param eventId    Nullable String eventId for tracking scenarios that occurred in the same request.
     * @param properties Nullable JsonObject contextual data for adding additional info to scenarios.
     * @param objects    Objects to convert to Runabout instances for the scenario.
     */
     void saveScenario(final String eventId, final JsonObject properties, final Object... objects);
}
