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
