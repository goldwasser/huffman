import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Validator for parsing symbol-frequency pairs.
 * 
 * If valid, produces ValidatorResult having result type LinkedHashMap&lt;String,Integer&gt;.
 * 
 * Entries should be given one per line using format as:
 * <pre>
 *    a 25
 *    b 76
 *    e 135
 * </pre>
 *
 * Blank lines are ignored.  Lines with only a frequency are presumed
 * to have string " " as the symbol.
 * 
 * FreqValidator ensures that symbols are unique and frequencies are positive integers.
 * 
 * Note that symbols can be any distinct strings (not just characters).
 */
public class FrequencyValidator extends TwoColumnValidator {

	/**
	 * Parses symbol-frequency data.
	 * 
	 * Assumes format described in class overview.
	 * @param original String
	 * @return LinkedHashMap&lt;String,Integer&gt; describing frequency values
	 * @throws ValidatorException if frequencies are not positive integers, or if
	 * any error conditions detected through TwoColumnValidator.parse(String)
	 */
	@Override
	public LinkedHashMap<String, Integer> parse(String original) throws ValidatorException {
		LinkedHashMap<String, String> pairs = (LinkedHashMap<String,String>) super.parse(original);
		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (Entry<String,String> entry : pairs.entrySet()) {
			Integer i = null;
			try {
				i = Integer.parseInt(entry.getValue());
			} catch (NumberFormatException e) { }
			if (i == null || i.intValue() <= 0)
				throw new ValidatorException("Illegal Frequency: " + entry.getValue());
			result.put(entry.getKey(), i);
		}
		return result;
	}
}
