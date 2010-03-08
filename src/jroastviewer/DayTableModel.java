/*
 * DayTableModel.java
 *
 * Created on 12 Март 2007 г., 16:16
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author leo
 */
public class DayTableModel extends AbstractTableModel
{
    
    /** Creates a new instance of DayTableModel */
    public DayTableModel()
    {
    }
    
        private String[] columnNames =  {
            "Id", "Day", "Start", "End",  "Input SKU", 
            "InpWeight", "OutWeight", "Output SKU", "Time",
            "Operator", "Status", "Level"
        };
        
        
        private Vector<Vector<Object>>     data = new Vector<Vector<Object>>();
        
        public void initSQL(String where)
        {
            String sql = "select roastID, coalesce(roastLoad, roastStart) roastLoad, coalesce(roastUnload, '01-01-1970 00:00') roastUnload, "
                        + "coalesce(roastUnload - roastLoad, '01-01-1970 00:00') roastTime, osku.skuName as oskuName, isku.skuName as iskuName,"
                        + "roastInputQTY, roastOutputQTY, roastStatus, worker.workerFullname, roastLevels.roastLevelFullName "
                        + "from roast, sku isku, sku osku, worker, roastLevels "
                        + " where roastInputSkuID = isku.skuID and roastOutputSkuID=osku.skuID and worker.workerID=roast.roastWorkerLNK "
                        + " and roastLevels.roastLevelID=roast.roastLevelLNK ";
            sql += where + " order by roastId";
            
            data.clear();
            try
            {
                Connection con = RVOptions.getOpt().getDBC();
                Statement sta = con.createStatement();
                ResultSet rs = sta.executeQuery(sql);
                while(rs.next())
                {
                    Vector<Object> row = new Vector<Object>();
                    row.add(new Integer(rs.getInt("roastId")));
                    row.add(new SimpleDateFormat("yyyy-MM-dd").format(rs.getTimestamp("roastLoad")));
                    row.add(new SimpleDateFormat("HH:mm:ss").format(rs.getTimestamp("roastLoad")));
                    row.add(new SimpleDateFormat("HH:mm:ss").format(rs.getTimestamp("roastUnload")));
                    row.add(rs.getString("iskuName"));
                    row.add(new Double(rs.getFloat("roastInputQTY")/1000.0));
                    row.add(new Double(rs.getFloat("roastOutputQTY")/1000.0));
                    row.add(rs.getString("oskuName"));
                    row.add(new SimpleDateFormat("HH:mm:ss").format(rs.getTimestamp("roastTime")));
                    row.add(rs.getString("workerFullName"));
                    row.add(rs.getString("roastStatus"));
                    row.add(rs.getString("roastLevelFullName"));
                    data.add(row);
                }
                RVOptions.getOpt().retDBC(con);
            }
            catch(SQLException e)
            {
                RVOptions.getOpt().err("Can't load rows for day table model:");
                RVOptions.getOpt().err(e.getMessage());
            }
            catch(Exception ex)
            {
                RVOptions.getOpt().err("Can't load rows for day table model (any ex):");
                RVOptions.getOpt().err(ex.getMessage());
            }
            fireTableDataChanged();
        }
        
        public int getColumnCount()
        {
            return columnNames.length;
        }
        
        public int getRowCount()
        {
            return data.size();
        }
        
        public String getColumnName(int col)
        {
            return columnNames[col];
        }
        
        public Object getValueAt(int row, int col)
        {
            return data.get(row).get(col);
        }
        
        
    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
        public boolean isCellEditable(int row, int col)
        {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return false;
        }
        
    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
        public void setValueAt(Object value, int row, int col)
        {
            fireTableCellUpdated(row, col);
        }    
}
