package io.sharptree.maximo.dbmanage;

import org.jdom2.Element;
import psdi.dbmanage.statement.ChangeStatement;
import psdi.dbmanage.statement.Script;
import psdi.util.StringUtility;

/**
 * DBC change statement that removes an automation script from the target system.
 *
 * @author Jason Venhuizen
 */
public class RemoveAutoScriptStatement extends ChangeStatement {

    /**
     * Creates a new instance of the RemoveAutoScriptStatement, that is initialized with the `remove_autoscript` change statement name.
     *
     * @param script      the DBC script object that contains the `remove_autoscript` change statement.
     * @param fromElement the XML script element that represents the `remove_autoscript` change statement.
     * @throws Exception thrown if an error occurs creating the RemoveAutoScriptStatement change statement.
     */
    public RemoveAutoScriptStatement(Script script, Element fromElement) throws Exception {
        super("remove_autoscript", script, fromElement);
    }

    /**
     * Returns the autoscript attribute value or "null" if not present.
     * <p>
     *
     * @see ChangeStatement#toString()
     */
    @Override
    public String toString() {
        String autoscript = getString("name");
        return "remove_autoscript " + ((autoscript != null && !autoscript.isEmpty()) ? autoscript : "<null>");
    }

    /**
     * Validates that the `remove_autoscript` change statement contains the autoscript attribute.
     * <p>
     *
     * @throws Exception thrown if the autoscript attribute has not been provided.
     * @see ChangeStatement#validate()
     */
    @Override
    public void validate() throws Exception {
        requiredValue("name");
    }

    /**
     * Removes the automation script specified in the "name" attribute.
     * The automation script removal includes removing references from the following objects.
     * <ul>
     *   <li>action</li>
     *   <li>actiongroup</li>
     *   <li>autoscript</li>
     *   <li>scriptlaunchpoint</li>
     *   <li>launchpointvars</li>
     *   <li>autoscriptvars</li>
     *   <li>autoscriptstate</li>
     *   <li>inspformscript</li>
     *   <li>oslcquery</li>
     *   <li>ososlcaction</li>
     *   <li>jsonmapping</li>
     * </ul>
     * <p>
     *
     * @throws Exception thrown if the autoscript attribute has not been provided or an error occurs executing the SQL statements.
     * @see ChangeStatement#validate()
     */
    @Override
    public void run() throws Exception {
        validate();

        String whereSql = StringUtility.replaceAll("upper(autoscript) = '<AUTOSCRIPT>' ", "<AUTOSCRIPT>", getString("name").toUpperCase());

        String scriptWhereSql = StringUtility.replaceAll("upper(scriptname) = '<AUTOSCRIPT>' ", "<AUTOSCRIPT>", getString("name").toUpperCase());

        String actionWhereSql = StringUtility.replaceAll("upper(action) in (select launchpointname from scriptlaunchpoint where LAUNCHPOINTTYPE = 'ACTION' AND upper(autoscript) = '<AUTOSCRIPT>') and value = 'com.ibm.tivoli.maximo.script.ScriptAction'", "<AUTOSCRIPT>", getString("name").toUpperCase());
        String actionGrpWhereSql = StringUtility.replaceAll("upper(member) in (SELECT action FROM ACTION WHERE upper(action) in (select launchpointname from scriptlaunchpoint where LAUNCHPOINTTYPE = 'ACTION' AND upper(autoscript) = '<AUTOSCRIPT>') and value = 'com.ibm.tivoli.maximo.script.ScriptAction')", "<AUTOSCRIPT>", getString("name").toUpperCase());

        doSql("delete from actiongroup where " + actionGrpWhereSql);
        doSql("delete from actiongroup where " + actionGrpWhereSql);
        doSql("delete from action where " + actionWhereSql);

        doSql("delete from autoscript where " + whereSql);
        doSql("delete from scriptlaunchpoint where " + whereSql);
        doSql("delete from launchpointvars where " + whereSql);
        doSql("delete from autoscriptvars where " + whereSql);
        doSql("delete from autoscriptstate where " + whereSql);
        doSql("delete from inspformscript where " + whereSql);

        doSql("delete from oslcquery where " + scriptWhereSql);
        // no direct link from autoscript to ososlcaction, for now we will keep sigoption
        // doSql("delete from ososlcaction where " + scriptWhereSql);
        doSql("delete from jsonmapping where " + scriptWhereSql);


    }

}
