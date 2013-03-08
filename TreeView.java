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

import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Creates a view of a data model as a tree.
 * 
 */
public class TreeView extends JComponent implements ModelView, Resizable{
	private TreePainter painter;
	private String highlighted;
	private DataModel.TreeIterator pathHighlighted;
	private int buffer;
	private DataModel model;
	private Color highlightColor;
	private static Color primaryColor = Color.RED;
	
	/**
	 * Sets or resets the data model for the tree view.
	 */
	public void setModel(DataModel m) {
		model = m;
		setSymbolSelection(null);
		double aspect = (1.0 * m.size() * getHeight()) / (m.getRoot().getDepth() * getWidth());
		painter = new TreePainter(m.getRoot(), aspect);
		highlightColor = Color.red;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		if (painter != null) {
			Graphics2D g2 = (Graphics2D) g;
			DataModel.TreeIterator root = model.getRoot();
			Rectangle2D box = painter.getBounds(root);
			double factor = Math.min(getWidth() / box.getWidth(), getHeight() / box.getHeight());
			Point2D fix = new Point2D.Double(-box.getMinX() * factor + (getWidth() - factor*box.getWidth())/2, -box.getMinY() * factor);
			if (highlighted != null) {
				painter.drawSelection(g2, root, model.getLeaf(highlighted), factor, fix, highlightColor);
			}
			if (pathHighlighted != null) {
				painter.drawSelection(g2, root, pathHighlighted, factor, fix, highlightColor);
			}
			painter.draw(g2, root, factor, fix);
		}
	}
	
	/**
	 * Called to designate a particular symbol for highlighting.
	 * 
	 * @param s symbol to highlight (or null if no highlighting is desired)
	 */
	public void setSymbolSelection(String s) {
		setSymbolSelection(s, primaryColor);
	}

	public void setSymbolSelection(String s, Color color) {
		pathHighlighted = null;
		highlighted = s;
		highlightColor = color;
		repaint();
	}
	
	public void setPathHighlight(String path) {
		setPathHighlight(path, primaryColor);
	}
	
	public void setPathHighlight(String path, Color color) {
		highlightColor = color;
		highlighted = null;
		if (model != null)
			pathHighlighted = model.getRoot();
		for (int i = 0; i < path.length(); i++) {
			char child = path.charAt(i);
			if (child == '0')
				pathHighlighted = pathHighlighted.getLeft();
			else
				pathHighlighted = pathHighlighted.getRight();
		}
		repaint();
	}

	public void reFit() {
		//same formula from constructor
		if (getWidth() != 0 && model.getRoot().getDepth() != 0){
			double aspect = (1.0 * model.size() * getHeight()) / (model.getRoot().getDepth() * getWidth());
			painter.reFit(aspect);
			repaint();
		}
	}
	
}
