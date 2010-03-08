/*
 * RoastGraph.java
 *
 * Created on 13 Март 2007 г., 17:20
 */

package jroastviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.*;


/**
 *
 * @author  Leo
 */
public class RoastGraph extends javax.swing.JFrame 
        implements  ChartChangeListener, 
                    ChartProgressListener,
                    Comparator<RoastGraph>
{
    
    class DayComboData
    {
        int         id;
        String      outSKU;
        String      level;
        public String  toString() { return Integer.toString(id) + ": " + outSKU + "/" + level;}
    };
        
    /** Creates new form RoastGraph */
    public RoastGraph() {
        initComponents();
    }
    
    public RoastGraph(Integer id)
    {
        initComponents();
        graphPanel.setLayout(new BoxLayout(graphPanel, BoxLayout.Y_AXIS));
        init(id);
        rchart = createChart();
        graphPanel.add(new ChartPanel(rchart));
//        rchart.addChangeListener(this);
        rchart.addProgressListener(this);
        compareDayPicker.getMonthView().setFirstDayOfWeek(2);
        compareDayPicker.setDateInMillis(data.head.start.getTime());
    }
    
    void    init(Integer id)
    {
        data.init(id);
        setTitle(data.head.getTitle());
        inpSkuLbl.setText(data.head.inpSKU);
        inpWeightLbl.setText(Double.toString(data.head.inpWeight));
        outSkuLbl.setText(data.head.outSKU);
        outWeightLbl.setText(Double.toString(data.head.outWeight));
        roastLevelLbl.setText(data.head.roastlevel);
        loadLbl.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(data.head.load));
        unloadLbl.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(data.head.unload));
        timeLbl.setText(new SimpleDateFormat("00:mm:ss").format(data.head.unload.getTime() - data.head.load.getTime()));
    }
    
    private XYDataset createDataset()
    {
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries intimeseries = new XYSeries("Input Temp");
        XYSeries outtimeseries = new XYSeries("Inner Temp");
        for(int i=0;i<data.rows.size();i++)
        {
            intimeseries.add(i, data.rows.get(i).inpTemp);
            outtimeseries.add(i, data.rows.get(i).innerTemp);
        }
        if(innerTempBox.isSelected())
            collection.addSeries(outtimeseries);
        if(inputTempBox.isSelected())
            collection.addSeries(intimeseries);
        return collection;
    }

    private XYDataset createCompareDataset()
    {
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries intimeseries = new XYSeries("Input Temp 2");
        XYSeries outtimeseries = new XYSeries("Inner Temp 2");
        for(int i=0;i<cmpdata.rows.size();i++)
        {
            intimeseries.add(i, cmpdata.rows.get(i).inpTemp);
            outtimeseries.add(i, cmpdata.rows.get(i).innerTemp);
        }
        if(innerTempBox.isSelected())
            collection.addSeries(outtimeseries);
        if(inputTempBox.isSelected())
            collection.addSeries(intimeseries);
        return collection;
    }
    
    private XYDataset createDiffDataset()
    {
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries intimeseries = new XYSeries("Zero");
        XYSeries outtimeseries = new XYSeries("Diff (2-1)");
        boolean inner = innerTempBox.isSelected();
        int sz = cmpdata.rows.size() < data.rows.size() ? cmpdata.rows.size() : data.rows.size();
        for(int i=0;i<sz;i++)
        {
            intimeseries.add(i, 0D);
            outtimeseries.add(i, inner ?
                      cmpdata.rows.get(i).innerTemp - data.rows.get(i).innerTemp
                        : cmpdata.rows.get(i).inpTemp - data.rows.get(i).inpTemp);
        }
        collection.addSeries(intimeseries);
        collection.addSeries(outtimeseries);
        return collection;
    }
    
    private JFreeChart createChart()
    {
        XYDataset xydataset = createDataset();
        
        StandardXYItemRenderer xyitemrenderer = new StandardXYItemRenderer();
        NumberAxis numberaxis = new NumberAxis("Temp");
        xyplot = new XYPlot(xydataset, null, numberaxis, xyitemrenderer);
        xyplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);         
        xyplot.setDomainGridlinePaint(Color.lightGray);
        xyplot.setDomainGridlineStroke(new BasicStroke(1.0F));
        xyplot.setRangeGridlinePaint(Color.lightGray);
        xyplot.setRangeGridlineStroke(new BasicStroke(1.0F));
        xyplot.setRangeTickBandPaint(new Color(240, 240, 240)); 
        ValueAxis valueaxis = xyplot.getRangeAxis();
        valueaxis.setRange(100D, 450D);
        
        xyitemrenderer.setSeriesPaint(0, Color.blue);
        xyitemrenderer.setSeriesStroke(0, new BasicStroke(2.0F));
        xyitemrenderer.setSeriesPaint(1, Color.green);
        xyitemrenderer.setSeriesStroke(1, new BasicStroke(2.0F));
        
        ValueMarker valuemarker1 = new ValueMarker(data.getLoadOffset(), Color.orange, new BasicStroke(2.0F));
        ValueMarker valuemarker2 = new ValueMarker(data.getUnLoadOffset(), Color.red, new BasicStroke(2.0F));
        xyplot.addDomainMarker(valuemarker1, org.jfree.ui.Layer.BACKGROUND);
        xyplot.addDomainMarker(valuemarker2, org.jfree.ui.Layer.BACKGROUND);
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setDomainCrosshairLockedOnData(true);
        xyplot.setRangeCrosshairVisible(false); 
        xyplot.setDomainCrosshairStroke(new BasicStroke(1.5F, 1, 1, 1.0F, new float[] {
            10F, 6F
        }, 0.0F));
        
        NumberAxis numberaxis2 = new NumberAxis("Temp Diff");
        diffplot = new XYPlot(new XYSeriesCollection(), null, numberaxis2,
                new XYDifferenceRenderer(Color.red, Color.green, false));
        diffplot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);         
        diffplot.setDomainGridlinePaint(Color.lightGray);
        diffplot.setDomainGridlineStroke(new BasicStroke(1.0F));
        diffplot.setRangeGridlinePaint(Color.lightGray);
        diffplot.setRangeGridlineStroke(new BasicStroke(1.0F));
        diffplot.setRangeTickBandPaint(new Color(240, 240, 240)); 
        
        
        
        combineddomainxyplot = new CombinedDomainXYPlot(new NumberAxis("Sec"));
        combineddomainxyplot.setGap(10D);
        combineddomainxyplot.add(xyplot, 3); 
        NumberAxis domaxis = (NumberAxis)combineddomainxyplot.getDomainAxis();
        domaxis.setTickUnit(new NumberTickUnit(60D), true, true);
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);
        return new JFreeChart("Roast data - " + data.head.getShortTitle(), JFreeChart.DEFAULT_TITLE_FONT, 
                combineddomainxyplot, true); 
    }

    private void redrawChart()
    {
        XYPlot plot = xyplot;
        plot.setDataset(createDataset());
        XYItemRenderer xyitemrenderer = plot.getRenderer();
        xyitemrenderer.setSeriesPaint(0, innerTempBox.isSelected() ? Color.blue : Color.green);        
        combineddomainxyplot.remove(diffplot);
        if(compareCheck.isSelected() && cmpdata != null)
        {
            plot.setDataset(1, createCompareDataset());
            NumberAxis numberaxis = new NumberAxis("Secondary");
            numberaxis.setAutoRangeIncludesZero(false);
            numberaxis.setRange(100D, 450D);
            plot.setRangeAxis(1, numberaxis);
            plot.mapDatasetToRangeAxis(1, 1); 
            StandardXYItemRenderer xyitemrenderer1 = new StandardXYItemRenderer();
            xyitemrenderer1.setSeriesPaint(0, Color.black);
            xyitemrenderer1.setSeriesStroke(0, new BasicStroke(1.0F));
            xyitemrenderer1.setSeriesPaint(1, new Color(20,160,20));
            xyitemrenderer1.setSeriesStroke(1, new BasicStroke(1.0F));
            plot.setRenderer(1, xyitemrenderer1);
            
            diffplot.setDataset(createDiffDataset());
            combineddomainxyplot.add(diffplot, 1);
        }
        else
        {
            plot.setDataset(1, null);
            plot.setRangeAxis(1, null);
        }
        ValueAxis valueaxis = plot.getRangeAxis();
        valueaxis.setRange(100D, 450D);
        
        ValueMarker valuemarker1 = new ValueMarker(data.getLoadOffset(), Color.orange, new BasicStroke(2.0F));
        ValueMarker valuemarker2 = new ValueMarker(data.getUnLoadOffset(), Color.red, new BasicStroke(2.0F));
        xyplot.clearDomainMarkers();
        if(compareCheck.isSelected() && cmpdata != null)
        {
            int of1 = data.getUnLoadOffset();
            int of2 = cmpdata.getUnLoadOffset();
            IntervalMarker intervalmarker = new IntervalMarker( of1 > of2 ? of2 : of1, of1> of2 ? of1 : of2);
            intervalmarker.setLabelOffsetType(org.jfree.ui.LengthAdjustmentType.EXPAND);
            intervalmarker.setPaint(new Color(255, 235, 235));
            intervalmarker.setAlpha(0.2f);
            intervalmarker.setLabel("Unload diff");
            intervalmarker.setLabelFont(new Font("SansSerif", 0, 11));
            intervalmarker.setLabelPaint(Color.red);
            intervalmarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
            intervalmarker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
            xyplot.addDomainMarker(intervalmarker, org.jfree.ui.Layer.BACKGROUND); 
            ValueMarker valuemarker3 = new ValueMarker(cmpdata.getUnLoadOffset(), Color.red, new BasicStroke(2.0F, 1, 1, 1.0F, 
                    new float[] { 10F, 6F }, 0.0F));
            xyplot.addDomainMarker(valuemarker3, org.jfree.ui.Layer.BACKGROUND);            
        }
        xyplot.addDomainMarker(valuemarker1, org.jfree.ui.Layer.BACKGROUND);
        xyplot.addDomainMarker(valuemarker2, org.jfree.ui.Layer.BACKGROUND);
    }

    
    public void chartChanged(ChartChangeEvent chartchangeevent)
    {
        XYDataset xydataset = xyplot.getDataset();
        Comparable comparable = xydataset.getSeriesKey(0);
        int d = (int)xyplot.getDomainCrosshairValue();
        if(d<0 || d>=data.rows.size())
            return;
        inpTempLbl.setText(Double.toString(data.rows.get(d).inpTemp));
        innerTempLbl.setText(Double.toString(data.rows.get(d).innerTemp));
    }
    
    public void chartProgress(ChartProgressEvent chartprogressevent)
    {
        XYDataset xydataset = xyplot.getDataset();
        Comparable comparable = xydataset.getSeriesKey(0);
        int d = (int)xyplot.getDomainCrosshairValue();
        if(d<0 || d>=data.rows.size())
            return;
        inpTempLbl.setText(Double.toString(data.rows.get(d).inpTemp));
        innerTempLbl.setText(Double.toString(data.rows.get(d).innerTemp));
        
    } 
    
    public int compare(RoastGraph d1, RoastGraph d2)
    {
        return  d1.data.head.outSKU.compareTo(d2.data.head.outSKU);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        inpSkuLbl = new javax.swing.JLabel();
        inpWeightLbl = new javax.swing.JLabel();
        outSkuLbl = new javax.swing.JLabel();
        outWeightLbl = new javax.swing.JLabel();
        roastLevelLbl = new javax.swing.JLabel();
        loadLbl = new javax.swing.JLabel();
        unloadLbl = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        timeLbl = new javax.swing.JLabel();
        comparePanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        inpSkuLbl1 = new javax.swing.JLabel();
        inpWeightLbl1 = new javax.swing.JLabel();
        outSkuLbl1 = new javax.swing.JLabel();
        outWeightLbl1 = new javax.swing.JLabel();
        roastLevelLbl1 = new javax.swing.JLabel();
        loadLbl1 = new javax.swing.JLabel();
        unloadLbl1 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        timeLbl1 = new javax.swing.JLabel();
        setMainBut = new javax.swing.JButton();
        graphPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        inputTempBox = new javax.swing.JCheckBox();
        innerTempBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        inpTempLbl = new javax.swing.JLabel();
        innerTempLbl = new javax.swing.JLabel();
        compareCheck = new javax.swing.JCheckBox();
        compareDayPicker = new org.jdesktop.swingx.JXDatePicker();
        compareBox = new javax.swing.JComboBox();
        compareSameSKUCheck = new javax.swing.JCheckBox();
        compareMonthCheck = new javax.swing.JCheckBox();

        setTitle("Roast Graph");
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Roast Info (1)"));
        jLabel2.setText("InpSKU:");

        jLabel3.setText("InpWeight:");

        jLabel4.setText("OutSKU:");

        jLabel5.setText("OutWeight:");

        jLabel6.setText("Roast Level:");

        jLabel7.setText("Load:");

        jLabel8.setText("UnLoad:");

        inpSkuLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        inpSkuLbl.setText("0");

        inpWeightLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        inpWeightLbl.setText("0");

        outSkuLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        outSkuLbl.setText("0");

        outWeightLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        outWeightLbl.setText("0");

        roastLevelLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        roastLevelLbl.setText("0");

        loadLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        loadLbl.setText("0");

        unloadLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        unloadLbl.setText("0");

        jLabel1.setText("Time:");

        timeLbl.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        timeLbl.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(inpSkuLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inpWeightLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
            .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(outSkuLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unloadLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(outWeightLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                    .addComponent(roastLevelLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(timeLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(inpSkuLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(inpWeightLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(outSkuLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(outWeightLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(roastLevelLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(loadLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(unloadLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(timeLbl)))
        );

        comparePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Compare with (2)"));
        jLabel9.setText("InpSKU:");

        jLabel10.setText("InpWeight:");

        jLabel11.setText("OutSKU:");

        jLabel12.setText("OutWeight:");

        jLabel13.setText("Roast Level:");

        jLabel14.setText("Load:");

        jLabel15.setText("UnLoad:");

        inpSkuLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        inpSkuLbl1.setText("0");

        inpWeightLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        inpWeightLbl1.setText("0");

        outSkuLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        outSkuLbl1.setText("0");

        outWeightLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        outWeightLbl1.setText("0");

        roastLevelLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        roastLevelLbl1.setText("0");

        loadLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        loadLbl1.setText("0");

        unloadLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        unloadLbl1.setText("0");

        jLabel16.setText("Time:");

        timeLbl1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        timeLbl1.setText("0");

        setMainBut.setText("Set as Main");
        setMainBut.setEnabled(false);
        setMainBut.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                setMainButActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout comparePanelLayout = new javax.swing.GroupLayout(comparePanel);
        comparePanel.setLayout(comparePanelLayout);
        comparePanelLayout.setHorizontalGroup(
            comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(inpWeightLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(inpSkuLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jSeparator4, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel13)
                .addGap(16, 16, 16)
                .addComponent(roastLevelLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(loadLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(unloadLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(outWeightLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(outSkuLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(timeLbl1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(setMainBut, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addContainerGap())
        );
        comparePanelLayout.setVerticalGroup(
            comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(comparePanelLayout.createSequentialGroup()
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(inpSkuLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(inpWeightLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(outSkuLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(outWeightLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(roastLevelLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(loadLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(unloadLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(comparePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(timeLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                .addComponent(setMainBut)
                .addContainerGap())
        );
        comparePanel.getAccessibleContext().setAccessibleName("Compare with (2)");

        graphPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Roast Temperature"));
        javax.swing.GroupLayout graphPanelLayout = new javax.swing.GroupLayout(graphPanel);
        graphPanel.setLayout(graphPanelLayout);
        graphPanelLayout.setHorizontalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 822, Short.MAX_VALUE)
        );
        graphPanelLayout.setVerticalGroup(
            graphPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        inputTempBox.setSelected(true);
        inputTempBox.setText("Input T");
        inputTempBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        inputTempBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        inputTempBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                inputTempBoxActionPerformed(evt);
            }
        });

        innerTempBox.setSelected(true);
        innerTempBox.setText("Inner T");
        innerTempBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        innerTempBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        innerTempBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                innerTempBoxActionPerformed(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        inpTempLbl.setFont(new java.awt.Font("Tahoma", 1, 12));
        inpTempLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        inpTempLbl.setText("0.0");

        innerTempLbl.setFont(new java.awt.Font("Tahoma", 1, 12));
        innerTempLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        innerTempLbl.setText("0.0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(inpTempLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inputTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(innerTempLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(innerTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(763, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(innerTempLbl)
                        .addComponent(inpTempLbl)
                        .addComponent(inputTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(innerTempBox, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        compareCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        compareCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        compareCheck.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                compareCheckActionPerformed(evt);
            }
        });

        compareDayPicker.setToolTipText("Choose date and select roasts in the combo below");
        compareDayPicker.setEnabled(false);
        compareDayPicker.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                compareDayPickerActionPerformed(evt);
            }
        });

        compareBox.setEnabled(false);
        compareBox.setLightWeightPopupEnabled(false);
        compareBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                compareBoxActionPerformed(evt);
            }
        });

        compareSameSKUCheck.setText("Same SKU");
        compareSameSKUCheck.setToolTipText("Check this box if you want to see only the same SKU");
        compareSameSKUCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        compareSameSKUCheck.setEnabled(false);
        compareSameSKUCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        compareSameSKUCheck.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                compareSameSKUCheckActionPerformed(evt);
            }
        });

        compareMonthCheck.setText("Whole month");
        compareMonthCheck.setToolTipText("Check this box if you want to see roast for whole month, not for a day");
        compareMonthCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        compareMonthCheck.setEnabled(false);
        compareMonthCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
        compareMonthCheck.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                compareMonthCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(compareCheck)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(compareDayPicker, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(compareSameSKUCheck)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(compareMonthCheck))
                                        .addComponent(compareBox, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(comparePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(compareCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(compareDayPicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compareSameSKUCheck)
                            .addComponent(compareMonthCheck))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(compareBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comparePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(graphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void setMainButActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_setMainButActionPerformed
    {//GEN-HEADEREND:event_setMainButActionPerformed
        RData rd = data;
        data = cmpdata;
        cmpdata = rd;
 
        setTitle(data.head.getTitle());
        inpSkuLbl.setText(data.head.inpSKU);
        inpWeightLbl.setText(Double.toString(data.head.inpWeight));
        outSkuLbl.setText(data.head.outSKU);
        outWeightLbl.setText(Double.toString(data.head.outWeight));
        roastLevelLbl.setText(data.head.roastlevel);
        loadLbl.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(data.head.load));
        unloadLbl.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(data.head.unload));
        timeLbl.setText(new SimpleDateFormat("00:mm:ss").format(data.head.unload.getTime() - data.head.load.getTime()));

        inpSkuLbl1.setText(cmpdata.head.inpSKU);
        inpWeightLbl1.setText(Double.toString(cmpdata.head.inpWeight));
        outSkuLbl1.setText(cmpdata.head.outSKU);
        outWeightLbl1.setText(Double.toString(cmpdata.head.outWeight));
        roastLevelLbl1.setText(cmpdata.head.roastlevel);
        loadLbl1.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(cmpdata.head.load));
        unloadLbl1.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(cmpdata.head.unload));
        timeLbl1.setText(new SimpleDateFormat("00:mm:ss").format(cmpdata.head.unload.getTime() - cmpdata.head.load.getTime()));
        
        redrawChart();
    }//GEN-LAST:event_setMainButActionPerformed

    private void compareMonthCheckActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compareMonthCheckActionPerformed
    {//GEN-HEADEREND:event_compareMonthCheckActionPerformed
        refreshCompare();
    }//GEN-LAST:event_compareMonthCheckActionPerformed

    private void compareSameSKUCheckActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compareSameSKUCheckActionPerformed
    {//GEN-HEADEREND:event_compareSameSKUCheckActionPerformed
        refreshCompare();
    }//GEN-LAST:event_compareSameSKUCheckActionPerformed

    private void compareBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareBoxActionPerformed
        DayComboData daydata = (DayComboData)compareBox.getSelectedItem();
        if(daydata == null)
            return;
        cmpdata = new RData();
        cmpdata.init(new Integer(daydata.id));
        inpSkuLbl1.setText(cmpdata.head.inpSKU);
        inpWeightLbl1.setText(Double.toString(cmpdata.head.inpWeight));
        outSkuLbl1.setText(cmpdata.head.outSKU);
        outWeightLbl1.setText(Double.toString(cmpdata.head.outWeight));
        roastLevelLbl1.setText(cmpdata.head.roastlevel);
        loadLbl1.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(cmpdata.head.load));
        unloadLbl1.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(cmpdata.head.unload));
        timeLbl1.setText(new SimpleDateFormat("00:mm:ss").format(cmpdata.head.unload.getTime() - cmpdata.head.load.getTime()));
        redrawChart();
    }//GEN-LAST:event_compareBoxActionPerformed

    private void refreshCompare()
    {
        String sql = "select roastID, sku.skuName outSkuName, roastLevels.roastLevelFullName "
                    + "from roast, sku, roastLevels "
                    + "where roast.roastOutputSkuID=sku.skuID and roast.roastLevelLNK=roastLevels.roastLevelID "
                    + " and roastID<>" + data.head.id.toString()
                    + " and roastLoad>='";
        
        if(compareMonthCheck.isSelected())
        {
            Date day = compareDayPicker.getDate();
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(day);
            gc.add(Calendar.MONTH,1);
            sql += new SimpleDateFormat("01-MM-yyyy 00:00").format(day);
            sql += "' and roastLoad < '" + new SimpleDateFormat("01-MM-yyyy 00:00").format(gc.getTime()) + "'";
        } else
        {
            sql += new SimpleDateFormat("dd-MM-yyyy 00:00").format(compareDayPicker.getDate());
            sql += "' and roastLoad <= '" + new SimpleDateFormat("dd-MM-yyyy 23:59:59").format(compareDayPicker.getDate()) + "' ";
        }
        
        if(compareSameSKUCheck.isSelected())
        {
            sql += " and sku.skuName='" + data.head.outSKU + "'";
        }
        
        sql += " order by 2, 1";
        RVOptions.getOpt().dbg("compareCombo SQL: " + sql);
        compareBox.removeAllItems();
        try
        {
            Connection con = RVOptions.getOpt().getDBC();
            Statement sta = con.createStatement();
            ResultSet rs = sta.executeQuery(sql);
            while(rs.next())
            {
                DayComboData row = new DayComboData();
                row.id = rs.getInt("roastID");
                row.outSKU = rs.getString("outSkuName");
                row.level = rs.getString("roastLevelFullName");
                compareBox.addItem(row);
            }
            RVOptions.getOpt().retDBC(con);
        }
        catch(SQLException e)
        {
            RVOptions.getOpt().err("DayCombo SQL query: " + sql);
            RVOptions.getOpt().err(e.getMessage());
        }        
    }
    
    private void compareDayPickerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareDayPickerActionPerformed

        refreshCompare();
    }//GEN-LAST:event_compareDayPickerActionPerformed
    
    private void compareCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareCheckActionPerformed
        compareDayPicker.setEnabled(compareCheck.isSelected());
        compareBox.setEnabled(compareCheck.isSelected());
        comparePanel.setEnabled(compareCheck.isSelected());
        compareSameSKUCheck.setEnabled(compareCheck.isSelected());
        compareMonthCheck.setEnabled(compareCheck.isSelected());
        setMainBut.setEnabled(compareCheck.isSelected());
        redrawChart();
    }//GEN-LAST:event_compareCheckActionPerformed

    private void inputTempBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_inputTempBoxActionPerformed
    {//GEN-HEADEREND:event_inputTempBoxActionPerformed
        redrawChart();
    }//GEN-LAST:event_inputTempBoxActionPerformed

    private void innerTempBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_innerTempBoxActionPerformed
    {//GEN-HEADEREND:event_innerTempBoxActionPerformed
        redrawChart();
    }//GEN-LAST:event_innerTempBoxActionPerformed
    
    void    setCompModel(DefaultComboBoxModel mod) { compareBox.setModel(mod);};
    
    Integer getID() { return data.head.id;}
    RData   getData() { return data;};
    
    private  RData          data = new RData();
    private  RData          cmpdata =  null;
    private  JFreeChart     rchart;
    private  XYPlot         xyplot;
    private  XYPlot         diffplot;
    private  CombinedDomainXYPlot combineddomainxyplot;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox compareBox;
    private javax.swing.JCheckBox compareCheck;
    private org.jdesktop.swingx.JXDatePicker compareDayPicker;
    private javax.swing.JCheckBox compareMonthCheck;
    private javax.swing.JPanel comparePanel;
    private javax.swing.JCheckBox compareSameSKUCheck;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JCheckBox innerTempBox;
    private javax.swing.JLabel innerTempLbl;
    private javax.swing.JLabel inpSkuLbl;
    private javax.swing.JLabel inpSkuLbl1;
    private javax.swing.JLabel inpTempLbl;
    private javax.swing.JLabel inpWeightLbl;
    private javax.swing.JLabel inpWeightLbl1;
    private javax.swing.JCheckBox inputTempBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JLabel loadLbl;
    private javax.swing.JLabel loadLbl1;
    private javax.swing.JLabel outSkuLbl;
    private javax.swing.JLabel outSkuLbl1;
    private javax.swing.JLabel outWeightLbl;
    private javax.swing.JLabel outWeightLbl1;
    private javax.swing.JLabel roastLevelLbl;
    private javax.swing.JLabel roastLevelLbl1;
    private javax.swing.JButton setMainBut;
    private javax.swing.JLabel timeLbl;
    private javax.swing.JLabel timeLbl1;
    private javax.swing.JLabel unloadLbl;
    private javax.swing.JLabel unloadLbl1;
    // End of variables declaration//GEN-END:variables
    
}
