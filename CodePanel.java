import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class CodePanel extends JPanel implements ModelView {
	
	private static Highlighter.HighlightPainter errorPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
	private static Color[] colors = {new Color(30,144,255), new Color(255, 140, 0)};
	private static Highlighter.HighlightPainter[] p;
	static {
		p = new DefaultHighlighter.DefaultHighlightPainter[colors.length];
		for (int j=0; j < colors.length; j++)
			p[j] = new DefaultHighlighter.DefaultHighlightPainter(colors[j]);		
	}
	
	private HuffmanDemo master;
	private Highlighter h1 = null;
	private Highlighter h2 = null;
	private JTextField plainTextField;     // TODO: make this a JTextArea with height 1 and scroll (to accomodate newlines)
	private JTextField encodedTextField;
	private JLabel message;
	private JLabel title1;
	private JLabel title2;

	private DocumentListener plainListener;
	private DocumentListener encodedListener;
	
	// State information for internal model of encode/decode
	private boolean advanceBitwise;   // true when decoding; false when encoding
	private String plainText;
	private String encodedText;
	private ArrayList<Integer> endingBit;    // endingBit[0] is the bit that ends char 0 in encoded text, etc
	private int curChar;        // index of next char to highlight
	private int curBit;         // index of next bit to highlight
	

	//holds all the buttons that control the operations on the input text
	private RemoteControlPanel controlPanel = new RemoteControlPanel();
	private RemoteControlListener controlListener;
	
	void start() {
		(new Thread(controlPanel)).start();
	}
	
	public void stop() {
		controlPanel.stop();
	}
	
	CodePanel(final HuffmanDemo master) {
		this.master = master;
		
		//these text fields are used by coder for the user to decode and encode desired text
		plainTextField = new JTextField();
		encodedTextField = new JTextField();
		
		plainListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) { }
			public void insertUpdate(DocumentEvent arg0) {
				restartFromPlain();
			}
			public void removeUpdate(DocumentEvent arg0) {
				restartFromPlain();
			}
		};
		plainTextField.getDocument().addDocumentListener(plainListener);
		
		encodedListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent arg0) { }
			public void insertUpdate(DocumentEvent arg0) {
				restartFromCoded();
			}
			public void removeUpdate(DocumentEvent arg0) {
				restartFromCoded();
			}
		};
		encodedTextField.getDocument().addDocumentListener(encodedListener);
		
		
		h1 = new DefaultHighlighter();
		h2 = new DefaultHighlighter();
		plainTextField.setHighlighter(h1);
		encodedTextField.setHighlighter(h2);
		
		//set-up the decode/encode aspect
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		message = new JLabel("Input either plaintext or encoded text into appropriate field");
		message.setAlignmentX(CENTER_ALIGNMENT);
		add(message);

		add(plainTextField);
		title1 = new JLabel("plaintext");
		title1.setAlignmentX(CENTER_ALIGNMENT);
		add(title1);
		
		controlListener = new RemoteControlListener() {
			
			public void jumpToBeginning() {
				if (curChar != 0 || curBit != 0) {
					curChar = curBit = 0;
				}
				draw();
			}
			
			public void jumpToEnd() {
				int newCurBit = encodedText.length();
				int newCurChar = Math.min(1+endingBit.size(), plainText.length());  // last char or possibly first errant char
				if (curBit != newCurBit || curChar != newCurChar) {
					curBit = newCurBit;
					curChar = newCurChar;
				}
				draw();
			}
			
			public void jumpForward() {
				if (advanceBitwise) {  // decoding
					if (curBit < encodedText.length()) {
						if (curChar < endingBit.size() && curBit == endingBit.get(curChar))
							curChar++;
						curBit++;
					}
					if (curBit >= encodedText.length())
						controlPanel.stop();  // reached the end; make sure play stops (if running)
				} else {   // encoding
					int lastChar = Math.min(plainText.length(), 1+endingBit.size());
					if (curChar < lastChar) {
						curChar++;
						if (curChar <= endingBit.size())
							curBit = 1 + endingBit.get(curChar-1);
					}
					if (curChar >= lastChar)
						controlPanel.stop();  // reached the end; make sure play stops (if running)
				}
				draw();
			}
			
			public void jumpBackward() {
				if (advanceBitwise) {
					if (curBit > 0) {
						curBit--;
						if (curChar > 0 && curBit == endingBit.get(curChar-1))
							curChar--;
						draw();
					}
				} else {
					if (curChar > 0) {
						curChar--;
						curBit = (curChar == 0 ? 0 : 1 + endingBit.get(curChar-1));
						draw();
					}
				}
			}
			
		};
		
		controlPanel.addRemoteControlListener(controlListener);
		add(controlPanel);

		add(encodedTextField);
		title2 = new JLabel("encoded text");
		title2.setAlignmentX(CENTER_ALIGNMENT);
		add(title2);
		restartFromPlain();   // even though empty string
	}
	
	void setFontSize(int size) {
		Font currentFont = encodedTextField.getFont();
		Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), size);
		encodedTextField.setFont(newFont);
		plainTextField.setFont(newFont);
		message.setFont(newFont);
		newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), 3*size/4);
		title1.setFont(newFont);
		title2.setFont(newFont);
	}

	/*
	 * A version that deactivates DocumentListener temporarily when setting text field.
	 */
	void setPlainText(String text) {
		plainTextField.getDocument().removeDocumentListener(plainListener);
		plainTextField.setText(text);
		plainTextField.getDocument().addDocumentListener(plainListener);
	}

	/*
	 * A version that deactivates DocumentListener temporarily when setting text field.
	 */
	void setEncodedText(String text) {
		encodedTextField.getDocument().removeDocumentListener(encodedListener);
		encodedTextField.setText(text);
		encodedTextField.getDocument().addDocumentListener(encodedListener);
	}

	private void restartFromPlain() {
		advanceBitwise = false;
		plainText = plainTextField.getText();
		endingBit = new ArrayList<Integer>();
		StringBuilder build = new StringBuilder();
		int numBits = 0;
		Hashtable<String, String> lookup = master.symbolToCodeHash();
		for (int j=0; j < plainText.length(); j++) {
			// lookup table uses escaped special characters
			String original = HuffmanDemo.printableSymbol(""+plainText.charAt(j));
			String value = lookup.get(original);   // Need String not char
			if (value != null) {
				build.append(value);
				numBits += value.length();
				endingBit.add(numBits-1);  // index of last bit of current pattern
			} else {
				// invalid character
				break;
			}
		}
		encodedText = build.toString();
		controlListener.jumpToBeginning();  // set up appropriate display
		dump();
	}
	
	private void restartFromCoded() {
		advanceBitwise = true;
		encodedText = encodedTextField.getText();
		endingBit = new ArrayList<Integer>();
		StringBuilder build = new StringBuilder();
		Hashtable<String, String> lookup = master.codeToSymbolHash();
		String codeword = "";
		for (int j=0; j < encodedText.length(); j++) {
			char ch = encodedText.charAt(j);
			if (ch != '0' && ch != '1') {
				// BAD character in encoded text field; truncate internal view ending there
				encodedText = encodedText.substring(0, 1+j);
				break;  // ignore rest
			}
			codeword += ch;
			String value = lookup.get(codeword);
			if (value != null) {  // reached a complete codeword
				build.append(HuffmanDemo.rawSymbol(value));
				endingBit.add(j);
				codeword = "";
			}
		}
		plainText = build.toString();
		controlListener.jumpToBeginning();  // set up appropriate display
		dump();
	}
	
	
	/*
	 * A one size fits all approach to displaying texts and highlights,
	 * redoing from scratch, rather than taking advantage of existing state.
	 * If this turns out not to be fast enough, we can work harder to do more.
	 */
	private void draw() {
		if (HuffmanDemo.DEBUG > 1)
			System.out.println("curChar=" + curChar + ", curBit=" + curBit);
		
		if (advanceBitwise)
			setPlainText(plainText.substring(0,curChar));
		else
			setEncodedText(encodedText.substring(0, curBit));

		message.setForeground(Color.black);
		
		// clear highlights and rebuild
		h1.removeAllHighlights();
		h2.removeAllHighlights();
		boolean partialExists = false;
		boolean badLast = false;

		for (int j=0; j < curChar; j++) {
			try {
				Highlighter.HighlightPainter painter = (j >= endingBit.size() ? errorPainter : p[j % p.length]);
				h1.addHighlight(j, j+1, painter);
				if (painter.equals(errorPainter)){
					message.setText("Character '" + plainText.charAt(j) + "' does not appear in the alphabet");
					message.setForeground(Color.red);
					badLast = true;
				}
				if (j < endingBit.size()) {
					int start = (j == 0 ? 0 : endingBit.get(j-1));
					h2.addHighlight(start, 1+endingBit.get(j), p[j % p.length]);
				}
			} catch (BadLocationException e) { }
		}
		// look for any leftover bits for partially decoded segment
		int lastCharBit=0;
		if (curChar <= endingBit.size()) {
			lastCharBit = (curChar == 0 ? 0 : 1+endingBit.get(curChar-1));
			if (curBit > lastCharBit) {
				partialExists = true;
				try {
//					Highlighter.HighlightPainter painter = (curChar==plainText.length() ? errorPainter : p[curChar % p.length]);
					Highlighter.HighlightPainter painter = p[curChar % p.length];
					char ch = encodedText.charAt(curBit-1);
					if (ch == '0' || ch == '1') {
						h2.addHighlight(lastCharBit, curBit, painter);
					} else {
						badLast = true;
						h2.addHighlight(lastCharBit, curBit-1, painter);
						h2.addHighlight(curBit-1, curBit, errorPainter);
					}
				} catch (BadLocationException e) { }
			}
		}
		
		String suffix = "";
		if (partialExists) {
			if (badLast) {
				message.setText("Encoded text may only contain 0's and 1's");
				message.setForeground(Color.red);
				suffix = encodedText.substring(lastCharBit, curBit-1);
			} else {
				suffix = encodedText.substring(lastCharBit, curBit);
			}
		} else if (curChar > 0 && curChar <= endingBit.size()) {
			int begin = (curChar > 1 ? 1+endingBit.get(curChar-2) : 0);
			suffix = encodedText.substring(begin, 1+endingBit.get(curChar-1)); 
		}
		if (!suffix.equals("")) {
			int j = (partialExists ? curChar : curChar-1);
			Color color = colors[j % colors.length];
			if (!partialExists) {
				master.highlightSymbol(plainText.substring(curChar-1, curChar), color);
				if (advanceBitwise)
					message.setText("Code " + suffix + " decoded as '" + plainText.charAt(curChar-1) + "'");
				else
					message.setText("Encoding '" + plainText.charAt(curChar-1) + "' as " + suffix);
			} else if (!badLast) {
				message.setText("Partial code " + suffix + " not yet complete");
			}
			master.highlightPath(suffix, color);
		} else {
			master.highlightSymbol(null,  Color.BLACK);
			master.highlightPath("", Color.BLACK);
			if (!badLast) {
				if (plainText.length() == 0 && encodedText.length() == 0) {
					message.setText("Input plaintext or encoded text to begin");
				} else {
					message.setText("Ready to " + (advanceBitwise ? "decode" : "encode"));
				}
			}
		}
	}
	
	// DEBUGGING USE ONLY
	private void dump() {
		if (HuffmanDemo.DEBUG > 0) {
			String first = "";
			String second = "";
			for (int j=0; j < plainText.length(); j++) {
				String raw = HuffmanDemo.printableSymbol(plainText.substring(j,  j+1));
				String code = "";
				if (j < endingBit.size()) {  // there exists a code for this
					int i = (j == 0 ? 0 : 1+endingBit.get(j-1));
					code = encodedText.substring(i, 1 + endingBit.get(j));
				}
				int width = 1 + Math.max(raw.length(),code.length());
				first += String.format("%1$-" + width + "s", raw);
				second += String.format("%1$-" + width + "s", code);
			}
			
			// look for leftover bits
			int stop = (endingBit.size() > 0 ? endingBit.get(endingBit.size()-1) : 0);
			if (encodedText.length() > 1+stop)
					second += encodedText.substring(1+stop);
			
			System.out.println("Plain: " + first);
			System.out.println("Coded: " + second);
		}
	}

	public void setModel(DataModel m) {
		plainTextField.setText("");
	}
	
}
