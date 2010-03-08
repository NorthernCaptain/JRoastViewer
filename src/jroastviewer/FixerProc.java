/*
 * FixerProc.java
 *
 * Created on 21 Март 2007 г., 15:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jroastviewer;

import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author leo
 */
public class FixerProc
{
    int     totalRecords = 0;
    
    int     fixedRecords = 0;
    int     noloadRecords = 0;
    int     nounloadRecords = 0;
    int     noendRecords = 0;
    int     procstatRecords = 0;
    int     totalrows = 0;
    int     strippedrows = 0;
    
    Date    from, to;
    
    boolean needRowStrip = false;
    
    class   FixInfo
    {
        int     id;
        boolean     noload = false;
        boolean     nounload = false;
        boolean     noend = false;
        boolean     procStat = false;
    }
    
    Vector<FixInfo>     toFix = new Vector<FixInfo>();
    
    
    
    /** Creates a new instance of FixerProc */
    public FixerProc()
    {
    }
    
    void init(Date ifrom, Date ito)
    {
        from = ifrom;
        to = ito;
        
        String sql = "select roastID, roastLoad, roastUnload, roastEnd, roastStatus "
            + "from roast "
            + "where (roastLoad is null or roastUnload is null or roastEnd is null or roastStatus = 'processing') "
            + " and roastStart>='";
        sql += new SimpleDateFormat("dd-MM-yyyy 00:00").format(from) + "' and roastStart<='";
        sql += new SimpleDateFormat("dd-MM-yyyy 00:00").format(to) + "' order by 1";
        
        toFix.clear();
        
        noloadRecords = nounloadRecords = noendRecords = procstatRecords = 0;
        try
        {
            Connection con = RVOptions.getOpt().getDBC();
            Statement sta = con.createStatement();
            ResultSet rs = sta.executeQuery(sql);
            
            while(rs.next())
            {
                FixInfo nfo = new FixInfo();
                nfo.id = rs.getInt("roastID");
                
                if(rs.getDate("roastLoad")==null)
                {
                    nfo.noload = true;
                    noloadRecords++;
                }
                if(rs.getDate("roastUnload")==null)
                {
                    nfo.nounload = true;
                    nounloadRecords++;
                }
                if(rs.getDate("roastEnd")==null)
                {
                    nfo.noend = true;
                    noendRecords++;
                }
                if(rs.getString("roastStatus").equals("processing"))
                {
                    nfo.procStat = true;
                    procstatRecords++;
                }
                toFix.add(nfo);
            }
            
            sql = "select count(roastID) totals "
            + "from roast "
            + "where roastStart>='";
            sql += new SimpleDateFormat("dd-MM-yyyy 00:00").format(from) + "' and roastStart<='";
            sql += new SimpleDateFormat("dd-MM-yyyy 23:59:59").format(to) + "'";
            
            rs = sta.executeQuery(sql);
            if(rs.next())
                totalRecords = rs.getInt("totals");
            
            
            sql = "select count(*) totals "
            + "from roastDet "
            + "where roastDetRowDate>='";
            sql += new SimpleDateFormat("dd-MM-yyyy 00:00").format(from) + "' and roastDetRowDate<='";
            sql += new SimpleDateFormat("dd-MM-yyyy 23:59:59").format(to) + "'";
            
            rs = sta.executeQuery(sql);
            if(rs.next())
                totalrows = rs.getInt("totals");

            RVOptions.getOpt().retDBC(con);
        }
        catch(SQLException e)
        {
            RVOptions.getOpt().err("Can't read fixer info, sql: " + sql);
            RVOptions.getOpt().err(e.getMessage());
        }

    }
    
    int  getNumberIterations()
    {
        return (toFix.size() + (needRowStrip ? totalRecords : 0))*2;
    }
    
    void start(final RFixer invoker)
    {
        final SwingWorker worker = new SwingWorker()
        {
            public Object construct()
            {
                RData data = new RData();
                strippedrows = 0;
                                                
                for(int i=0;i<toFix.size();i++)
                {
                    data.init(new Integer(toFix.get(i).id));
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                             invoker.addProgress(1);
                        }
                        });

                    data.fixErrors(toFix.get(i));
                    
                    java.awt.EventQueue.invokeLater(new Runnable() {
                        public void run() {
                             invoker.addProgress(1);
                        }
                        });
                        
                }
                
                if(!needRowStrip)
                    return null;
                
                String sql = "";
                
                try
                {
                    sql = "select roastId "
                        + "from roast "
                        + "where roastStart>='";
                    sql += new SimpleDateFormat("dd-MM-yyyy 00:00").format(from) + "' and roastStart<='";
                    sql += new SimpleDateFormat("dd-MM-yyyy 23:59:59").format(to) + "'";
                    Connection con = RVOptions.getOpt().getDBC();
                    Statement sta = con.createStatement();
                    ResultSet rs = sta.executeQuery(sql);
            
                    while(rs.next())
                    {
                        data.init(new Integer(rs.getInt("roastID")));
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                 invoker.addProgress(1);
                            }
                            });
                    
                    
                        strippedrows += data.stripUnusedRows();
                        java.awt.EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                 invoker.addProgress(1);
                            }
                            });
                    }
                }
                catch(SQLException e)
                {
                    RVOptions.getOpt().err("Can't exec: " + sql);
                    RVOptions.getOpt().err(e.getMessage());
                }
                return null;
            }
            public void finished()
            {
                Toolkit.getDefaultToolkit().beep();
                invoker.finishProcess();
            }
        };
        worker.start();
        
    }
}
