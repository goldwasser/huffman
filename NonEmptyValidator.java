import java.awt.Color;

import javax.swing.JFrame;

/**
 * An input dialog that accepts and returns any non-empty string
 * entered by the user.
 */
public class NonEmptyValidator implements Validator {

	/**
	 * Returns the original string, if non-empty.
	 * @param original String
	 * @return original String
	 * @throws ValidatorException if an empty string
	 */
	public String parse(String original) throws ValidatorException {
		if (original.isEmpty())
			throw new ValidatorException("You must enter text to submit");
		return original;
	}
}
