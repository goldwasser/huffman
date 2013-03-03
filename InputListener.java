/**
 * A listener interface to monitor submission from an InputDialog
 */
public interface InputListener {
	/**
	 * Invoked when an InputDialog either accepts validated input, or
	 * when Cancel is indicated.
	 * 
	 * @param input An appropriate representation of the data that was inputed (or null if Cancel indicated)
	 */
	public void inputSubmitted(Object input);
}
