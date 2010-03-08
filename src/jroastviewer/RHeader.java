/*
 * RHeader.java
 *
 * Created on 13 Март 2007 г., 18:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jroastviewer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author Leo
 */
public class RHeader {
    
    Integer     id;
    Timestamp   start;
    Timestamp   load;
    Timestamp   unload;
    Timestamp   end;
    
    String      inpSKU;
    String      outSKU;
    
    double      inpWeight;
    double      outWeight;
    
    String      roaster;
    String      roastlevel;
    /** Creates a new instance of RHeader */
    
    String  getTitle() { return id.toString() + ": " + outSKU + "/" + roastlevel + " from " + inpSKU + " by " + roaster;}
    String  getShortTitle() { return id.toString() + ": " + outSKU + "/" + roastlevel;}
    public RHeader() {
    }
    
    public RHeader(Integer iid)
    {
        init(id);
    }
    
    public void init(Integer iid)
    {
        id = iid;
        String sql = "select roastStart, coalesce(roastLoad, '01-01-1970 00:00') roastLoad, "
                    + "coalesce(roastUnload, '01-01-1970 00:00') roastUnload, coalesce(roastEnd, '01-01-1970 00:00') roastEnd, "
                    + "isku.skuName iskuName, osku.skuName oskuName, "
                    + "coalesce(roastInputQTY, 0)/1000.0 inpWeight, coalesce(roastOutputQTY,0)/1000.0 outWeight, roastLevels.roastLevelFullName, "
                    + "worker.workerFullname "
                    + "from roast, sku isku, sku osku, roastLevels, worker "
                    + "where roast.roastInputSkuID=isku.skuID and roast.roastOutputSkuID=osku.skuID and roast.roastLevelLNK=roastLevels.roastLevelID "
                    + " and worker.workerID=roast.roastWorkerLNK "
                    + " and roast.roastId=" + id.toString();
        try
        {
            Connection con = RVOptions.getOpt().getDBC();
            Statement sta = con.createStatement();
            ResultSet rs = sta.executeQuery(sql);
            if(rs.next())
            {
                start = rs.getTimestamp("roastStart");
                load = rs.getTimestamp("roastLoad");
                unload = rs.getTimestamp("roastUnload");
                end = rs.getTimestamp("roastEnd");
                
                inpSKU = rs.getString("iskuName");
                outSKU = rs.getString("oskuName");
                inpWeight = rs.getDouble("inpWeight");
                outWeight = rs.getDouble("outWeight");
                
                roaster = rs.getString("workerFullName");
                roastlevel = rs.getString("roastLevelFullName");
            }
        }
        catch(SQLException e)
        {
            RVOptions.getOpt().err("SQL Exception while initing roast header data:");
            RVOptions.getOpt().err(e.getMessage());
            RVOptions.getOpt().err("SQL was: " + sql);            
        }
             
    }
}
