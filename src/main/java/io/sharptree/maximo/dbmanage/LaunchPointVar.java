package io.sharptree.maximo.dbmanage;

/**
 * Java object that maps to the launchPointVar JSON value in the scriptConfig from the automation script.
 *
 * @author Jason VenHuizen
 */
public class LaunchPointVar {

    /**
     * The launch point variable name.
     */
    public String varName;
    /**
     * The launch point variable binding value.
     */
    public String varBindingValue;


    /**
     * Validate the launchPointVar to perform basic sanity checks for the provided values.
     *
     * @throws Exception thrown if a validation error occurs.
     */
    public void validate() throws Exception {
        if (varName == null || varName.isEmpty()) {
            throwMissingAttributeException("varName");
        }
        if (varBindingValue == null || varBindingValue.isEmpty()) {
            throwMissingAttributeException("varBindingValue");
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
