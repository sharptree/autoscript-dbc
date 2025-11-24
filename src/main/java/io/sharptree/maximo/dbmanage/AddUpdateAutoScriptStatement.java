package io.sharptree.maximo.dbmanage;

import com.google.gson.Gson;
import com.ibm.tivoli.maximo.dbmanage.MXExceptionWithDefault;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import psdi.dbmanage.statement.ChangeStatement;
import psdi.dbmanage.statement.InsertSql;
import psdi.dbmanage.statement.Script;
import psdi.util.StringUtility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.sql.PreparedStatement;

import java.util.Stack;
import java.util.stream.Collectors;

/**
 * DBC change statement that adds or updates an automation script in the target system.
 *
 * @author Jason Venhuizen
 */
public class AddUpdateAutoScriptStatement extends ChangeStatement {

    private File scriptFile = null;

    /**
     * Creates a new instance of the AddUpdateAutoScriptStatement, that is initialized with the `add_update_autoscript` change statement name.
     *
     * @param script      the DBC script object that contains the `add_update_autoscript` change statement.
     * @param fromElement the XML script element that represents the `add_update_autoscript` change statement.
     * @throws Exception thrown if an error occurs creating the AddUpdateAutoScriptStatement change statement.
     */
    public AddUpdateAutoScriptStatement(Script script, Element fromElement) throws Exception {
        super("add_update_autoscript", script, fromElement);
    }

    /**
     * Returns the path attribute value or "null" if not present.
     * <p>
     * {@inerhitDoc}
     *
     * @see ChangeStatement#toString()
     */
    @Override
    public String toString() {
        String path = getString("path");
        return "add_update_autoscript from " + ((path != null && !path.isEmpty()) ? path : "<null>");
    }

    /**
     * Validates that the `add_update_autoscript` change statement contains the path attribute and that path can be resolved to a file reference.
     * <p>
     * {@inerhitDoc}
     *
     * @throws Exception thrown if the path attribute has not been provided or the path cannot be resolved to a file.
     * @see ChangeStatement#validate()
     */
    @Override
    public void validate() throws Exception {
        requiredValue("path");

        String path = getString("path");

        File checkFile = new File(path);

        if (checkFile.exists()) {
            scriptFile = checkFile;
        } else {
            checkFile = new File(getScriptRun().getOutputDirectory() + File.separator + path);
            if (checkFile.exists()) {
                scriptFile = checkFile;
            }
        }

        if (scriptFile == null) {
            throw new Exception("The automation script file " + path + " cannot be found.");
        }
    }


    /**
     * Load the automation source from the specified path, parse the scriptConfig variable and then create or replace the script based on the script configuration provided.
     * <p>
     * {@inerhitDoc}
     *
     * @throws Exception thrown if the path attribute has not been provided or if there are issues loading the script file or parsing the scriptConfig.
     * @see ChangeStatement#validate()
     */
    @Override
    public void run() throws Exception {

        validate();
        String source = getSourceFromFile(scriptFile);
        AutoscriptConfig config = getConfigFromScript(source);

        // validate that the script configuration has the required values.
        config.validate();

        // remove the automation script if it exists.
        removeAutoscriptIfExists(config.autoscript);

        createScript(config, source, getString("language"));

    }

