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

