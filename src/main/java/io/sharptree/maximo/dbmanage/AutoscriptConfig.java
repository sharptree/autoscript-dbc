package io.sharptree.maximo.dbmanage;

import java.util.List;

/**
 * Java object that maps to the scriptConfig JSON value in the automation script.
 *
 * @author Jason VenHuizen
 */
public class AutoscriptConfig {
    /**
     * The automation script name.
     */
    public String autoscript;
    /**
     * The description of the automation script.
     */
    public String description;

    /**
     * The automation script version, defaults to 1.0.0.
     */
    public String version = "1.0.0";
    /**
     * Flag to indicate that the automation script is active or not, defaults to true.
     */
    public boolean active = true;

    /**
     * The automation script log level defaults to INFO.
     */
    public String logLevel = "INFO";

    /**
     * A list of automation script variables.
     */
    public List<AutoscriptVar> autoScriptVars;

    /**
     * A list of automation script launch points.
     */
    public List<ScriptLaunchPoint> scriptLaunchPoints;

    /**
     * Flag to indicate whether invoking script functions is allowed.
     */
    public boolean allowInvokingScriptFunctions = false;

    /**
     * Validate the script configuration to perform basic sanity checks for the provided values.
     *
     * @throws Exception thrown if a validation error occurs.
     */
    public void validate() throws Exception {
        if (autoscript == null || autoscript.isEmpty()) {
            throwMissingAttributeException("autoscript");
        }

        if (autoScriptVars != null && !autoScriptVars.isEmpty()) {
            for (AutoscriptVar autoscriptVar : autoScriptVars) {
                autoscriptVar.validate();
            }
        }

        if (scriptLaunchPoints != null && !scriptLaunchPoints.isEmpty()) {
            for (ScriptLaunchPoint scriptLaunchPoint : scriptLaunchPoints) {
                scriptLaunchPoint.validate();
            }
        }

    }

    /**
     * Creates and throws a new Exception if an attribute is missing.
     *
     * @param attribute the missing attribute name.
     * @throws Exception throws an exception stating that the attribute is missing.
     */
    private void throwMissingAttributeException(String attribute) throws Exception {
        throw new Exception("scriptConfig must provide the " + attribute + " attribute.");
    }
}




