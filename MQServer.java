import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventGenerator;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.RegistryProxy;

/**
 * Class MQServer provides a Java RMI distributed MQServer object in the MQ
 * system.
 * <P>
 * Usage: java Start MQServer <I>host</I> <I>port</I> <I>ServerName</I> <BR>
 * <I>host</I> = Registry Server's host <BR>
 * <I>port</I> = Registry Server's port <BR>
 * <I>ServerName</I> = ID of this MQServer itself
 * 
 */
public class MQServer implements MQServerRef {
	private String host;
	private int port;
	private String mqServerName;
	private RegistryProxy registry;
	// HashMap to store the message objects which contains receiverID,senderID
	// and messageText,The message object
	// is used as the value and receiverID as
	// the key
	private HashMap<Integer, ArrayList<Message>> msgqueue = new HashMap<Integer, ArrayList<Message>>();
	// HashMap to store the Receiver objects,the receiver objects are used as
	// the value and receiverID as the key
	private HashMap<Integer, ReceiverRef> receiverlist = new HashMap<Integer, ReceiverRef>();
	private RemoteEventGenerator<ServerEvent> eventGenerator;
	// To count the incoming messages from the senders
	private int incomingCount = 0;
	// To count the outgoing messages from the MQServer
	private int outgoingCount = 0;

	/**
	 * Construct a new MQServer object.
	 * <P>
	 * The command line arguments are: <BR>
	 * <TT>args[0]</TT> = Registry Server's host <BR>
	 * <TT>args[1]</TT> = Registry Server's port <BR>
	 * <TT>args[2]</TT> = Name of the MQServer itself
	 * 
	 * 
	 * @param args
	 *            Command line arguments.
	 * 
	 * @exception Exception
	 *                Thrown if an exception is occurred.
	 * @exception IllegalArgumentException
	 *                Thrown if an IllegalArgumentException is occurred.
	 */
	public MQServer(String[] args) throws Exception {

		try {
			// Parse command line arguments.
			if (args.length != 3) {
				System.out.println("Invalid number of Arguments");
				throw new IllegalArgumentException(
						"Usage: java Start MQServer <host> <port> <Servername>");

			}
			host = args[0];
			port = parseInt(args[1], "port");
			mqServerName = args[2];
			try {
				// Get a proxy for the Registry Server.
				registry = new RegistryProxy(host, port);
				// Export object for MQserver.
				UnicastRemoteObject.exportObject(this, 0);

				// Prepare to generate remote events.
				eventGenerator = new RemoteEventGenerator<ServerEvent>();

				// Bind Message Queue into the Registry Server.
				try {
					registry.bind(mqServerName, this);
				} catch (AlreadyBoundException e) {
					try {
						UnicastRemoteObject.unexportObject(this, true);
					} catch (NoSuchObjectException e2) {
					}
					System.out.println("Server name already exists");
				} catch (RemoteException exc) {
					try {
						UnicastRemoteObject.unexportObject(this, true);
					} catch (NoSuchObjectException e) {
					}
					;
				}
			} catch (RemoteException e) {
				System.out.println("Registry Server Not found");
				usage();
			}
		} catch (Exception e) {
			usage();
		}
	}

