/*
 * Copyright 2013, Michael H. Goldwasser and Nicholas Brown.
 *
 * This file is part of the Huffman Coding Demonstration.
 *
 * The Huffman Coding Demonstration is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
