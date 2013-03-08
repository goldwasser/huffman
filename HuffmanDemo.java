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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.BevelBorder;


/**
 * Top-level class for the Huffman Encoding Demonstration.
 * 
 * This can either be executed as an applet or an application.
 */
@SuppressWarnings("serial")
public class HuffmanDemo extends JApplet{
	
	public final static int DEBUG = 0;  // 0=None, 1+ controls verbosity
	
	private final static int WIDTH = 1100;
	private final static int HEIGHT = 800;
	private DataModel model;
	private enum InputMode { RAW, FREQ, CODE };
	private InputMode inputMode;
	
	private String lastRaw;
	private DataModel lastFreq;
	private DataModel lastCodebook;
	private static ReSizer resizer;
	private CodePanel coderView;
	private TableView tableView;
	private TreeView treeView;
	
	/*
	 * method to stop playing
	 */
	synchronized public void stop() {
		coderView.stop();
	}
	
	/**
	 * Starts the demo as an Application
	 * @param args
	 */
	
	public static void main(String[] args) {
		HuffmanDemo demo = new HuffmanDemo();
		demo.init();
		JFrame frame = new JFrame();
		frame.add(demo);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setVisible(true);
        //resizer.setFrame(frame);
        frame.addComponentListener(resizer);
	}

