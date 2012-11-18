/**************************************************************************
Author   :     Satish

Date     :     12th June 2010

File     :     SerialConnection.java

Purpose  :     This class is responsible to set a connection to the serial port and also listen to events at the serial ports. 

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

import javax.comm.*;

import java.io.*;
import java.awt.TextArea;
import java.awt.event.*;
import java.util.TooManyListenersException;

/**
 * This class is responsible to build a connection to the serial port. As it
 * resister the serial port event and implement the serialEvent() method of the
 * SerialPortEventListener, when ever any event get generated the application
 * prints the response to the console. <br/> To open a connection to the serial
 * port, follow the bellow procedures. <br><code>
 * &nbsp;&nbsp;&nbsp;&nbsp;private SerialParameters defaultParameters = new SerialParameters("COM4", 460800, SerialPort.FLOWCONTROL_NONE, SerialPort.FLOWCONTROL_NONE, 8, 1, SerialPort.PARITY_NONE);
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;mySerial = new SerialConnection(params);
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;mySerial.openConnection();
 * <br/>			
 * </code>
 * 
 * @author Satish
 */
public class SerialConnection implements SerialPortEventListener {

	private SerialParameters parameters;/*
	 * This variable holds the connection
	 * parameters
	 */
	private OutputStream os;
	private InputStream is;

	private CommPortIdentifier portId;
	private SerialPort sPort;

	private boolean open;

	private String receptionString = "";

	/**
	 * This method takes the incoming response from serial port and get the
	 * <code>String</code> representation of that.
	 * 
	 * @return
	 */
	public String getIncommingString() {
		byte[] bVal = receptionString.getBytes();
		receptionString = "";
		return new String(bVal);
	}

	/**
	 * This constructer take the <code>SerialParameters</code> from user and
	 * crates a SerialConnection Object.
	 * 
	 * @param parameters -
	 *            Connection parameters
	 */
	public SerialConnection(SerialParameters parameters) {
		this.parameters = parameters;
		open = false;
	}

	/**
	 * Attempts to open a serial connection and streams using the parameters in
	 * the SerialParameters object. If it is unsuccesfull at any step it returns
	 * the port to a closed state, throws a
	 * <code>SerialConnectionException</code>, and returns.
	 * 
	 * Gives a timeout of 30 seconds on the portOpen to allow other applications
	 * to reliquish the port if have it open and no longer need it.
	 */
	public void openConnection() throws SerialConnectionException {

		try {
			System.out.println("Port Name : " + parameters.getPortName());
			/* Creating a CommPortIdentifier object using the port name */
			portId = CommPortIdentifier.getPortIdentifier(parameters
					.getPortName());
		} catch (NoSuchPortException e) {
			// System.out.println("Yes the problem is here 1 ");
			e.printStackTrace();
			// throw new SerialConnectionException(e.getMessage());
		} catch (Exception e) {
			// System.out.println("ErrorErrorErrorError");
			e.printStackTrace();
		}
		// System.out.println(portId);
		// System.out.println("OK 1 ");
		/*
		 * Open the port represented by the CommPortIdentifier object. Give the
		 * open call a relatively long timeout of 30 seconds to allow a
		 * different application to reliquish the port if the user wants to.
		 */
		try {
			/*
			 * Creating a serial port object using a identifier name and a port
			 * no. Kindly check if the port is free before using that.
			 */
			sPort = (SerialPort) portId.open("SMSConnector", 30000);
		} catch (PortInUseException e) {
			/* If the port is in use by any other process in the OS */
			throw new SerialConnectionException(e.getMessage());
		}
		sPort.sendBreak(1000);

		/*
		 * Set the parameters of the connection. If they won't set, close the
		 * port before throwing an exception.
		 */
		try {
			this.setConnectionParameters();
		} catch (SerialConnectionException e) {
			/* Close the port */
			sPort.close();
			throw e;
		}
		/*
		 * Open the input and output streams for the connection. If they won't
		 * open, close the port before throwing an exception.
		 */
		try {
			/* Open the IO streams connection on the connected port */
			os = sPort.getOutputStream();
			is = sPort.getInputStream();
		} catch (IOException e) {
			sPort.close();
			throw new SerialConnectionException("Error opening i/o streams");
		}
		try {
			/* Register to the serial port events. So to get the response. */
			System.out.println("Event Listener get added..");
			sPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			sPort.close();
			throw new SerialConnectionException("too many listeners added");
		}
		System.out.println("sPort.notifyOnDataAvailable(true)");
		/* Set notifyOnDataAvailable to true to allow event driven input. */
		sPort.notifyOnDataAvailable(true);

		System.out.println("sPort.notifyOnBreakInterrupt(true)");
		/* Set notifyOnBreakInterrup to allow event driven break handling. */
		sPort.notifyOnBreakInterrupt(true);

		/*
		 * Set receive timeout to allow breaking out of polling loop during
		 * input handling.
		 */
		try {
			sPort.enableReceiveTimeout(30);
		} catch (UnsupportedCommOperationException e) {
		}
		open = true;
	}

