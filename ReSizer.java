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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import javax.swing.JFrame;


public class ReSizer implements ComponentListener{

	private ArrayList<Resizable> subComponents = new ArrayList<Resizable>();
	private JFrame frame;
	
	ReSizer() {
		
	}
	ReSizer(JFrame f) {
		frame = f;
	}
	
	public void setFrame(JFrame f) {
		frame = f;
	}
	
	public void addComponent(Resizable component) {
		subComponents.add(component);
	}
	public void componentHidden(ComponentEvent e) {
		
	}

	public void componentMoved(ComponentEvent e) {
		
	}

	public void componentResized(ComponentEvent e) {
		for(Resizable r: subComponents) {
			try {
			r.reFit();
			}
			catch(NullPointerException n) {
				if (r.getClass() == TreeView.class)
				{}
				else
					throw n;
			}
		}
		frame.paintComponents(frame.getGraphics());
	}

	public void componentShown(ComponentEvent e) {
		
	}

}
