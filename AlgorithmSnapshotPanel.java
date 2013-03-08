/*
 * Copyright 2013, Michael H. Goldwasser and Nicholas Brown.
 *
 * This file is part of the Huffman Coding Demonstration.
 *
 * The Huffman Coding Demonstration is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AlgorithmSnapshotPanel extends JPanel implements ActionListener, Runnable {
	final int NUM_INCREMENTS = 100;
	public enum Phase {NONE, RISING, SHIFTING, LOWERING};
	private DataModel model;
	private Phase phase = Phase.NONE;
	private int step;  // 0=initial, 1=first merge, 2=first PQ.sort, ... 2n-1 complete
	private double aspect;
	private double t;
	private JCheckBox check;
	private TreePainter painter;
	private JLabel status;
	private boolean run = false;
	private JButton playNpause;
	private int index = 1; //this is used to keep track of the index in case of pause during a play button action
	
	synchronized void setRun(boolean b) {
		run = b;
	}
	public boolean getRun() {
		return run;
	}
	
	public void run() {
		while (run && step != 2*model.size()-3) {
			if (step % 2 == 1 && check.isSelected()) {
				//conditionals in place in case the run is interrupted with a pause and needs to find its place
				if (phase.equals(Phase.RISING) || phase.equals(phase.NONE)) {
					if (phase.equals(phase.NONE))
						index = 1;
					phase = Phase.RISING;
					for (; index <= NUM_INCREMENTS && run; index++) {
						t = ((double) index) / NUM_INCREMENTS;
						paintImmediately(getVisibleRect());
					}
				}
				//can't be else if in case it reaches here without a pause interrupting
				if (run && (phase.equals(Phase.RISING) || phase.equals(Phase.SHIFTING))) {
					if (phase.equals(Phase.RISING))
						index = 1;
					phase = Phase.SHIFTING;
					for (; index <= NUM_INCREMENTS && run; index++) {
						t = ((double) index) / NUM_INCREMENTS;
						paintImmediately(getVisibleRect());
					}
				}
				//if statement doesn't need to surround the whole thing because the other if statements already tested the condition
				if (run && phase.equals(Phase.SHIFTING)) {
					index = 1;
					phase = Phase.LOWERING;
				}
				for (; index <= NUM_INCREMENTS && run; index++) {
					t = ((double) index) / NUM_INCREMENTS;
					paintImmediately(getVisibleRect());
				}
				
				if (run)
					phase = Phase.NONE;
			}
			if (run) {
				step++;
				setStatus();
				repaint();
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		playNpause.setIcon(new ImageIcon("play16.gif"));
		run = false;
	}
	
	/**
	 * method to change size of font
	 * @param size
	 */
	public void setFontSize(int size) {
		Font currentFont = status.getFont();
		status.setFont(new Font(currentFont.getName(), currentFont.getStyle(), size));
		
	}
	
	public AlgorithmSnapshotPanel(JCheckBox c, JLabel status, JButton button) {
		check = c;
		this.status = status;
		phase = Phase.NONE;
		playNpause = button;
	}
	
	public void paintComponent(Graphics g) {
		if (model != null) {
			Graphics2D g2 = (Graphics2D) g;
			g2.clearRect(0,  0, getWidth(), getHeight());
			Rectangle2D box = painter.getBounds(model.getRoot());
			double factor = Math.min(getWidth() / box.getWidth(), getHeight() / box.getHeight());

			DataModel.TreeIterator[] subs = model.getPQTrace(step/2);
			double subx[] = new double[subs.length];
			
			int endJ = subs.length;
			int splitJ = subs.length;
			double boxW=0, boxH=0;
			DataModel.TreeIterator combined = null;
			if (step % 2 == 1) {
				combined = subs[subs.length-1].getParent();
				boxW = painter.getBounds(combined).getWidth();
				boxH = Math.max(painter.getBounds(subs[subs.length-1]).getHeight(),
							    painter.getBounds(subs[subs.length-2]).getHeight());
				
				DataModel.TreeIterator[] future = model.getPQTrace(1+step/2);
				for (splitJ = future.length - 1; !future[splitJ].equals(combined); splitJ--);
			}
			
			double totalWidth = 0;
			double splitWidth = 0;
			double maxHeight = 0;
			int k=0;
			for (DataModel.TreeIterator s : subs) {
				Rectangle2D subbox = painter.getBounds(s);
				totalWidth +=  subbox.getWidth();
				subx[k] = totalWidth - subbox.getMaxX();
				maxHeight = Math.max(maxHeight, subbox.getHeight());
				k++;
				if (k == splitJ)
					splitWidth = totalWidth;
			}
			splitWidth = totalWidth - splitWidth;

			double padX = (getWidth() - factor*totalWidth)/2;
			double adjustX = 0;
			if (step % 2 == 1) {
				endJ -= 2;
				double combineX = (subx[subs.length-2] + subx[subs.length-1]) / 2;
				double combineY = maxHeight - 0.5 + aspect;
				double adjustY = 0;
//				if (splitJ != subs.length-2) {
					switch (phase) {
					case RISING:
						adjustY = - t * boxH;
						break;
					case SHIFTING:
						adjustX = - t * (splitWidth - boxW);
						adjustY = - boxH;
						break;
					case LOWERING:
						adjustX = - (splitWidth - boxW);
						adjustY = (t-1) * boxH;
						if (maxHeight != boxH)
							adjustY += t * aspect;
						break;
					}
//				}
				Point2D p = new Point2D.Double(factor*adjustX + padX + factor*combineX,
											   factor*adjustY + getHeight() - factor*combineY);
				
				if (step != 2 * model.size() - 3 && phase == Phase.NONE) {
					painter.drawSelection(g2, combined, subs[subs.length-1], factor, p);
					painter.drawSelection(g2, combined, subs[subs.length-2], factor, p);
				}
				painter.draw(g2, combined, factor, p);
			}
					
			for (int j=0; j < endJ; j++) {
				double x = padX + factor*subx[j];
				double y = getHeight() - factor*(maxHeight-0.5);
				if (j >= splitJ) {
					if (phase == Phase.SHIFTING)
						x += t * factor * boxW;
					else if (phase == Phase.LOWERING)
						x += factor * boxW;
				}
				if (phase == Phase.LOWERING && maxHeight == boxH)
					y -= t * factor * aspect;
				painter.draw(g2, subs[j], factor, new Point2D.Double(x, y));
			}
		}
	}
	
	public void setModel(DataModel m) {
		model = m;
		step = 0;
		phase= Phase.NONE;
		aspect = (1.0 * m.size() * getHeight()) / (m.getRoot().getDepth() * getWidth());
		painter = new TreePainter(m.getRoot(), aspect);
		setStatus();
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("<"))
			step = Math.max(0, step-1);
		else if (cmd.equals(">")) {
			if (step < 2 * model.size() - 3) {
				if (step % 2 == 1 && check.isSelected())
					animate();
				step += 1;
			}
		} else if (cmd.equals("<<"))
			step = 0;
		else if (cmd.equals(">>"))
			step = 2*model.size() - 3;

		setStatus();
		repaint();

	}
	
	private void setStatus() {
		if (step == 2*model.size() - 3) {
			status.setText("Process is complete");
//			System.out.println("complete");
		}
		else if (step % 2 == 1) {
			status.setText("Next step is to reorder priority queue");
//			System.out.println("reorder");
		}
		else {
			status.setText("Next step is to combine two lowest frequency groups");
//			System.out.println("combine");
		}
	}
	
	// Note that step has not yet been changed
	private void animate() {
		phase = Phase.RISING;
		for (int k=1; k <= NUM_INCREMENTS; k++) {
			t = ((double) k) / NUM_INCREMENTS;
			paintImmediately(getVisibleRect());
		}
		
		phase = Phase.SHIFTING;
		for (int k=1; k <= NUM_INCREMENTS; k++) {
			t = ((double) k) / NUM_INCREMENTS;
			paintImmediately(getVisibleRect());
		}
		
		phase = Phase.LOWERING;
		for (int k=1; k <= NUM_INCREMENTS; k++) {
			t = ((double) k) / NUM_INCREMENTS;
			paintImmediately(getVisibleRect());
		}
		
		phase = Phase.NONE;
	}


	
}
