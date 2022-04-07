package io.sharptree.maximo.dbmanage;

import org.jdom.Element;
import psdi.dbmanage.StatementFactory;
import psdi.dbmanage.statement.ChangeStatement;
import psdi.dbmanage.statement.Script;
import psdi.tools.UpdateDBCalloutBase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;

/**
 * Add the automation script change statements to the DBC command class lookup cache prior before performing the installation.
 *
 * @author Jason VenHuizen
 */
public class AutoScriptExtCallout extends UpdateDBCalloutBase {

    /**
     * Default constructor for the AutoScriptExtCallout class.
     */
    public AutoScriptExtCallout() {
        super();
    }

    /**
     * {@inerhitDoc}
     *
     * @see UpdateDBCalloutBase#beforeProductInstall(Connection)
     */
    @Override
    public boolean beforeProductInstall(Connection con) {
        try {
            Field commandClassLookupField = StatementFactory.class.getDeclaredField("commandClassLookup");
            commandClassLookupField.setAccessible(true);

            @SuppressWarnings("unchecked")
            HashMap<String, Constructor<? extends ChangeStatement>> commandClassLookup = (HashMap<String, Constructor<? extends ChangeStatement>>) commandClassLookupField.get(null);

            commandClassLookup.put("add_update_autoscript", AddUpdateAutoScriptStatement.class.getConstructor(Script.class, Element.class));
            commandClassLookup.put("remove_autoscript", RemoveAutoScriptStatement.class.getConstructor(Script.class, Element.class));

        } catch (Exception e) {
            getPrintStream().println("An error occurred registering the autoscript actions prior to installation.");
            e.printStackTrace(getPrintStream());
        }
        return super.beforeProductInstall(con);
    }
}
