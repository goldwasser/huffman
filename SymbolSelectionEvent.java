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
