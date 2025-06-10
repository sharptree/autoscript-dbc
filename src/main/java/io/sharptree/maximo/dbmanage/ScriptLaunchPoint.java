package io.sharptree.maximo.dbmanage;

import java.util.List;

/**
 * Java object that maps to the scriptLaunchPoints JSON value in the scriptConfig from the automation script.
 *
 * @author Jason VenHuizen
 */
public class ScriptLaunchPoint {
    /**
     * The launch point name.
     */
    public String launchPointName;

    /**
     * The launch point description.
     */
    public String description;

    /**
     * The launch point type.
     */
    public String launchPointType;

    /**
     * Flag indicating if the launch point is active, defaults to true.
     */
    public boolean active = true;

    /**
     * The name of the object the launch point is triggered from.
     */
    public String objectName;

    /**
     * The name of the attribute the launch point is triggered from.
     */
    public String attributeName;

    /**
     * The condition for launching the script.
     */
    public String condition;

    /**
     * Flag indicating the script is launched on object initialization.
     */
    public boolean initializeValue;

    /**
     * Flag indicating the script is launched on object application validation.
     */
    public boolean validateApplication;

    /**
     * Flag indicating the script is launched on validation that an object can be created .
     */
    public boolean allowObjectCreation;

    /**
     * Flag indicating the script is launched on validation that an object can be deleted .
     */
    public boolean allowObjectDeletion;

    /**
     * Flag indicating the script is launched on save of an object.
     */
    public boolean save;

    /**
     * Flag indicating the script is launched on save of an object on add.
     */
    public boolean add;

    /**
     * Flag indicating the script is launched on save of an object on update.
     */
    public boolean update;

    /**
     * Flag indicating the script is launched on save of an object on delete.
     */
    public boolean delete;

    /**
     * Flag indicating the script is launched before save.
     */
    public boolean beforeSave;

    /**
     * Flag indicating the script is launched after save.
     */
    public boolean afterSave;

    /**
     * Flag indicating the script is launched after commit.
     */
    public boolean afterCommit;

    /**
     * Flag indicating the script is launched on attribute access restriction initialization.
     */
    public boolean initializeAccessRestriction;

    /**
     * Flag indicating the script is launched on attribute validation.
     */
    public boolean validate;

    /**
     * Flag indicating the script is launched on attribute retrieve list.
     */
    public boolean retrieveList;

    /**
     * Flag indicating the script is launched on attribute action.
     */
    public boolean runAction;

    /**
     * The name of the action for action launch type.
     */
    public String actionName;

    /**
     * List of launch point variables.
     */
    public List<LaunchPointVar> launchPointVars;

    /**
     * Validate the scriptLaunchPoint to perform basic sanity checks for the provided values.
     *
     * @throws Exception thrown if a validation error occurs.
     */
    public void validate() throws Exception {
        if (launchPointName == null || launchPointName.isEmpty()) {
            throwMissingAttributeException("launchPointName");
        }
        if (launchPointType == null || launchPointType.isEmpty()) {
            throwMissingAttributeException("launchPointName");
        }

        if (launchPointType.equalsIgnoreCase("OBJECT") || launchPointType.equalsIgnoreCase("ATTRIBUTE")) {
            if (objectName == null || objectName.isEmpty()) {
                throwMissingAttributeException("objectName");
            }
        }

        if (launchPointType.equalsIgnoreCase("ATTRIBUTE")) {
            if (attributeName == null || attributeName.isEmpty()) {
                throwMissingAttributeException("attributeName");
            }
        }

        if (launchPointType.equalsIgnoreCase("ACTION")) {
            if (actionName == null || actionName.isEmpty()) {
                throwMissingAttributeException("actionName");
            }
        }

        if (launchPointType.equalsIgnoreCase("OBJECT")) {
            if (!initializeValue && !validateApplication && !allowObjectDeletion && !allowObjectCreation && !save) {
                throw new Exception("For an object launch point one of the following must be true: initializeValue,validateApplication,allowObjectDeletion,allowObjectCreation or save.");
            }
            if (save) {
                if (!add && !update && !delete) {
                    throw new Exception("For an object launch point with the save action one of the follow must be true: add, update or delete");
                }
                if (!beforeSave && !afterSave && !afterCommit) {
                    throw new Exception("For an object launch point with the save action one of the follow must be true: beforeSave, afterSave or afterCommit");
                }
            }
        }

        if (launchPointType.equalsIgnoreCase("ATTRIBUTE")) {
            // fix issue-2 The autoscript dbc callout class is not able to integrate the autoscript based on the initializeValue event of attribute launchpoint
            if (!initializeAccessRestriction && !initializeValue && !validate && !retrieveList && !runAction) {
                throw new Exception("For an attribute launch point one of the follow must be true: initializeAccessRestriction, initializeValue,validate,retrieveList or runAction");
            }
        }
        if (launchPointVars != null && !launchPointVars.isEmpty()) {
            for (LaunchPointVar launchPointVar : launchPointVars) {
                launchPointVar.validate();
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
