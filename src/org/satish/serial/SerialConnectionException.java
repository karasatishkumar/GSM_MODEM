/**************************************************************************
Author   :     Satish

Date     :     12th June 2010

File     :     SerialConnectionException.java

Purpose  :     Custom exception for serial port connection.. 

Compile  :     JDK 1.6

Functions:     See JavaDoc

Note     :

History  :
Date        Version  Person      		Change
--------------------------------------------------------------------------
12th June 2010    0.1     Satish   	Initial Creation

Bug Fixes:
Date        Person      Bug Fix Details


TO DO:
-------------------------------------------------------------------------
1.
2.
3.

--------------------------------------------------------------------------

 ***************************************************************************/
package org.satish.serial;

/**
 * Custom exception for serial port connection
 * @author Satish
 *
 */
public class SerialConnectionException extends Exception {

	/**
	 * Constructs a <code>SerialConnectionException</code>
	 * with the specified detail message.
	 *
	 * @param   s   the detail message.
	 */
	public SerialConnectionException(String str) {
		super(str);
	}

	/**
	 * Constructs a <code>SerialConnectionException</code>
	 * with no detail message.
	 */
	public SerialConnectionException() {
		super();
	}
}
