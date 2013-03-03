import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * Validator for parsing symbol-code pairs.
 * 
 * Returns LinkedHashMap&lt;String,String&gt; representing codebook.
 *
 * Entries should be given one per line using format as: 
 * <pre>
 *    a 01
 *    b 00
 *    e 1
 * </pre>
 * 
 * Blank lines are ignored.  Lines with only a frequency are presumed
 * to have string " " as the symbol.
 * 
 * Dialog ensures that symbols are unique and codes are prefix-free.
 * 
 * Note that symbols can be any distinct strings (not just characters).
 */
public class CodebookValidator extends TwoColumnValidator {
	
	/**
	 * Parses symbol-codeword data.
	 * 
	 * Assumes format described in class overview.
	 * @param original String
	 * @return LinkedHashMap&lt;String,String&gt; describing codewords for each symbol
	 * @throws ValidatorException if codewords not 0/1 strings, if codebook
	 * is not prefix-free, or if any error conditions detected through
	 * TwoColumnValidator.parse(String)
	 */
	@Override
	public LinkedHashMap<String,String> parse(String original) throws ValidatorException {
		LinkedHashMap<String, String> pairs = (LinkedHashMap<String, String>) super.parse(original);
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		for (Entry<String,String> entry : pairs.entrySet()) {
			String code = entry.getValue();

			if (!code.matches("[01]+"))
				throw new ValidatorException("Illegal Code: " + code);

			for (String other : result.values())
				if (code.startsWith(other) || other.startsWith(code))
					throw new ValidatorException("Ambiguous Codes: " + code + " " + other);

			result.put(entry.getKey(), code);
		}
		return result;
	}
}

