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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A GUI component for gathering input from a text area.
 * 
 * The dialog GUI has a message panel at top that can be used
 * as a prompt, a large text area at center for gathering input,
 * and a panel with Submit and Cancel buttons below.  When the user
 * submits or cancels, a given InputListener will be notified of the
 * result (the result is null in the case of a cancellation).
 * 
 * An optional Validator can be specified to check whether the input
 * being submitted is valid, and producing a resulting object from
 * the parsed input.  If a submit is deemed invalid, the InputListener
 * will not yet be informed of the action, and an appropriate error
 * message will be displayed in the GUI.
 */
public class InputDialog extends JComponent {
	
	/**
	 * Initializes the dialog component.
	 * 
	 * The default validator accepts all input.
	 */
	public InputDialog() {
		listeners = new HashSet<InputListener>();
		setLayout(new BorderLayout());
		promptLabel = new JLabel(" ");
		promptLabel.setHorizontalAlignment(JLabel.CENTER);
		add(promptLabel, BorderLayout.NORTH);
		Box centerBox = new Box(BoxLayout.Y_AXIS);
		centerBox.add(Box.createVerticalStrut(20));
		textBox = new JTextArea();
		textBox.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createLoweredBevelBorder()));
//		add(new JScrollPane(textBox), BorderLayout.CENTER);
		centerBox.add(new JScrollPane(textBox));
		add(centerBox, BorderLayout.CENTER);
		JButton submitButton = new JButton("Submit");
		JButton cancelButton = new JButton("Cancel");
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(submitButton);
		buttonPanel.add(cancelButton);
		add(buttonPanel, BorderLayout.SOUTH);

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) {
				for (InputListener listener : listeners)
					listener.inputSubmitted(null);
			}
		});
		
		submitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event) {
				boolean valid = true;
				Object result = null;
				if (validator == null)
					result = textBox.getText();
				else try {
					result = validator.parse(textBox.getText());
				} catch (ValidatorException e) {
					valid = false;
					setMessage(e.getMessage(), Color.red);
					repaint();
				}
				
				if (valid)
					for (InputListener listener : listeners)
						listener.inputSubmitted(result);
			}
		});
	}

	/**
	 * Registers an InputListener to this dialog.
	 * @param listener
	 */
	public void addInputListener(InputListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Unregisters an InputListener from this dialog (if it exists).
	 * @param listener
	 */
	public void removeInputListener(InputListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Sets the text area contents
	 */
	public void setText(String text) {
		textBox.setText(text);
	}
	
	/**
	 * Sets the display message at the top of dialog.
	 * @param msg String message
	 * @param c Color instance
	 */
	public void setMessage(String msg, Color c) {
		promptLabel.setText(msg);
		promptLabel.setForeground(c);
	}
	
	/**
	 * Sets this dialogs validator to the given parameter.
	 * 
	 * Any previous validator becomes irrelevant.
	 * @param v new Validator
	 */
	public void setValidator(Validator v) {
		validator = v;
	}
	
	/**
	 * method to change size of font
	 * @param size
	 */
	public void setFontSize(int size) {
		Font currentFont = textBox.getFont();
		textBox.setFont(new Font(currentFont.getName(), currentFont.getStyle(), size));
		currentFont = promptLabel.getFont();
		promptLabel.setFont(new Font(currentFont.getName(), currentFont.getStyle(), size));
	}
	
	private Validator validator;
	private HashSet<InputListener> listeners;
	private JTextArea textBox;
	private JLabel promptLabel;
}
