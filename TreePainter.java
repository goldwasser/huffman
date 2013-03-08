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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Paints an embedding of a proper binary tree (or subtree) onto a Graphics context.
 * 
 * The virtual coordinate system is such that leaves are spaced one unit
 * apart horizontally using inorder traversal.  Vertical scale from one
 * level to another can be parameterized (but is 2 by default).
 *
 */
public class TreePainter {
	static final private Color highlightColor = Color.red;
	static final private Color freqColor = Color.yellow; // Color.white;
	static final private double NodeDiameter = 0.9;
	
	private double maxDepth;   // y-coord for lowest leaf
	private double verticalScale;
	private HashMap<DataModel.TreeIterator, Point2D> coords;
	private HashMap<DataModel.TreeIterator, Rectangle2D> bounds;
	private DataModel.TreeIterator root;
	private double currentFontSize;

	/**
	 * Creates a TreePainter instance based upon the given model
	 * 
	 * Uses default vertical separation of 2.
 	 * @param root TreeIterator designating the root of Huffman (sub)tree
	 */
	public TreePainter(DataModel.TreeIterator root) {
		this(root, 2.0);
	}
	
	/**
	 * Creates a TreePainter instance based upon the given model
	 * 
 	 * @param root TreeIterator designating the root of Huffman (sub)tree
	 * @param verticalScale designates separation distance from level to level
	 *   (with one being the horizontal separation from node to node inorder)
	 */
	public TreePainter(DataModel.TreeIterator root, double verticalScale) {
		this.root = root;
		this.verticalScale = verticalScale;
		coords = new HashMap<DataModel.TreeIterator, Point2D>();
		bounds = new HashMap<DataModel.TreeIterator, Rectangle2D>();

		// compute coordinates
		maxDepth = 0;
		embed(root, 0, 0, verticalScale);
		
		// shift all so that root is aligned with x=0
		double shift = coords.get(root).getX();
		for (Map.Entry<DataModel.TreeIterator, Point2D> entry : coords.entrySet()) {
			Point2D old = entry.getValue();
			Point2D updated = new Point2D.Double(old.getX() - shift, old.getY());
			coords.put(entry.getKey(), updated); 
		}
		
	}

	
	/**
	 * Computes node coordinates for subtree rooted at iterator.
	 * 
	 * The leftmost leaf will be given x-coordinate of 'first',
	 * while all nodes have depth based on setting with root at 'depth'.
	 * @param root TreeIterator representing the root of the tree
	 * @param depth depth of root
	 * @param first x-coordinate of leftmost leaf
	 * @return x-coordinate of rightmost leaf
	 */
	private int embed(DataModel.TreeIterator root, double depth, int first, double verticalScale) {
		DataModel.TreeIterator left = root.getLeft();
		if (left != null) {
			int temp = embed(left, depth+verticalScale, first, verticalScale);
			
			DataModel.TreeIterator right = root.getRight();
			temp = embed(right, depth+verticalScale, temp+1, verticalScale);
			
			double leftX = coords.get(left).getX();
			double rightX = coords.get(right).getX();
			Point2D here = new Point2D.Double( (leftX + rightX) / 2, depth);
			coords.put(root, here);
			
			Rectangle2D leftB = bounds.get(left);
			Rectangle2D rightB = bounds.get(right);
			double minX =  leftX + leftB.getMinX() - here.getX();
			double maxX =  rightX + rightB.getMaxX() - here.getX();
			double height= verticalScale + Math.max(leftB.getHeight(), rightB.getHeight());
			bounds.put(root, new Rectangle2D.Double(minX, -0.5, maxX-minX, height));
			
			return temp;
			
		} else {
			coords.put(root, new Point2D.Double(first, depth));
			bounds.put(root, new Rectangle2D.Double(-0.5, -0.5, 1.0, 2.0));
			return first;
		}
	}
	
	/**
	 * Returns a reasonable bounding box for the subtree rooted at given iterator,
	 * assuming that root is centered at 0,0.
	 * @return Rectangle2D representing bounding box
	 */
	public Rectangle2D getBounds(DataModel.TreeIterator root) {
		return bounds.get(root);
	}

    // TODO: get rid of this function and rewrite draw routines to do
	// downward traversal from subroot, rather than iteration through
	// full tree.
	private boolean isDescendent(DataModel.TreeIterator node, DataModel.TreeIterator root) {
		DataModel.TreeIterator walk = node;
		if (walk == null) return false;
		while (!walk.equals(root)) {
			DataModel.TreeIterator parent = walk.getParent();
			if (parent == null)
				return false;
			walk = parent;
		}
		return true;
	}
	