	/**
	 * This method is called by Sender to forward the messages to the receiver,
	 * it is synchronized for multiple senders to access it simultaneously
	 * 
	 * @param message
	 *            The message object contains SenderID, ReceiverID, messageText
	 * 
	 * @return null
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public synchronized void messageSender(Message message)
			throws RemoteException {
		// TODO Auto-generated method stub

		if (receiverlist.containsKey(message.receiverID)) {

			ReceiverRef recref = (ReceiverRef) receiverlist
					.get(message.receiverID);
			try {
				recref.getMessage(message);

				// Split message

				System.out.println("From " + message.senderID + " to "
						+ message.receiverID + ": " + "\""
						+ message.messageText + "\"" + " delivered");
				incomingCount++;
				outgoingCount++;

			} catch (Exception e) {
				receiverlist.remove(message.receiverID);
				ArrayList<Message> msglist = new ArrayList<Message>();
				msglist.add(message);
				msgqueue.put(message.receiverID, msglist);
				System.out.println("From " + message.senderID + " to "
						+ message.receiverID + ": " + "\""
						+ message.messageText + "\"" + " queued");
				incomingCount++;

			}
			eventGenerator.reportEvent(new ServerEvent(mqServerName,
					incomingCount, outgoingCount));
		}

		else {
			// If the sender/message HashMap contains the receiverID
			if (msgqueue.containsKey(message.receiverID)) {

				ArrayList<Message> msgObj = msgqueue.get(message.receiverID);
				msgObj.add(message);
				msgqueue.put(message.receiverID, msgObj);
				System.out.println("From " + message.senderID + " to "
						+ message.receiverID + ": " + "\""
						+ message.messageText + " queued");
				incomingCount++;

			} else {
				ArrayList<Message> msglist = new ArrayList<Message>();
				msglist.add(message);
				msgqueue.put(message.receiverID, msglist);
				System.out.println("From " + message.senderID + " to "
						+ message.receiverID + ": " + "\""
						+ message.messageText + "\"" + " queued ");
				incomingCount++;

			}
			eventGenerator.reportEvent(new ServerEvent(mqServerName,
					incomingCount, outgoingCount));
		}

	}

	/**
	 * This method is called by the Receiver to receive the messages from the
	 * MQserver, it is synchronized for multiple receivers to access it
	 * simultaneously
	 * 
	 * @param recObj
	 *            The message contains the Receiver's unicastRemote object
	 * 
	 * @param Message
	 *            The message object contains SenderID, ReceiverID, messageText
	 * @return null
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public synchronized void messageReceiver(ReceiverRef recObj, Message message)
			throws RemoteException {
		// Insert the receiver object in the HashMap
		receiverlist.put(message.receiverID, recObj);

		// If the message queue contains the receiverID, fetch the ArrayList and
		// insert messages to that ArrayList
		if (msgqueue.containsKey(message.receiverID) == true) {
			ArrayList<Message> getmsg = msgqueue.get(message.receiverID);

			if (getmsg != null) {
				for (int i = 0; i < getmsg.size(); i++) {

					recObj.getMessage(getmsg.get(i));

					System.out.println("From " + getmsg.get(i).senderID
							+ " to " + getmsg.get(i).receiverID + ": " + "\""
							+ getmsg.get(i).messageText + "\"" + " delivered");
					outgoingCount++;
					// Report a ServerEvent to any remote event listeners.
					eventGenerator.reportEvent(new ServerEvent(mqServerName,
							incomingCount, outgoingCount));

				}
				msgqueue.remove(message.receiverID);

			}

		}

	}

	/**
	 * Parse an integer command line argument.
	 * 
	 * @param arg
	 *            Command line argument.
	 * @param name
	 *            Argument name.
	 * 
	 * @return Integer value of <TT>arg</TT>.
	 * 
	 * @exception IllegalArgumentException
	 *                Thrown if an IllegalArgumentException is occurred.
	 */

	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			System.out.println("MQServer: Invalid <" + name + ">: \"" + arg
					+ "\"");
			throw new IllegalArgumentException("MQServer: Invalid <" + name
					+ ">: \"" + arg + "\"");

		}
	}

	/**
	 * Print a usage message.
	 */
	private static void usage() {
		System.err
				.println("Usage: java Start MQServer <host> <port> <servername>");
		System.err.println("<host> = Registry Server's host");
		System.err
				.println("<port> = Registry Server's port ( Must be an Integer )");
		System.err.println("<servername> = MQServer's name");
		System.exit(1);

	}

	/**
	 * Add the given remote event listener to the MQServer. Whenever a activity
	 * is forwarded to this MQServer, this Server will report a ServerEvent to
	 * the given listener.
	 * 
	 * @param listener
	 *            Remote event listener.
	 */
	public Lease addListener(RemoteEventListener<ServerEvent> listener)
			throws RemoteException {
		return eventGenerator.addListener(listener);
	}

}
