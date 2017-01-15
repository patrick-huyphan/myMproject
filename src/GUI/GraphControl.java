package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GraphControl {

	GraphPanel graphPanel;
	JLabel label;
	final int MIN = -225, MAX = 225;
	double[][] data;
	NumberFormat nf;

	public GraphControl(GraphPanel gp) {
		graphPanel = gp;
		data = graphPanel.getSortedData();
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
	}

	public JPanel getValuePanel() {
		label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		Dimension d = label.getPreferredSize();
		d.height = 25;
		label.setPreferredSize(d);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label);
		return panel;
	}

	public JPanel getUIPanel() {
		final JSlider slider = new JSlider(JSlider.HORIZONTAL, MIN, MAX, 0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int value = slider.getValue();
				double[] xy = getValues(value);
				graphPanel.setDot(xy);
				label.setText("x = " + nf.format(xy[0]) + ",  y = " + nf.format(xy[1]));
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(slider);
		return panel;
	}

	private double[] getValues(int value) {
		double w = graphPanel.getWidth();
		double h = graphPanel.getHeight();
		double xScale = (graphPanel.STEPS / 10) / (double) MAX;
		double x = value * xScale;
		double y = getSortedValueForX(x);
		return new double[] { x, y };
	}

	private double getSortedValueForX(double x) {
		// find points in sorted data that straddle x
		int index = -1;
		for (int j = 0; j < data.length - 1; j++) {
			double x1 = data[j][0];
			double x2 = data[j + 1][0];
			if (x1 < x && x < x2) {
				index = j;
				break;
			}
		}
		// find corresponding y value from sorted data
		if (index > -1) {
			// slope of line between the two points
			double dx = data[index + 1][0] - data[index][0];
			double dy = data[index + 1][1] - data[index][1];
			double slope = dy / dx;
			// y - y1 / x - x1  =  dy / dx
			double y = data[index][1] + (x - data[index][0]) * slope;
			return y;
		}
		return -1;
	}
}