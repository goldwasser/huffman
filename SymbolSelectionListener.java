import java.util.EventListener;


public interface SymbolSelectionListener extends EventListener {

	/**
	 * Called whenever the symbol selection has changed.
	 * 
	 * This includes when a new symbol is selected or when
	 * newly set to no selection.
	 * @param e
	 */
	public void selectionChanged(SymbolSelectionEvent e);
}
