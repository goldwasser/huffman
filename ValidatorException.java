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
