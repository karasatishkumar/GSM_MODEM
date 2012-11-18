/**************************************************************************
Author   :     Satish

Date     :     17th June 2010

File     :     SMSSender.java

Purpose  :     This class makes use of the SerialConnection class to connect
 			   to the GSM modem and send a message.

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

import java.util.Date;

import javax.comm.SerialPort;

import org.satish.serial.SerialConnection;
import org.satish.serial.SerialParameters;

/**
 * This java client makes use of <code>SerialConnection</code> class to
 * connect to the GSM modem connected to the COM4 port. After connecting to the
 * modem, this class makes use of the AT commands to send the message to the
 * particular recipient.
 * 
 * @author satish
 * 
 */
public class SMSSender implements Runnable {

	private static final long STANDARD = 500;
	private static final long LONG = 2000;
	private static final long VERYLONG = 20000;

	SerialConnection mySerial = null;

	static final private char cntrlZ = (char) 26;
	// String in, out;
	Thread aThread = null;
	private long delay = STANDARD;
	String recipient = null;
	String message = null;

	private String csca = "+919886005444"; // the message center
	private SerialParameters defaultParameters = new SerialParameters("COM4",
			460800, SerialPort.FLOWCONTROL_NONE, SerialPort.FLOWCONTROL_NONE,
			8, 1, SerialPort.PARITY_NONE);
	public int step;
	public int status = -1;
	public long messageNo = -1;

	/**
	 * Starting point of the client to initiate.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SMSSender smsSender = new SMSSender("+919886599334",
					"Sending message from java client.\nTesting with COM4.");
			smsSender.send();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * Constructer to set the recipient no and message to send.
	 * 
	 * @param recipient -
	 *            Recipient mobile no
	 * @param message -
	 *            The message to send
	 */
	public SMSSender(String recipient, String message) {

		this.recipient = recipient;
		this.message = message;

	}

	/**
	 * connect to the port and start the thread.
	 */
	public int send() throws Exception {

		SerialParameters params = defaultParameters;

		mySerial = new SerialConnection(params);

		mySerial.openConnection();

		aThread = new Thread(this);

		aThread.start();
		// log("start");

		return 0;
	}

	/**
	 * implement the thread, message / response via steps, handle time out..
	 * This method use the following steps send the message using AT commands.<br>
	 * <br>
	 * <code>
	 * &nbsp;&nbsp;&nbsp;&nbsp;ATZ //To check the GSM modem is connected or not <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;OK<br>
	 * <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;ATH0<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;OK<br>
	 * <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;AT+CMGF=1 // To set the text messaging mode<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;OK<br>
	 * <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;AT+CSCA="+8784892468966423" // To set the message center no<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;OK<br>
	 * <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;AT+CMGS="+919886599334" // To set the recipient no.<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;>Your message to send + [Ctrl+z]// To write the message to send and "Ctrl+z" to finish the message<br>
	 * <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;OK // message sent<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;CMGS:+919886599334<br>
	 * </code>
	 */

	public void run() {

		boolean timeOut = false;
		long startTime = (new Date()).getTime();

		while ((step < 7) && (!timeOut)) {
			// log(""+((new Date()).getTime() - startTime);
			// check where we are in specified delay
			timeOut = ((new Date()).getTime() - startTime) > delay;

			// if atz does not work, type to send cntrlZ and retry, in case a
			// message was stuck
			if (timeOut && (step == 1)) {
				step = -1;
				mySerial.send("" + cntrlZ);
			}

			// read incoming string
			String result = mySerial.getIncommingString();

			// log ("<- "+result+"\n--------");
			int expectedResult = -1;

			try {
				// log ("Step:"+step);

				switch (step) {
				case 0:

					mySerial.send("atz");
					delay = LONG;
					startTime = (new Date()).getTime();
					break;

				case 1:
					delay = STANDARD;
					mySerial.send("ath0");
					startTime = (new Date()).getTime();
					break;
				case 2:
					expectedResult = result.indexOf("OK");

					if (expectedResult > -1) {
						mySerial.send("at+cmgf=1");
						startTime = (new Date()).getTime();
					} else {
						step = step - 1;
					}
					break;
				case 3:
					expectedResult = result.indexOf("OK");

					if (expectedResult > -1) {
						mySerial.send("at+csca=\"" + csca + "\"");
						startTime = (new Date()).getTime();
					} else {
						step = step - 1;
					}

					break;
				case 4:
					expectedResult = result.indexOf("OK");

					if (expectedResult > -1) {
						mySerial.send("at+cmgs=\"" + recipient + "\"");
						startTime = (new Date()).getTime();
					} else {
						step = step - 1;
					}

					break;
				case 5:
					expectedResult = result.indexOf(">");

					if (expectedResult > -1) {
						mySerial.send(message + cntrlZ);
						startTime = (new Date()).getTime();
					} else {
						step = step - 1;
					}
					delay = VERYLONG;// waitning for message ack

					break;

				case 6:
					expectedResult = result.indexOf("OK");
					// read message number
					if (expectedResult > -1) {
						int n = result.indexOf("CMGS:");
						result = result.substring(n + 5);
						n = result.indexOf("\n");
						status = 0;

					} else {
						step = step - 1;
					}

					break;
				}
				step = step + 1;

				aThread.sleep(100);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mySerial.closeConnection();

		// if timed out set status

		if (timeOut) {
			status = -2;
			System.out.println("*** time out at step " + step + "***");
		}
	}
}