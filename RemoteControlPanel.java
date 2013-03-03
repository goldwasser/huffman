import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

public class RemoteControlPanel extends JPanel implements Runnable {
	private boolean run = false;  // boolean to denote whether currently in "play" mode
	private ArrayList<RemoteControlListener> listeners = new ArrayList<RemoteControlListener>();
	private JButton playNpause;
	
	public RemoteControlPanel() {
		final JButton resetButton = new JButton("<<");
		add(resetButton);
		final JButton backButton = new JButton("<");
		add(backButton);
		final ImageIcon play = new ImageIcon(getClass().getResource("Play16.gif"));
		play.setImage(play.getImage().getScaledInstance(20, -1, 0));
		final ImageIcon pause = new ImageIcon(getClass().getResource("Pause16.gif"));
		pause.setImage(pause.getImage().getScaledInstance(20, -1, 0));
		playNpause = new JButton(play);
		add(playNpause);
		final JButton forwardButton = new JButton(">");
		add(forwardButton);
		final JButton endButton = new JButton(">>");
		add(endButton);
		final Timer timer = new Timer(1000, new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				try{
					forwardButton.getActionListeners()[0].actionPerformed(new ActionEvent("", ActionEvent.ACTION_PERFORMED, ""));
				} catch (Exception ex){
					ex.printStackTrace();
				}
			}
		});
		timer.setInitialDelay(0);
		
		playNpause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!run) {
					run = true;
					playNpause.setIcon(pause);
					forwardButton.setEnabled(false);
					backButton.setEnabled(false);
					resetButton.setEnabled(false);
					endButton.setEnabled(false);
					timer.start();
//					System.out.println("play");
				}
				else {
					run=false;
					timer.stop();
					playNpause.setIcon(play);
					forwardButton.setEnabled(true);
					backButton.setEnabled(true);
					resetButton.setEnabled(true);
					endButton.setEnabled(true);
//					System.out.println("pause");
				}
			}
		});

		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (RemoteControlListener listener : listeners)
					listener.jumpForward();
			}
		});
		
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				for (RemoteControlListener listener : listeners)
					listener.jumpBackward();
			}
		});
		

		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				for (RemoteControlListener listener : listeners)
					listener.jumpToBeginning();
			}
		});
			
		
		endButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				for (RemoteControlListener listener : listeners)
					listener.jumpToEnd();
			}
		});
		
		
	}
		
	/*
	 * This may be called by client code if a jumpForward call
	 * is made that has outcome of reaching the end step.
	 * 
	 * The jumpForward may have been the result of a recurring Timer.
	 * This requests that an active timer be deactiviated.
	 */
	public void stop() {
		if (run)
			playNpause.getActionListeners()[0].actionPerformed(new ActionEvent("", ActionEvent.ACTION_PERFORMED, ""));
	}

	public void addRemoteControlListener(RemoteControlListener listener) {
		listeners.add(listener);
	}


	public void run() {

	}
}
