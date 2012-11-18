/**************************************************************************
Author   :     Satish

Date     :     17th June 2010

File     :     SMSReceiver.java

Purpose  :     This class makes use of the SerialConnection class to connect
 			   to the GSM modem and listen to the serial port event to get all 
 			   kind of response.

Compile  :     JDK 1.6

Functions:     See JavaDoc

Note     :

History  :
Date        Version  Person      		Change
--------------------------------------------------------------------------
17th June 2010    0.1     Satish   	Initial Creation

Bug Fixes:
Date        Person      Bug Fix Details


TO DO:
-------------------------------------------------------------------------
1.
2.
3.

--------------------------------------------------------------------------

 ***************************************************************************/
package org.satish.gsm;

import javax.comm.SerialPort;

import org.satish.serial.SerialConnection;
import org.satish.serial.SerialParameters;

/**
 * This class makes use of the SerialConnection class to connect to the GSM
 * modem and listen to the serial port event to get all kind of response.
 * 
 * @author satish
 * 
 */
public class SMSReceiver implements Runnable {
	private SerialParameters defaultParameters = new SerialParameters("COM4",
			460800, SerialPort.FLOWCONTROL_NONE, SerialPort.FLOWCONTROL_NONE,
			8, 1, SerialPort.PARITY_NONE);
	SerialConnection mySerial = null;

	/**
	 * Starting point of the client to establish a connection to the modem and
	 * register the serial port events.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SMSReceiver smsReceiver = new SMSReceiver();
		smsReceiver.connect();
	}

	/**
	 * Creates a Serial Connection to the modem.
	 */
	public void connect() {
		try {
			SerialParameters params = defaultParameters;
			mySerial = new SerialConnection(params);
			mySerial.openConnection();
			Thread aThread = new Thread(this);
			aThread.start();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * Thread implementation for event listener.
	 */
	public void run() {
		try {
			Thread.sleep(5000);
			// }
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}
}
