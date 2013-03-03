import java.util.EventObject;

/**
 * Simple event type to designate a newly selected symbol.
 */
@SuppressWarnings("serial")
public class SymbolSelectionEvent extends EventObject {
	private String symbol;
	
	public SymbolSelectionEvent(Object src, String symbol) {
		super(src);
		this.symbol = symbol;
	}
	
	/**
	 * Returns the newly selected symbol (or null, if none selected).
	 * @return String symbol
	 */
	public String getSelectedSymbol() {
		return symbol;
	}

}
