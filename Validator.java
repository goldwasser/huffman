/**
 * An interface to represent the strategy for parsing and validating input
 * received by an InputDialog.
 */
public interface Validator {
	/**
	 * Parses a raw String from an input text area.
	 * 
	 * @param raw String that was entered in a text area
	 * @return Object representing the parsed information
	 * @throws ValidatorException if input was invalid
	 */
	public Object parse(String raw) throws ValidatorException;
}
