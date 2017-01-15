package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.Random;

import javax.swing.JPanel;

public class GraphPanel extends JPanel {
	Random seed;
	NumberFormat nf;
	double[][] data;
	final int PAD = 25, STEPS = 50;
	GeneralPath path; // initialized in
	Point2D.Double dot; // initVariables
	Font font;

	public GraphPanel() {
		seed = new Random();
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		generateData();
		font = new Font("lucida sans regular", Font.PLAIN, 14);
		setPreferredSize(new Dimension(500, 500));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double w = getWidth();
		double h = getHeight();
		// draw ordinate
		g2.draw(new Line2D.Double(w / 2, PAD, w / 2, h - PAD));
		// draw abcissa
		g2.draw(new Line2D.Double(PAD, h / 2, w - PAD, h / 2));
		drawTickMarks(g2, w, h);
		labelAxes(g2, w, h);

		// plot GeneralPath
		if (path == null)
			initVariables(w, h);
		g2.draw(path);

		// plot data as points
		g2.setPaint(Color.red);
		double x, y;
		double xScale = (w / 2 - PAD) / (STEPS / 10);
		double yScale = (h / 2 - PAD) / (STEPS / 10);
		for (int j = 0; j < data.length; j++) {
			x = w / 2 + data[j][0] * xScale;
			y = h / 2 - data[j][1] * yScale;
			g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
		}

		// show slider position on graph
		g2.setPaint(Color.cyan); 
		x = w / 2 + dot.x * xScale;
		y = h / 2 - dot.y * yScale;
		g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));

	}

	public void setDot(double[] xy) {
		if (xy[1] != -1) {
			dot.x = xy[0];
			dot.y = xy[1];
		}
		repaint();
	}

	private void drawTickMarks(Graphics2D g2, double w, double h) {
		double xInc = (w - 2 * PAD) / STEPS;
		double yInc = (h - 2 * PAD) / STEPS;
		// ordinate tick marks
		double x = w / 2, y = PAD;
		for (int j = 0; j <= STEPS; j++) {
			if (j != STEPS / 2)
				g2.draw(new Line2D.Double(x, y, x + 2, y));
			y += yInc;
		}
		// abcissa tick marks
		x = PAD;
		y = h / 2;
		for (int j = 0; j <= STEPS; j++) {
			if (j != STEPS / 2)
				g2.draw(new Line2D.Double(x, y, x, y + 2));
			x += xInc;
		}
	}

	private void labelAxes(Graphics2D g2, double w, double h) {
		// label ordinate
		float sx = (float) (w / 2 + 4), sy;
		float syInc = (float) ((h - 2 * PAD) / (STEPS / 5));
		g2.setFont(font);
		String s = "";
		FontRenderContext frc = g2.getFontRenderContext();
		for (int j = STEPS / 10, k = 0; j >= -STEPS / 10; j--, k++) {
			s = String.valueOf(j);
			LineMetrics lm = font.getLineMetrics(s, frc);
			sy = PAD + syInc * k + lm.getAscent() / 2;
			if (j != 0)
				g2.drawString(s, sx, sy);
		}
		// label abcissa
		LineMetrics lm = font.getLineMetrics(s, frc);
		sy = (float) (h / 2 + lm.getAscent() + 2);
		float sxInc = (float) ((w - 2 * PAD) / (STEPS / 5));
		for (int j = -STEPS / 10, k = 0; j <= STEPS / 10; j++, k++) {
			s = String.valueOf(j);
			float sw = (float) font.getStringBounds(s, frc).getWidth();
			sx = PAD + sxInc * k - sw / 2;
			if (j != 0)
				g2.drawString(s, sx, sy);
		}
	}

	private void generateData() {
		data = new double[STEPS / 5][2];
		for (int row = 0; row < STEPS / 5; row++)
			for (int col = 0; col < 2; col++)
				data[row][col] = (STEPS / 10) * (2 * seed.nextDouble() - 1);
		// printArray(data);
	}

	public double[][] getSortedData() {
		// copy data array
		double[][] sorted = new double[data.length][data[0].length];
		for (int j = 0; j < sorted.length; j++) {
			sorted[j][0] = data[j][0];
			sorted[j][1] = data[j][1];
		}
		// sort by x value
		double temp0 = 0, temp1 = 0;
		for (int j = 0; j < sorted.length; j++) {
			for (int k = j + 1; k < sorted.length; k++) {
				if (sorted[k][0] < sorted[j][0]) {
					temp0 = sorted[j][0];
					temp1 = sorted[j][1];
					sorted[j][0] = sorted[k][0];
					sorted[j][1] = sorted[k][1];
					sorted[k][0] = temp0;
					sorted[k][1] = temp1;
				}
			}
		}
		// printArray(sorted);
		return sorted;
	}

	private void initVariables(double w, double h) {
		double[][] sorted = getSortedData();
		// printArray(sorted);
		path = new GeneralPath();
		float x, y;
		double xScale = (w / 2 - PAD) / (STEPS / 10);
		double yScale = (h / 2 - PAD) / (STEPS / 10);
		for (int j = 0; j < sorted.length; j++) {
			x = (float) (w / 2 + sorted[j][0] * xScale);
			y = (float) (h / 2 - sorted[j][1] * yScale);
			if (j == 0)
				path.moveTo(x, y);
			else
				path.lineTo(x, y);
		}
		dot = new Point2D.Double(0, 0);
	}

	private void printArray(double[][] d) {
		System.out.println("  x\t y\n------------");
		for (int j = 0; j < d.length; j++)
			System.out.println(nf.format(d[j][0]) + "\t" + nf.format(d[j][1]));
	}
}