	/**
	 * Sets the connection parameters to the setting in the parameters object.
	 * If set fails return the parameters object to original settings and throw
	 * exception.
	 */
	public void setConnectionParameters() throws SerialConnectionException {

		/* Save state of parameters before trying a set. */
		int oldBaudRate = sPort.getBaudRate();
		int oldDatabits = sPort.getDataBits();
		int oldStopbits = sPort.getStopBits();
		int oldParity = sPort.getParity();
		int oldFlowControl = sPort.getFlowControlMode();

		/*
		 * Set connection parameters, if set fails return parameters object to
		 * original state.
		 */
		try {
			sPort.setSerialPortParams(parameters.getBaudRate(), parameters
					.getDatabits(), parameters.getStopbits(), parameters
					.getParity());
		} catch (UnsupportedCommOperationException e) {
			parameters.setBaudRate(oldBaudRate);
			parameters.setDatabits(oldDatabits);
			parameters.setStopbits(oldStopbits);
			parameters.setParity(oldParity);
			throw new SerialConnectionException("Unsupported parameter");
		}

		/* Set flow control. */
		try {
			sPort.setFlowControlMode(parameters.getFlowControlIn()
					| parameters.getFlowControlOut());
		} catch (UnsupportedCommOperationException e) {
			throw new SerialConnectionException("Unsupported flow control");
		}
	}

	/**
	 * Close the port and clean up associated elements.
	 */
	public void closeConnection() {
		/* If port is alread closed just return. */
		if (!open) {
			return;
		}

		/*
		 * Remove the key listener.
		 * messageAreaOut.removeKeyListener(keyHandler);
		 */

		/* Check to make sure sPort has reference to avoid a NPE. */
		if (sPort != null) {
			try {
				// close the i/o streams.
				os.close();
				is.close();
			} catch (IOException e) {
				System.err.println(e);
			}

			// Close the port.
			sPort.close();
		}

		open = false;
	}

	/**
	 * Send a one second break signal.
	 */
	public void sendBreak() {
		sPort.sendBreak(1000);
	}

	/**
	 * Reports the open status of the port.
	 * 
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Handles SerialPortEvents. The two types of SerialPortEvents that this
	 * program is registered to listen for are DATA_AVAILABLE and BI. During
	 * DATA_AVAILABLE the port buffer is read until it is drained, when no more
	 * data is available and 30ms has passed the method returns. When a BI event
	 * occurs the words BREAK RECEIVED are written to the messageAreaIn.
	 */

	public void serialEvent(SerialPortEvent e) {
		/* Create a StringBuffer and int to receive input data. */
		StringBuffer inputBuffer = new StringBuffer();
		int newData = 0;

		/* Determine type of event. */

		switch (e.getEventType()) {

		/*
		 * Read data until -1 is returned. If \r is received substitute \n for
		 * correct newline handling.
		 */
		case SerialPortEvent.DATA_AVAILABLE:
			while (newData != -1) {
				try {
					newData = is.read();
					if (newData == -1) {
						break;
					}
					if ('\r' == (char) newData) {
						inputBuffer.append('\n');
					} else {
						inputBuffer.append((char) newData);
					}
				} catch (IOException ex) {
					System.err.println(ex);
					return;
				}
			}

			/* Append received data to messageAreaIn. */
			receptionString = receptionString + (new String(inputBuffer));
			System.out.print("<-" + receptionString);
			break;

		// If break event append BREAK RECEIVED message.
		case SerialPortEvent.BI:
			receptionString = receptionString + ("\n--- BREAK RECEIVED ---\n");
		}

	}

	/**
	 * This method writes the data to the output stream.
	 * 
	 * @param message
	 */
	public void send(String message) {
		byte[] theBytes = (message + "\n").getBytes();
		for (int i = 0; i < theBytes.length; i++) {

			char newCharacter = (char) theBytes[i];
			if ((int) newCharacter == 10)
				newCharacter = '\r';

			try {
				os.write((int) newCharacter);
			} catch (IOException e) {
				System.err.println("OutputStream write error: " + e);
			}

		}

	}
}