    private void createScript(AutoscriptConfig config, String source, String scriptLanguage) throws Exception {

        InsertSql is = new InsertSql("AUTOSCRIPT", getConnection());

        String autoscript = config.autoscript.toUpperCase();

        is.addColumnStringValue("autoscript", autoscript);
        is.addColumnStringValue("description", config.description);
        is.addColumnStringValue("version", config.version);
        is.addColumnStringValue("loglevel", config.logLevel);
        is.addColumnStringValue("status", "Active");
        is.addColumnValue("active", config.active);
        is.addColumnStringValue("source", "1=1");
        is.addColumnValue("createddate", "sysdate");
        is.addColumnValue("statusdate", "sysdate");
        is.addColumnValue("changedate", "sysdate");
        is.addColumnStringValue("owner", "MAXADMIN");
        is.addColumnStringValue("createdby", "MAXADMIN");
        is.addColumnStringValue("changeby", "MAXADMIN");
        is.addColumnStringValue("scriptlanguage", scriptLanguage);
        is.addColumnValue("userdefined", true);
        is.addColumnValue("hasld", false);
        is.addColumnStringValue("langcode", "EN");


        boolean isInterface = autoscript.startsWith("OSOUT.") ||
                autoscript.startsWith("OSIN.") ||
                autoscript.startsWith("OSQUERY.") ||
                autoscript.startsWith("OSACTION.") ||
                autoscript.startsWith("PUBLISH.") ||
                autoscript.startsWith("SYNC.") ||
                autoscript.startsWith("INVOKE.") || config.allowInvokingScriptFunctions;

        is.addColumnValue("interface", isInterface);

        doSql(is.generateInsertSql());


        is = new InsertSql("AUTOSCRIPTSTATE", getConnection());
        is.addColumnStringValue("autoscript", autoscript);
        is.addColumnStringValue("changeby", "MAXADMIN");
        is.addColumnValue("changedate", "sysdate");
        is.addColumnStringValue("status", "Active");

        doSql(is.generateInsertSql());


        Clob c = getConnection().createClob();
        c.setString(1, source);
        PreparedStatement s = getConnection().prepareStatement("update autoscript set source = ? where autoscript = ?");
        s.setClob(1, c);
        s.setString(2, autoscript);

        s.executeUpdate();
        s.close();

        if (config.autoScriptVars != null && !config.autoScriptVars.isEmpty()) {
            for (AutoscriptVar autoScriptVar : config.autoScriptVars) {
                createAutoscriptVar(autoscript, autoScriptVar);
            }
        }

        if (config.scriptLaunchPoints != null && !config.scriptLaunchPoints.isEmpty()) {
            for (ScriptLaunchPoint scriptLaunchPoint : config.scriptLaunchPoints) {
                createScriptLaunchPoint(autoscript, scriptLaunchPoint);
            }
        }

    }

    private void createAutoscriptVar(String autoscript, AutoscriptVar autoscriptVar) throws Exception {
        InsertSql is = new InsertSql("AUTOSCRIPTVARS", getConnection());

        is.addColumnStringValue("autoscript", autoscript);
        is.addColumnStringValue("varname", autoscriptVar.varname);
        is.addColumnStringValue("varbindingvalue", autoscriptVar.varBindingValue);
        is.addColumnStringValue("varbindingtype", autoscriptVar.varBindingType);
        is.addColumnStringValue("vartype", autoscriptVar.varType);
        is.addColumnStringValue("description", autoscriptVar.description);
        is.addColumnValue("allowoverride", autoscriptVar.allowOverride);
        is.addColumnStringValue("literaldatatype", autoscriptVar.literalDataType);

        if (!autoscriptVar.noValidation && !autoscriptVar.noAccessCheck && !autoscriptVar.noAction) {
            is.addColumnValue("accessflag", 0);
        } else if (autoscriptVar.noValidation && autoscriptVar.noAccessCheck && autoscriptVar.noAction) {
            is.addColumnValue("accessflag", 11);
        } else if (autoscriptVar.noValidation && !autoscriptVar.noAccessCheck && !autoscriptVar.noAction) {
            is.addColumnValue("accessflag", 1);
        } else if (!autoscriptVar.noValidation && autoscriptVar.noAccessCheck && !autoscriptVar.noAction) {
            is.addColumnValue("accessflag", 2);
        } else if (autoscriptVar.noValidation && autoscriptVar.noAccessCheck) {
            is.addColumnValue("accessflag", 3);
        } else if (!autoscriptVar.noValidation && !autoscriptVar.noAccessCheck) {
            is.addColumnValue("accessflag", 8);
        } else if (!autoscriptVar.noValidation) {
            is.addColumnValue("accessflag", 10);
        } else {
            is.addColumnValue("accessflag", 9);
        }

        doSql(is.generateInsertSql());
    }

