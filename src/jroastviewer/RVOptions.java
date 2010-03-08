/*
 * RVOptions.java
 *
 * Created on 10 Март 2007 г., 23:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jroastviewer;

import java.util.prefs.Preferences;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.swing.JTextArea;

/**
 *
 * @author Leo
 */
public class RVOptions {
    
    /** Creates a new instance of RVOptions */
    public RVOptions() {
        loadOptions();
        initSQL();
    }
    
    void              initSQL() 
    {
        try {
                Class.forName(dbDriver);

	} catch(java.lang.ClassNotFoundException e) {
			err("SQLDriver ClassNotFoundException: for driver " + dbDriver);
			err(e.getMessage());
	} 
    }
    
    synchronized Connection        getDBC() throws SQLException
    {
        Connection con = null;
        if(sqlcon != null)
        {
            con = sqlcon;
            sqlcon = null;
        }
        else
            con = DriverManager.getConnection(dbName, dbUser, dbPasswd);
        return con;
    }
    
    synchronized void              retDBC(Connection con)
    {
        sqlcon = con;
    }
    
    void              loadOptions()
    {
        dbDriver = pref.get("dbDriver", "net.sourceforge.jtds.jdbc.Driver");
        dbName = pref.get("dbName", "jdbc:jtds:sqlserver://192.168.1.130/test");
        dbUser = pref.get("dbUser","rst");
        dbPasswd = pref.get("dbPasswd", "rst");
    }
    
    void              saveOptions()
    {
        pref.put("dbDriver", dbDriver);
        pref.put("dbName", dbName);
        pref.put("dbUser", dbUser);
        pref.put("dbPasswd", dbPasswd);
    }
    
    private static final RVOptions gOpt = new RVOptions();
    
    public static RVOptions getOpt()
    {
        return gOpt;
    };

    public Preferences getPref() { return pref;}
    
    private Preferences pref = Preferences.userNodeForPackage(RVOptions.class);

    private Connection  sqlcon = null;
    private String  dbDriver="net.sourceforge.jtds.jdbc.Driver";
    private String  dbName="jdbc:jtds:sqlserver://192.168.1.130/test";
    private String  dbUser="rst";
    private String  dbPasswd="rst";
    
    String  getDbDriver() { return dbDriver;};
    void    setDbDriver(String newName) { dbDriver = newName; saveOptions();};

    
    String  getDbName() { return dbName;};
    void    setDbName(String newName) { dbName = newName; saveOptions();};
    
    String  getDbUser() { return dbUser;};
    void    setDbUser(String newName) { dbUser = newName; saveOptions();};
    
    String  getDbPasswd() { return dbPasswd;};
    void    setDbPasswd(String newName) { dbPasswd = newName; saveOptions();};
    
    private            JTextArea dbgOut = null;
    
    void               setDbgOut(JTextArea out)
    { dbgOut = out;}
    
    synchronized void               dbg(final String str)
    {
        if(dbgOut != null)
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    String s = new SimpleDateFormat("HH:mm:ss: ").format(new java.util.Date());
                    dbgOut.append(s + str + "\n");
                }
            });
        }
        else
            System.out.println(str);
    }
    
    synchronized void               err(final String str)
    {
        if(dbgOut != null)
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    String s = new SimpleDateFormat("HH:mm:ss: ").format(new java.util.Date());
                    dbgOut.append(s + "ERROR: " + str + "\n");
                }
            });
        }
        else
            System.err.println(str);
    }
    
}
