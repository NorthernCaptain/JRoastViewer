/*
 * RData.java
 *
 * Created on 13 Март 2007 г., 19:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jroastviewer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.Vector;

/**
 *
 * @author Leo
 */
public class RData{

    RHeader     head = new RHeader();
    
    int         firstRowID = 0;
    int         lastRowID = 0;
    
    
    class Row
    {
        int     id;
        double  inpTemp;
        double  innerTemp;
        int     flags;
    }
    
    Vector<Row>     rows=new Vector<Row>();
    
    public int compare(RData d1, RData d2)
    {
        return  d1.head.outSKU.compareTo(d2.head.outSKU);
    }
    /** Creates a new instance of RData */
    public RData() {
    }
    
    public  void    init(Integer id)
    {
        //innerTemp and inpTemp are swaped in DB, so we change them back here
        String sql= "select roastDetID, roastDetInputTemp/10.0 innerTemp, roastDetProductTemp/10.0 inpTemp, roastDetFlags "
                    + "from roastDet where roastDet.roastDetRoastLNK=" + id.toString() + " order by 1";
        head.init(id);
        rows.clear();
        firstRowID = lastRowID =0;
        try
        {
            long start_idx = 0;
            long unload_idx = 0;
            Connection con = RVOptions.getOpt().getDBC();
            Statement sta = con.createStatement();
            ResultSet rs = sta.executeQuery(sql);
            while(rs.next())
            {
                Row row = new Row();
                row.id = rs.getInt("roastDetID");
                row.inpTemp = rs.getDouble("inpTemp");
                row.innerTemp = rs.getDouble("innerTemp");
                row.flags = rs.getInt("roastDetFlags");
                rows.add(row);
                if(row.flags == -2100 && start_idx == 0)
                    start_idx = rows.size() - 1;
            }

            //считаем что в конце начала загрузки быть не может - значит мы ее не нашли и ставим ее в самое начало.
            if(start_idx + 600 > rows.size())
                start_idx = 1;
            
            {
                for(int i=(int)start_idx+1;i<rows.size();i++)
                    if(rows.get(i).flags == -2200 && unload_idx == 0)
                    {
                        unload_idx = i;
                        break;
                    }
            }
            
            if(rows.size()>0)
            {
                firstRowID = rows.get(0).id;
                lastRowID = rows.get(rows.size()-1).id;
            }
            
            //fix unload time according to the row data
            if(unload_idx > 0)
            {
                head.unload.setTime(head.load.getTime() + (unload_idx - start_idx)*1000L);
            }
            
            //strip unused extra data at the end
            RVOptions.getOpt().dbg("unload_idx: " + head.id.toString() + " = " + Long.toString(unload_idx) + " / " + Integer.toString(rows.size()));
            if(unload_idx > start_idx && rows.size() - unload_idx > 40)
            {
                for(int i=rows.size()-1;i>=unload_idx +40;i--)
                    rows.removeElementAt(i);
            }
            RVOptions.getOpt().dbg("start_idx: " + head.id.toString() + " = " + Long.toString(start_idx) + " / " + Integer.toString(rows.size()));
            //strip unused extra data from the start
            if(start_idx > 40)
            {
                for(int i=0; i< start_idx - 40;i++)
                    rows.removeElementAt(0);
            }
        }
        catch(SQLException e)
        {
            RVOptions.getOpt().err("SQL Exception while initing roast data:");
            RVOptions.getOpt().err(e.getMessage());
            RVOptions.getOpt().err("SQL was: " + sql);
        }
    }
    
    //return number of stripped rows
    int stripUnusedRows()
    {
        int count = 0;
        String sql;
        
        if(rows.size()==0)
            return 0;
        
        if(firstRowID!=0 && firstRowID < rows.get(0).id)
        {
            count += rows.get(0).id -firstRowID;
            sql = "delete from roastDet where roastDetID >=" + Integer.toString(firstRowID)
                + " and roastDetID<" + Integer.toString(rows.get(0).id)
                + " and roastDetRoastLNK=" + head.id.toString();
            
            try
            {
                Connection con = RVOptions.getOpt().getDBC();
                con.setAutoCommit(true);
                Statement sta = con.createStatement();
                RVOptions.getOpt().dbg("Strip start sql: " + sql);
 //               sta.executeUpdate(sql);
                sta.close();
                RVOptions.getOpt().retDBC(con);            
            }
            catch(SQLException e)
            {
                RVOptions.getOpt().err("Can't strip start rows for id=" + head.id.toString());
                RVOptions.getOpt().err(e.getMessage());
            }
        }
        
        if(lastRowID>0 && lastRowID > rows.get(rows.size()-1).id)
        {
            count += lastRowID - rows.get(rows.size()-1).id;
            sql = "delete from roastDet where roastDetID <=" + Integer.toString(lastRowID)
                + " and roastDetID>" + Integer.toString(rows.get(rows.size()-1).id)
                + " and roastDetRoastLNK=" + head.id.toString();
            
            try
            {
                Connection con = RVOptions.getOpt().getDBC();
                con.setAutoCommit(true);
                Statement sta = con.createStatement();
                RVOptions.getOpt().dbg("Strip end sql: " + sql);
//                sta.executeUpdate(sql);
                sta.close();
                RVOptions.getOpt().retDBC(con);            
            }
            catch(SQLException e)
            {
                RVOptions.getOpt().err("Can't strip end rows for id=" + head.id.toString());
                RVOptions.getOpt().err(e.getMessage());
            }
            
        }
        return count;
    }
    
    void    fixErrors(FixerProc.FixInfo nfo)
    {
        
    }
    
    int getLoadOffset()
    {
        for(int i=0;i<rows.size();i++)
            if(rows.get(i).flags==-2100)
                return i;
        return 0;
    }
    
    int getUnLoadOffset()
    {
        for(int i=0;i<rows.size();i++)
            if(rows.get(i).flags==-2200)
                return i;
        return 0;
    }
}
