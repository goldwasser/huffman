import java.awt.Color;
import java.util.LinkedHashMap;

/**
 * An abstract class to provide support for validating dialogs
 * in which the entered text is expected to represent some form
 * of key/value pairs, one per line, separated by one or more spaces.
 * 
 * Lines that have only whitespace will be ignored. Lines with only
 * one token will be interpreted as having a single space (" ") as the
 * input string, and the remaining token as the key. Lines with two
 * tokens will be interpreted as key/value pairs.
 * 
 * Inputs having a line with more than two tokens, or two or more lines
 * using the same key, will be considered invalid.
 *
 * Parse returns an instance of LinkedHashMap&lt;String, String&gt;. 
 */
public abstract class TwoColumnValidator implements Validator {
	/**
	 * Parses the original text to form a map from string to string.
	 * 
	 * Relies on input conventions documented in the class overview.
	 * 
	 * @param original contents of the text area
	 * @return LinkedHashMap&lt;String,String&gt; instance with pairs given in order
	 * that they were entered.
	 * @throws ValidatorException if any line has more than two tokens,
	 *  or if there are no nonempty lines.
	 */
	public Object parse(String original) throws ValidatorException {
		LinkedHashMap<String, String> map = new LinkedHashMap<String,String>();
		String[] lines = original.split("\\n+");
		for (String line : lines) {
			String[] tokens = line.replaceFirst("^\\s+","").split("\\s+");

			if (tokens.length > 2)
				throw new ValidatorException("Invalid line: " + line);

			if (tokens.length == 0 || (tokens.length == 1 && tokens[0].isEmpty()))
				continue;
			
			String key,value;
			if (tokens.length == 2) {
				key = tokens[0];
				value = tokens[1];
			} else {
				key = " ";
				value = tokens[0];
			}

			if (map.containsKey(key))
				throw new ValidatorException("Duplicate key: " + key);

			map.put(key, value);
		}
		
		if (map.size() == 0)
			throw new ValidatorException("There must be at least one entry");
		
		return map;
	}
}
