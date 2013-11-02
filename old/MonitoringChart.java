/*
 * MonitoringChart
 * - display monitoring information of cpu, memory, and disk using jFreeChart
 *  
 *  (c)Copyright 2005,2006
 *  Written by Sungju Kwon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package old;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

public class MonitoringChart extends JPanel {
	private static final long serialVersionUID = 5008911220600698304L;
	
	private JFreeChart chart;
	
	public MonitoringChart() {
		this("Node Monitoring : apple1");
	}
	
    public MonitoringChart(String title) {
    		setLayout(new BorderLayout());
    		chart = createChart(title, createDataset());
    		add(new ChartPanel(chart), BorderLayout.CENTER);
    		
    		new DataThread().start();
    }

    private JFreeChart createChart(String title, final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
        		title, 
            "Time", 
            "Usage in %",
            dataset, 
            true, 
            true, 
            false
        );
        final XYPlot plot = result.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(60000.0);  // 60 seconds
        axis = plot.getRangeAxis();
        axis.setRange(0.0, 100.0); 
        return result;
    }

    private TimeSeries cpuSeries;
    private TimeSeries memorySeries;
    private TimeSeries diskSeries;
    
    private XYDataset createDataset() {
        cpuSeries = new TimeSeries("CPU Load", Millisecond.class);
        memorySeries = new TimeSeries("Memory Usage", Millisecond.class);
        diskSeries = new TimeSeries("Disk Usage", Millisecond.class);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(cpuSeries);
        dataset.addSeries(memorySeries);
        dataset.addSeries(diskSeries);
        
        return dataset;

    }
    
    private class DataThread extends Thread {
    		private double cpuLastValue = 60;
    		private double memoryLastValue = 50;
    		private double diskLastValue = 30;
    		public void run() {
    			try {
				while (true) {
					cpuSeries.add(new Millisecond(), cpuLastValue);
					memorySeries.add(new Millisecond(), memoryLastValue);
					diskSeries.add(new Millisecond(), diskLastValue);
					
					do {
						memoryLastValue = memoryLastValue + (2 - 4 * Math.random());
					} while (memoryLastValue < 0 || memoryLastValue > 100);
					
					do {
						cpuLastValue = cpuLastValue + (8 - 16 * Math.random());
					} while (cpuLastValue < 0 || cpuLastValue > 100);
					
					do {
						diskLastValue = diskLastValue + (1 - 2 * Math.random());
					} while (diskLastValue < 0 || diskLastValue > 100);
					
					sleep(100);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
    		}
    }
}