    private void createScriptLaunchPoint(String autoscript, ScriptLaunchPoint scriptLaunchPoint) throws Exception {
        InsertSql is = new InsertSql("SCRIPTLAUNCHPOINT", getConnection());

        String launchType = scriptLaunchPoint.launchPointType;

        is.addColumnStringValue("launchpointname", scriptLaunchPoint.launchPointName);
        is.addColumnStringValue("autoscript", autoscript);
        is.addColumnStringValue("description", scriptLaunchPoint.description);
        is.addColumnStringValue("launchpointtype", launchType);
        is.addColumnStringValue("objectname", scriptLaunchPoint.objectName);
        is.addColumnStringValue("attributename", scriptLaunchPoint.attributeName);
        is.addColumnStringValue("condition", scriptLaunchPoint.condition);
        is.addColumnValue("active", scriptLaunchPoint.active);

        if (launchType.equalsIgnoreCase("OBJECT")) {
            if (scriptLaunchPoint.initializeValue) {
                is.addColumnValue("objectevent", 1);
            } else if (scriptLaunchPoint.validateApplication) {
                is.addColumnValue("objectevent", 1024);
            } else if (scriptLaunchPoint.allowObjectCreation) {
                is.addColumnValue("objectevent", 2048);
            } else if (scriptLaunchPoint.allowObjectDeletion) {
                is.addColumnValue("objectevent", 4096);
            } else if (scriptLaunchPoint.save) {
                if (scriptLaunchPoint.beforeSave) {
                    if (scriptLaunchPoint.add && !scriptLaunchPoint.update && !scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 2);
                    } else if (scriptLaunchPoint.add && scriptLaunchPoint.update && !scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 6);
                    } else if (scriptLaunchPoint.add && scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 14);
                    } else if (!scriptLaunchPoint.add && scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 12);
                    } else if (!scriptLaunchPoint.add && !scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 8);
                    } else if (!scriptLaunchPoint.add && scriptLaunchPoint.update && !scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 4);
                    } else if (scriptLaunchPoint.add && !scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 10);
                    }
                } else if (scriptLaunchPoint.afterSave) {
                    if (scriptLaunchPoint.add && !scriptLaunchPoint.update && !scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 16);
                    } else if (scriptLaunchPoint.add && scriptLaunchPoint.update && !scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 48);
                    } else if (scriptLaunchPoint.add && scriptLaunchPoint.update) {
                        is.addColumnValue("objectevent", 112);
                    } else if (!scriptLaunchPoint.add && scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 96);
                    } else if (!scriptLaunchPoint.add && !scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 64);
                    } else if (!scriptLaunchPoint.add && scriptLaunchPoint.update) {
                        is.addColumnValue("objectevent", 32);
                    } else if (scriptLaunchPoint.add) {
                        is.addColumnValue("objectevent", 80);
                    }
                } else if (scriptLaunchPoint.afterCommit) {
                    if (scriptLaunchPoint.add && !scriptLaunchPoint.update && !scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 128);
                    } else if (scriptLaunchPoint.add && scriptLaunchPoint.update && !scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 384);
                    } else if (scriptLaunchPoint.add && scriptLaunchPoint.update) {
                        is.addColumnValue("objectevent", 896);
                    } else if (!scriptLaunchPoint.add && scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 768);
                    } else if (!scriptLaunchPoint.add && !scriptLaunchPoint.update && scriptLaunchPoint.delete) {
                        is.addColumnValue("objectevent", 512);
                    } else if (!scriptLaunchPoint.add && scriptLaunchPoint.update) {
                        is.addColumnValue("objectevent", 256);
                    } else if (scriptLaunchPoint.add) {
                        is.addColumnValue("objectevent", 640);
                    }
                }
            }
        } else if (launchType.equalsIgnoreCase("ATTRIBUTE")) {
            if (scriptLaunchPoint.initializeAccessRestriction) {
                is.addColumnValue("objectevent", 8);
            } else if (scriptLaunchPoint.initializeValue) {
                is.addColumnValue("objectevent", 2);
            } else if (scriptLaunchPoint.validate) {
                is.addColumnValue("objectevent", 0);
            } else if (scriptLaunchPoint.retrieveList) {
                is.addColumnValue("objectevent", 64);
            } else if (scriptLaunchPoint.runAction) {
                is.addColumnValue("objectevent", 1);
            }
        }

        doSql(is.generateInsertSql());

        if (launchType.equalsIgnoreCase("ACTION")) {
            is = new InsertSql("ACTION", getConnection());
            is.addColumnStringValue("action", scriptLaunchPoint.launchPointName);
            is.addColumnStringValue("objectname", scriptLaunchPoint.objectName);
            is.addColumnStringValue("type", "CUSTOM");
            is.addColumnStringValue("value", "com.ibm.tivoli.maximo.script.ScriptAction");
            is.addColumnStringValue("parameter", autoscript + "," + scriptLaunchPoint.launchPointName + "," + scriptLaunchPoint.actionName);
            is.addColumnStringValue("langcode", "EN");
            is.addColumnStringValue("usewith", "ALL");
            is.addColumnValue("hasld", false);
            doSql(is.generateInsertSql());
        }
        // in case of script for custom condition, we met errors when there is no lauchpointvars provided
        if(scriptLaunchPoint.launchPointVars != null){
            for (LaunchPointVar launchPointVar : scriptLaunchPoint.launchPointVars) {
                createLaunchPointVar(autoscript, scriptLaunchPoint.launchPointName, launchPointVar);
            }
        }
    }

    private void createLaunchPointVar(String autoscript, String launchPointName, LaunchPointVar launchPointVar) throws Exception {
        InsertSql is = new InsertSql("LAUNCHPOINTVARS", getConnection());
        is.addColumnStringValue("launchpointname", launchPointName);
        is.addColumnStringValue("autoscript", autoscript);
        is.addColumnStringValue("varname", launchPointVar.varName);
        is.addColumnStringValue("varbindingvalue", launchPointVar.varBindingValue);
        doSql(is.generateInsertSql());
    }

    private void removeAutoscriptIfExists(String autoscript) throws Exception {

        String whereSql = StringUtility.replaceAll("upper(autoscript) = '<AUTOSCRIPT>' ", "<AUTOSCRIPT>", autoscript.toUpperCase());
        String actionWhereSql = StringUtility.replaceAll("upper(action) in (select launchpointname from scriptlaunchpoint where autoscript = '<AUTOSCRIPT>') and value = 'com.ibm.tivoli.maximo.script.ScriptAction'", "<AUTOSCRIPT>", autoscript.toUpperCase());

        doSql("delete from action where " + actionWhereSql);

        doSql("delete from autoscript where " + whereSql);
        doSql("delete from scriptlaunchpoint where " + whereSql);
        doSql("delete from launchpointvars where " + whereSql);
        doSql("delete from autoscriptvars where " + whereSql);
        doSql("delete from autoscriptstate where " + whereSql);

    }

    private String getSourceFromFile(File scriptFile) throws Exception {
        if (scriptFile == null || !scriptFile.exists()) {
            throw new Exception("The specified script file is either null or does not exist.");
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(scriptFile, StandardCharsets.UTF_8))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }

    private AutoscriptConfig getConfigFromScript(String source) throws Exception {

        if (source == null || source.isEmpty()) {
            throw new Exception("The source cannot be null or empty; the script cannot be processed.");
        }

        StringBuilder wordBuffer = new StringBuilder();
        StringBuilder config = new StringBuilder();

        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);

            if (c == '\n' || c == '\r') {
                wordBuffer.setLength(0);
            } else {
                wordBuffer.append(c);

                if ((wordBuffer.toString().trim().startsWith("var") && wordBuffer.toString().endsWith("scriptConfig")) || wordBuffer.toString().trim().equals("scriptConfig") ) {
                    boolean startFound = false;

                    while (i < source.length() && !startFound) {
                        startFound = source.charAt(i++) == '{';
                    }
                    if (startFound) {
                        Stack<Character> stack = new Stack<>();
                        stack.push('{');
                        config.append("{");
                        while (i < source.length() && !stack.empty()) {
                            c = source.charAt(i++);
                            config.append(c);
                            if (c == '{') {
                                stack.push('{');
                            } else if (c == '}') {
                                stack.pop();
                            }
                        }

                        if (stack.empty()) {
                            break;
                        } else {
                            throw new Exception("The scriptConfig JSON was not properly formatted; missing a closing } character.");
                        }
                    } else {
                        throw new Exception("The scriptConfig JSON was not property formatted; missing a starting { character.");
                    }
                }
            }
        }

        if (config.length() == 0) {
            throw new Exception("The scriptConfig variable was not declared in the automation script source; the script cannot be processed.");
        }

        Logger.getLogger("statement").info(new MXExceptionWithDefault("scriptrun", "PrintConfig", "Parsing the script config\n {0}", config.toString()));

        return new Gson().fromJson(config.toString(), AutoscriptConfig.class);
    }

}
