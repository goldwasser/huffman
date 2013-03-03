import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * View of a data model for animating the Huffman algorithm.
 */
public class AlgorithmView extends JComponent implements ModelView {

	private int step;
	private double t;    // parameterizes animation from 0 to 1
	AlgorithmSnapshotPanel snapshot;
	
	/**
	 * method to change size of font
	 * @param size
	 */
	public void setFontSize(int size) {
		snapshot.setFontSize(size);
	}
	
	/*
	 * method to stop playing
	 */
	synchronized public void stop() {
		snapshot.setRun(false);
	}
	
	public AlgorithmView() {
		setLayout(new BorderLayout());
		
		JLabel status = new JLabel(" ");
		status.setHorizontalAlignment(JLabel.CENTER);
		JPanel buttonPanel = new JPanel();
		final JCheckBox check = new JCheckBox("animate");
		check.setSelected(true);
		buttonPanel.add(check);
		
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		final JButton resetButton = new JButton("<<");
		buttonPanel.add(resetButton);
		final JButton backButton = new JButton("<");
		buttonPanel.add(backButton);
		final ImageIcon play = new ImageIcon(getClass().getResource("Play16.gif"));
		play.setImage(play.getImage().getScaledInstance(20, -1, 0));
		final ImageIcon pause = new ImageIcon(getClass().getResource("Pause16.gif"));
		pause.setImage(pause.getImage().getScaledInstance(20, -1, 0));
		final JButton playNpause = new JButton(play);
		buttonPanel.add(playNpause);
		final JButton forwardButton = new JButton(">");
		buttonPanel.add(forwardButton);
		final JButton endButton = new JButton(">>");
		buttonPanel.add(endButton);
		buttonPanel.add(Box.createHorizontalGlue());
		
		playNpause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!snapshot.getRun()) {
					snapshot.setRun(true);
					new Thread(snapshot).start();
					playNpause.setIcon(pause);
					if (HuffmanDemo.DEBUG > 0) System.out.println("play");
				}
				else {
					playNpause.setIcon(play);
					stop();
					if (HuffmanDemo.DEBUG > 0) System.out.println("pause");
				}
			}
		});
		
		snapshot = new AlgorithmSnapshotPanel(check, status, playNpause);
		
		JButton[] buttons = {resetButton, backButton, forwardButton, endButton};
		for (JButton b : buttons)
			b.addActionListener(snapshot);

		add(status, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		add(snapshot, BorderLayout.CENTER);

	}


	public void setModel(DataModel m) {
		snapshot.setModel(m);
	}
	
}

