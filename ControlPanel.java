import javax.swing.JComponent;
import javax.swing.JPanel;


public class ControlPanel extends JPanel implements Runnable{
	
	synchronized public void add(JComponent component) {
		super.add(component);
	}
	public void run() {
		
	}

}
