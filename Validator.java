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
