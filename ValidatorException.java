/**
 * Exception used to denote a problem when validating input.
 * 
 * The result of getMessage() should be a string that can
 * be displayed to the user to explain the cause.
 */
public class ValidatorException extends Exception {

	/**
	 * Constructs an exception with the given error message
	 * @param msg String explaining the cause of the exception
	 */
	public ValidatorException(String msg) {
		super(msg);
	}
	
}