	/**
	 * Starts the demo as an Applet
	 */
	public void init() {
		// defaults for input dialog
		lastRaw = "";
		LinkedHashMap<String,Integer> temp = new LinkedHashMap<String,Integer>();
		temp.put("a", 25);
		temp.put("b", 76);
		temp.put("e", 135);
		lastFreq = DataModel.createFromFrequencies(temp);
		lastCodebook = lastFreq;
		
		// topmost content pane will have two cards.  One for input. Other for main menu options/view 
		final CardLayout topCards = new CardLayout();   // layout for topmost content pane
		tableView = new TableView(HEIGHT-70);  // 50 is arbitrary, to account for menu inlay (but not properly resized)

		// input card is solely comprised of InputDialog
		final InputDialog inputDialog = new InputDialog();
		final NonEmptyValidator rawValidator = new NonEmptyValidator();
		final FrequencyValidator freqValidator = new FrequencyValidator();
		final CodebookValidator codeValidator = new CodebookValidator();
		
		// main card has menu at top and then viewPanel with choice of cards in center
		// Card choice:  algorithmView, standardPanel
		final JPanel mainPanel = new JPanel();
		final JMenuBar menuBar = new JMenuBar();
		final JMenu viewMenu = new JMenu("View");
		//added menu option for changing font size
		final JMenu fontSize = new JMenu("Font Size");
		final JPanel viewPanel = new JPanel();
		final CardLayout viewCards = new CardLayout();

		// Standard Panel combines the TreeView at left and TableView at right
		final JPanel standardPanel = new JPanel();
		treeView = new TreeView();
		coderView = new CodePanel(this);
		coderView.start();

		// Algorithm View
		final AlgorithmViewPanel algView = new AlgorithmViewPanel();  // Note: used to be AlgorithmView instance
		final JMenuItem algSubMenu = new JMenuItem("Huffman Construction");
		
		// prepare topmost content pane
		getContentPane().setLayout(topCards);
		getContentPane().add(mainPanel, "main");
		getContentPane().add(inputDialog, "input");

		// prepare main view panel
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(menuBar, BorderLayout.NORTH);
		mainPanel.add(viewPanel, BorderLayout.CENTER);
		
		viewPanel.setLayout(viewCards);
		JLabel welcomeLabel = new JLabel("<html><center>Welcome to the Huffman Demo.<br>Please load data to begin.</center></html>");
		welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
		welcomeLabel.setFont(new Font("Serif", Font.PLAIN, 36));
		viewPanel.add(welcomeLabel, "welcome");
		viewPanel.add(standardPanel, "standard");
		viewPanel.add(algView, "algorithm");
		
		standardPanel.setLayout(new BorderLayout());
		standardPanel.add(treeView, BorderLayout.CENTER);
		tableView.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		standardPanel.add(tableView, BorderLayout.EAST);
		tableView.addSymbolSelectionListener(new SymbolSelectionListener()
		{
			public void selectionChanged(SymbolSelectionEvent e) {
				treeView.setSymbolSelection(e.getSelectedSymbol());
			}
		});
		standardPanel.add(coderView, BorderLayout.SOUTH);

		// setup menus for mainPanel
		JMenu menu;
		JMenuItem item;

		menu = new JMenu("Load");
		menuBar.add(menu);
		item = new JMenuItem("Raw Text");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) {
				inputMode = InputMode.RAW;
				inputDialog.setValidator(rawValidator);
				inputDialog.setMessage("Enter sample text", Color.BLACK);
				inputDialog.setText(lastRaw);
				topCards.show(getContentPane(),  "input");
				repaint();
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Frequencies");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) {
				inputMode = InputMode.FREQ;
				inputDialog.setValidator(freqValidator);
				inputDialog.setMessage("Enter symbol frequencies using sample format shown below", Color.BLACK);
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String,Integer> e : lastFreq.getFrequencyMap().entrySet()) {
					sb.append(printableSymbol(e.getKey()));
					sb.append(" ");
					sb.append(e.getValue());
					sb.append("\n");
				}
				topCards.show(getContentPane(), "input");
				inputDialog.setText(sb.toString());
				repaint();
			}
		});
		menu.add(item);

		item = new JMenuItem("Codebook");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0) {
				inputMode = InputMode.CODE;
				inputDialog.setValidator(codeValidator);
				inputDialog.setMessage("Enter codebook using sample format shown below", Color.BLACK);
				StringBuilder sb = new StringBuilder();
				for (Map.Entry<String,String> e : lastFreq.getCodebookMap().entrySet()) {
					sb.append(printableSymbol(e.getKey()));
					sb.append(" ");
					sb.append(e.getValue());
					sb.append("\n");
				}
				inputDialog.setText(sb.toString());
				topCards.show(getContentPane(), "input");
				repaint();
			}
		});
		menu.add(item);
		
		menuBar.add(viewMenu);
		
		item = new JMenuItem("Encode/Decode");
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				viewCards.show(viewPanel, "standard");
			}
		});
		viewMenu.add(item);
		
		item = algSubMenu;
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				viewCards.show(viewPanel, "algorithm");
			}
		});
		viewMenu.add(item);
		viewMenu.setEnabled(false);
		
		//set up size menu

		// jumping through hoops to recognize '+' key properly (as typical shows
		// as shift '=' on US keyboards) will allow non-shift version for convenience
		Action increaseFontAction = new AbstractAction("bigger") {
			public void actionPerformed(ActionEvent e) {
				int size = Math.min(80, tableView.getFontSize()+1);
				tableView.setFontSize(size);
				algView.setFontSize(size);
				inputDialog.setFontSize(size);
				coderView.setFontSize(size);
				tableView.reFit();
			}			
		};

		Action decreaseFontAction = new AbstractAction("smaller") {
			public void actionPerformed(ActionEvent e) {
				int size = Math.max(6, tableView.getFontSize()-1);
				tableView.setFontSize(size);
				algView.setFontSize(size);
				inputDialog.setFontSize(size);
				coderView.setFontSize(size);
				tableView.reFit();
			}			
		};
		
		KeyStroke stroke_minus = KeyStroke.getKeyStroke(
				KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		KeyStroke stroke_shiftminus = KeyStroke.getKeyStroke(
				KeyEvent.VK_MINUS, ActionEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		KeyStroke stroke_plus = KeyStroke.getKeyStroke(
				KeyEvent.VK_PLUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		KeyStroke stroke_equals = KeyStroke.getKeyStroke(
				KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		KeyStroke stroke_shiftequals = KeyStroke.getKeyStroke(
				KeyEvent.VK_EQUALS, ActionEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

		item = new JMenuItem(increaseFontAction);
		item.setAccelerator(stroke_plus);
		InputMap im = item.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.put(stroke_equals, "make bigger");
		im.put(stroke_shiftequals, "make bigger");
		item.getActionMap().put("make bigger", increaseFontAction);
		fontSize.add(item);
		
		item = new JMenuItem(decreaseFontAction);
		item.setAccelerator(stroke_minus);
		im = item.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.put(stroke_shiftminus, "make smaller");
		item.getActionMap().put("make smaller", decreaseFontAction);
		fontSize.add(item);
		
		/*
		 * this section makes sure the font for all the components are the same at the beginning
		 */
		int size = 24;
		tableView.setFontSize(size);
		algView.setFontSize(size);
		inputDialog.setFontSize(size);
		tableView.reFit();
		coderView.setFontSize(size);

		menuBar.add(fontSize);

		final ModelView[] views = {tableView, treeView, algView, coderView};
		inputDialog.addInputListener(new InputListener()
		{
			public void inputSubmitted(Object input) {
				if (input != null) {
					switch (inputMode) {
					case RAW:
						lastRaw = (String) input;
						model = DataModel.createFromRaw(lastRaw);
						lastFreq = lastCodebook = model;
						break;
					case FREQ:
						model = DataModel.createFromFrequencies((LinkedHashMap<String, Integer>) input);
						lastFreq = lastCodebook = model;
						break;
					case CODE:
						model = DataModel.createFromCodebook((LinkedHashMap<String, String>) input);
						lastCodebook = model;
						break;
					}
					for (ModelView mv : views)
						mv.setModel(model);
					viewCards.show(viewPanel, "standard");
					viewMenu.setEnabled(true);
					algSubMenu.setEnabled(model.hasFrequencyData());
					repaint();
					paint(viewPanel.getGraphics());
				}
				topCards.show(getContentPane(), "main");
				repaint();
			}	
						
		});
		
		//add components to resizer
		//resizer.addComponent(tableView);
		//resizer.addComponent(treeView);
		
	}
	
	/**
	 * Utility to replace whitespace characters with escape sequences.
	 * 
	 * Specifically, it replaces space with \s, newline with \n, and tab as \t
	 * @param sym
	 * @return String instance after replacements
	 */
	public static String printableSymbol(String sym) {
		String temp = sym;
		temp = temp.replace(" ", "\\s");
		temp = temp.replace("\n", "\\n");
		temp = temp.replace("\t", "\\t");
		return temp;
	}

	public static String rawSymbol(String sym) {
		String temp = sym;
		temp = temp.replace("\\s", " ");
		temp = temp.replace("\\n", "\n");
		temp = temp.replace("\\t", "\t");
		return temp;
	}

	public Hashtable<String, String> codeToSymbolHash() {
		return tableView.codeToSymbolHash();
	}

	public Hashtable<String, String> symbolToCodeHash() {
		return tableView.symbolToCodeHash();
	}

	public void highlightSymbol(String value, Color color) {
		treeView.setSymbolSelection(value, color);
		tableView.setSymbolSelection(value);
	}
	
	public void highlightPath(String code, Color color) {
		treeView.setPathHighlight(code, color);
	}

}