	/**
	 * Draw subtree rooted at given iterator with root node at origin
	 * 
	 * @param g2 Graphics2D context on which to draw
	 * @param subroot DataModel.TreeIterator representing root of subtree within data model
	 * @param unit number of pixels for one "unit" in coordinate space
	 */
	public void draw(Graphics2D g2, DataModel.TreeIterator subroot, double unit) {
		draw(g2, subroot, unit, new Point2D.Double(0,0));
	}

	
	/**
	 * Draw subtree rooted at given iterator with root node at given point
	 * 
	 * @param g2 Graphics2D context on which to draw
	 * @param subroot DataModel.TreeIterator representing root of subtree within data model
	 * @param unit number of pixels for one "unit" in coordinate space
	 * @param p Point2D designating where the center of the root note should be placed
	 */
	public void draw(final Graphics2D g2, DataModel.TreeIterator subroot, final double unit, Point2D p) {
		// TODO: why is p expressed in actual coords, but rest in virtual?
		Point2D subP = coords.get(subroot);
		final Point2D offset = new Point2D.Double(p.getX()-unit*subP.getX(), p.getY()-unit*subP.getY());
		g2.setColor(Color.black);
		g2.setStroke(new BasicStroke((float) (unit/10), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		// top-down recursion to draw line structure
		new Object()
		{
			public void recurse(DataModel.TreeIterator node, boolean isFirst) {
				if (node != null) {
					if (!isFirst) {
						Point2D a = coords.get(node);
						Point2D b = coords.get(node.getParent());
						g2.drawLine((int) (offset.getX() + unit * a.getX()),
								(int) (offset.getY() + unit * a.getY()),
								(int) (offset.getX() + unit * b.getX()),
								(int) (offset.getY() + unit * b.getY()));
					}
					recurse(node.getLeft(), false);
					recurse(node.getRight(), false);
				}
			}
		}.recurse(subroot, true);
		
		// draw nodes and symbols
		Font f = g2.getFont();
		Font big = f.deriveFont(AffineTransform.getScaleInstance(100,100)); // avoid roundoff issues?
		final double radius = unit * TreePainter.NodeDiameter * Math.min(1.0, verticalScale);
		currentFontSize = 100.0 * radius / g2.getFontMetrics(big).getMaxAdvance();
		g2.setFont(f.deriveFont(AffineTransform.getScaleInstance(currentFontSize, currentFontSize)));

		// top-down recursion to draw nodes and symbols
		new Object()
		{
			public void recurse(DataModel.TreeIterator node) {
				if (node != null) {
					Point2D ctr = coords.get(node);
					g2.fillOval((int) (offset.getX() + unit * ctr.getX() - radius/2),
							(int) (offset.getY() + unit * ctr.getY() - radius/2),
							(int) (radius),
							(int) (radius));

					if (node.getSymbol() != null) {
						String s = HuffmanDemo.printableSymbol(node.getSymbol());
						Rectangle2D box = g2.getFontMetrics().getStringBounds(s, g2);
						g2.drawString(s,
								 (int) (offset.getX() + unit * ctr.getX() - box.getWidth()/2),
								 (int) (offset.getY() + unit * (1 + ctr.getY())));
					}

					recurse(node.getLeft());
					recurse(node.getRight());
				}
			}
		}.recurse(subroot);
		
		g2.setFont(f);  // restore previous font
		
		if (root.getFrequency() > 0) {
			// 	draw frequency info
			g2.setColor(TreePainter.freqColor);
			big = f.deriveFont(AffineTransform.getScaleInstance(100,100)); // avoid roundoff issues?
			Rectangle2D bounds = big.getStringBounds("000000000000", 0, Integer.toString(root.getFrequency()).length(), g2.getFontRenderContext());
			double freqFontSize = 0.9 * 100 * radius / Math.max(bounds.getWidth(),bounds.getHeight());
			g2.setFont(f.deriveFont(AffineTransform.getScaleInstance(freqFontSize, freqFontSize)));
			
			// top-down recursion to draw frequencies
			new Object()
			{
				public void recurse(DataModel.TreeIterator node) {
					if (node != null) {
						Point2D ctr = coords.get(node);
						String s = Integer.toString(node.getFrequency());
						Rectangle2D box = g2.getFontMetrics().getStringBounds(s, g2);
						g2.drawString(s,
								(int) (offset.getX() + unit * ctr.getX() - box.getWidth()/2),
								(int) (offset.getY() + unit * ctr.getY() - box.getHeight()/2 - box.getMinY()));

						recurse(node.getLeft());
						recurse(node.getRight());
					}
				}
			}.recurse(subroot);

			g2.setFont(f);  // restore previous font
		}
	}

	/**
	 * Draw highlighting for path from subtree root to a node within that subtree.
	 * 
	 * In this version, the root of the subtree is positioned with center (0,0).
	 * 
	 * Note: drawSelection should be called BEFORE draw is called.
	 * 
	 * @param g2 Graphics2D context on which to draw
	 * @param subroot DataModel.TreeIterator representing root of subtree within data model
	 * @param node DataModel.TreeIterator representing the lower node
	 * @param unit number of pixels for one "unit" in coordinate space
	 */
	public void drawSelection(Graphics2D g2, DataModel.TreeIterator subroot, DataModel.TreeIterator node, double unit) {
		drawSelection(g2, subroot, node, unit, new Point2D.Double(0,0));
	}
	
	/**
	 * Draw highlighting for path from subtree root to a node with root rendered at given point.
	 * 
	 * Highlight path is in Red.
	 * Note: drawSelection should be called BEFORE draw is called.
	 * 
	 * @param g2 Graphics2D context on which to draw
	 * @param subroot DataModel.TreeIterator representing root of subtree within data model
	 * @param node DataModel.TreeIterator representing the leaf node
	 * @param unit number of pixels for one "unit" in coordinate space
	 * @param p Point2D designating where the center of the root note should be placed
	 */
	public void drawSelection(Graphics2D g2, DataModel.TreeIterator subroot, DataModel.TreeIterator node, double unit, Point2D p) {
		drawSelection(g2, subroot, node, unit, p, highlightColor);
	}
	
	/**
	 * Draw highlighting for path from subtree root to a node with root rendered at given point.
	 * 
	 * Highlight path is in Red.
	 * Note: drawSelection should be called BEFORE draw is called.
	 * 
	 * @param g2 Graphics2D context on which to draw
	 * @param subroot DataModel.TreeIterator representing root of subtree within data model
	 * @param node DataModel.TreeIterator representing the leaf node
	 * @param unit number of pixels for one "unit" in coordinate space
	 * @param color Color for the highlighted path.
	 */
	public void drawSelection(Graphics2D g2, DataModel.TreeIterator subroot,
			DataModel.TreeIterator node, double unit, Color color) {
		drawSelection(g2, subroot, node, unit, new Point2D.Double(0,0), color);
	}
	
	/**
	 * Draw highlighting for path from subtree root to a node with root rendered at given point.
	 * 
	 * Note: drawSelection should be called BEFORE draw is called.
	 * 
	 * @param g2 Graphics2D context on which to draw
	 * @param subroot DataModel.TreeIterator representing root of subtree within data model
	 * @param node DataModel.TreeIterator representing the leaf node
	 * @param unit number of pixels for one "unit" in coordinate space
	 * @param p Point2D designating where the center of the root note should be placed
	 * @param color Color for the highlighted path.
	 */
	public void drawSelection(Graphics2D g2, DataModel.TreeIterator subroot,
				DataModel.TreeIterator node, double unit, Point2D p, Color color) {
		Point2D subP = coords.get(subroot);
		Point2D offset = new Point2D.Double(p.getX()-unit*subP.getX(), p.getY()-unit*subP.getY());
		// color pen
		g2.setColor(color);
		g2.setStroke(new BasicStroke((float) (unit/3), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		DataModel.TreeIterator walk = node;
		while (!walk.equals(subroot)) {
			DataModel.TreeIterator parent = walk.getParent();
			Point2D a = coords.get(walk);
			Point2D b = coords.get(parent);
			Point2D realA = new Point2D.Double(offset.getX() + unit * a.getX(), offset.getY() + unit * a.getY());
			Point2D realB = new Point2D.Double(offset.getX() + unit * b.getX(), offset.getY() + unit * b.getY());
			g2.drawLine((int) realA.getX(), (int) realA.getY(), (int) realB.getX(), (int) realB.getY());
			
			Font f = g2.getFont();
			g2.setFont(f.deriveFont(AffineTransform.getScaleInstance(currentFontSize, currentFontSize)));

			boolean isLeft = (walk.equals(parent.getLeft()));
			double midY = (realA.getY() + realB.getY()) / 2;
			String bit = (isLeft ? "0" : "1");
			Rectangle2D box = g2.getFontMetrics().getStringBounds(bit, g2);
			double cornerY = midY;  // + box.getMaxY() - box.getHeight()/2;
			double cornerX = realA.getX() - (realA.getX() - realB.getX()) * (realA.getY() - midY) / (realA.getY() - realB.getY());
			double highlightOffset = realA.distance(realB) * unit / (6 * (realA.getY() - realB.getY()));
			g2.drawString(bit, (int) (cornerX - (isLeft ? box.getMaxX() + highlightOffset : box.getMinX() - highlightOffset) ),
						 (int) (cornerY));
			
			g2.setFont(f);  // restore original font
			walk = parent;
		}
	}
	
	
	private void debug() {
		for (Map.Entry<DataModel.TreeIterator, Point2D> entry : coords.entrySet()) {
			System.out.println("Iterator " + entry.getKey().hashCode());
			System.out.println("   freq:  " + entry.getKey().getFrequency());
			System.out.println("   point: " + entry.getValue());
			System.out.println("   box:   " + bounds.get(entry.getKey()));
			DataModel.TreeIterator p = entry.getKey().getParent();
			System.out.println("   parent " + (p == null ? -1 : p.hashCode()));
		}
	}
	
	public void reFit(double aspect) {
		embed(root, 0, 0, aspect);
	}
	
	/**
	 * Unit testing
	 * @param args
	 */
	public static void main(String[] args) {
		LinkedHashMap<String, Integer> m = new LinkedHashMap();
		m.put("a", 25);
		m.put("b", 76);
		m.put("e", 135);
		new TreePainter(DataModel.createFromFrequencies(m).getRoot(), 1);
	}

}
