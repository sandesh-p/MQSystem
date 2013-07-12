import java.io.Serializable;
import java.rmi.RemoteException;

import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;

/**
 * Class Sender is a client program for sending a message. Any number of
 * instances may run simultaneously. Class Sender is serializable so it can be
 * passed in remote method calls.
 * <P>
 * Class Sender has the main program for sending message to the MQserver.
 * <P>
 * Usage: java Sender <I>host</I> <I>port</I>
 * <I>serverName</I><I>senderID</I><I>receiverID</I> "<I>messageText</I>" <BR>
 * <I>host</I> = Registry Server's host <BR>
 * <I>port</I> = Registry Server's port <BR>
 * <I>serverName</I> = MQServer's name <BR>
 * <I>senderID</I> = ID of the sender <BR>
 * <I>receiverID</I> = ID of the receiver <BR>
 * <I>messageText</I> = Message to be sent
 */
public class Sender implements Serializable {
	// Default constructor
	public Sender() {
	}

	/**
	 * 
	 * Send the message to the MQserver
	 * 
	 * @param args
	 *            Command line arguments.
	 * 
	 * @return null
	 * 
	 * @exception Exception
	 *                Thrown if exception is occured
	 */
	public static void main(String[] args) throws Exception {
		try {
			// Parse command line arguments.
			if (args.length != 6) {
				usage();
			}
			String host = args[0];
			int port = parseInt(args[1], "port");
			String serverName = args[2];
			int senderID = parseInt(args[3], "senderID");
			int receiverID = parseInt(args[4], "receiverID");
			String messageText = args[5];

			try {
				// Look up serverName in the Registry server and send the
				// Message.
				RegistryProxy registry = new RegistryProxy(host, port);

				try {

					MQServerRef msgObj = (MQServerRef) registry
							.lookup(serverName);

					// Create a Message object to send

					Message messageTosend = new Message(senderID, receiverID,
							messageText);
					// String messageTosend = (senderID + " " + receiverID + " "
					// + messageText);

					msgObj.messageSender(messageTosend);

					// Print the receiverID and the messageText.
					System.out.printf("To " + receiverID + ": " + "\""
							+ messageText + "\"");
				} catch (NotBoundException e) {
					System.err.println("Server Not found");
					usage();
				}
			} catch (RemoteException e) {
				System.err.println("Registry Server Not found");
				usage();
			}
		} catch (Exception e) {
			usage();
		}
	}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage() {
		System.err
				.println("Usage: java Sender <host> <port> <servername> <senderID> <receiverID> \"<message>\"");
		System.err.println("<host> = Registry Server's host");
		System.err.println("<port> = Registry Server's port");
		System.err.println("<servername> = MQServer's name");
		System.err
				.println("<senderID> = ID of the Sender ( Must be an Integer )");
		System.err
				.println("<receiverID> = ID of the Receiver ( Must be an Integer )");
		System.err.println("<message> = The message to send");
		System.exit(1);
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
	 *                (unchecked exception) Thrown if <TT>arg</TT> cannot be
	 *                parsed as an integer.
	 */
	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			System.out.println("Sender: Invalid <" + name + ">: \"" + arg
					+ "\"");
			throw new IllegalArgumentException("Sender: Invalid <" + name
					+ ">: \"" + arg + "\"");
		}
	}
}
