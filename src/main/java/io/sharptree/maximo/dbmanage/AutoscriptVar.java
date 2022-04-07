package io.sharptree.maximo.dbmanage;

/**
 * Java object that maps to the autoScriptVars JSON value in the scriptConfig from the automation script.
 *
 * @author Jason VenHuizen
 */
public class AutoscriptVar {
    /**
     * The variable name.
     */
    public String varname;

    /**
     * The variable description.
     */
    public String description;

    /**
     * The variable binding type.
     */
    public String varBindingType;

    /**
     * The variable Maximo type.
     */
    public String literalDataType;

    /**
     * The variable binding value.
     */
    public String varBindingValue;

    /**
     * The variable type.
     */
    public String varType;

    /**
     * Does the variable allow overrides.
     */
    public boolean allowOverride;

    /**
     * Does the variable ignore validation.
     */
    public boolean noValidation;

    /**
     * Does the variable ignore read only.
     */
    public boolean noAccessCheck;

    /**
     * Does the variable ignore actions.
     */
    public boolean noAction;

    /**
     * Validate the autoScriptVar to perform basic sanity checks for the provided values.
     *
     * @throws Exception thrown if a validation error occurs.
     */
    public void validate() throws Exception {
        if (varname == null || varname.isEmpty()) {
            throwMissingAttributeException("varname");
        }
        if (varType == null || varType.isEmpty()) {
            throwMissingAttributeException("varType");
        }
        if (varBindingType == null || varBindingType.isEmpty()) {
            throwMissingAttributeException("varBindingType");
        }

        if (varBindingType.equalsIgnoreCase("LITERAL")) {
            if (literalDataType == null || literalDataType.isEmpty()) {
                throwMissingAttributeException("literalDataType");
            }
        }

        if (varBindingType.equalsIgnoreCase("MAXVAR") || varBindingType.equalsIgnoreCase("LITERAL") || varBindingType.equalsIgnoreCase("SYSPROP")) {
            if (varBindingValue == null || varBindingValue.isEmpty()) {
                throwMissingAttributeException("varBindingValue");
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
        throw new Exception("scriptConfig.autoscriptvars must provide the " + attribute + " attribute.");
    }
}
