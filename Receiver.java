import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;

/**
 * Class Receiver provides a Java RMI distributed receiver object in the MQ
 * system.
 * <P>
 * Usage: java Start Receiver <I>host</I> <I>port</I>
 * <I>serverName</I><I>receiverID</I><BR>
 * <I>host</I> = Registry Server's host <BR>
 * <I>port</I> = Registry Server's port <BR>
 * <I>serverName</I> = MQServer's name <BR>
 * <I>receiverID</I> = The ID of the receiverID <BR>
 */

public class Receiver implements ReceiverRef {

	public String host;
	public int port;
	public String servername;
	public int senderID;
	public int receiverID;
	public String messagetext;

	public Receiver() {

	}

	/**
	 * Construct a new Receiver object.
	 * <P>
	 * The command line arguments are: <BR>
	 * <TT>args[0]</TT> = Registry Server's host <BR>
	 * <TT>args[1]</TT> = Registry Server's port <BR>
	 * <TT>args[2]</TT> = Name of the MQServer to connect<BR>
	 * <TT>args[3]</TT> = ID of the Receiver<BR>
	 * 
	 * @param args
	 *            Command line arguments.
	 * 
	 * @exception NotBoundException
	 *                Thrown if the MQServer is not found.
	 * @exception IOException
	 *                Thrown if an I/O error or a remote error occurred.
	 */
	public Receiver(String[] args) throws IOException, NotBoundException {
		try {
			// Parse command line arguments.
			if (args.length != 4) {
				System.err.println("Invalid Number of Arguments");
				throw new IllegalArgumentException(
						"Usage: java Start Receiver <host> <port> <ServerName>");
			}
			host = args[0];
			port = parseInt(args[1], "port");
			servername = args[2];
			receiverID = parseInt(args[3], "receiverID");
			try {
				// Look up MQserver name in the Registry Server and receive the
				// message.
				RegistryProxy registry = new RegistryProxy(host, port);
				try {
					MQServerRef msgObj = (MQServerRef) registry
							.lookup(servername);

					ReceiverRef recMsgObj = (ReceiverRef) UnicastRemoteObject
							.exportObject(new Receiver(), 0);
					// we only have to send the receiverID here, hence senderID
					// = -1 and message is blank
					Message messageObj = new Message(-1, receiverID, "");
					msgObj.messageReceiver(recMsgObj, messageObj);
				} catch (NotBoundException e) {
					System.err.println("Server Not found");
					System.out.println("");
					usage();

				}
			} catch (RemoteException e) {
				System.err.println("Regisrty Not found");
				System.out.println("");
				usage();

			}
		} catch (Exception e) {
			usage();
			;
		}
	}

	/**
	 * This method is called by MQserver to forward the messages
	 * 
	 * @param message
	 *            The message object contains SenderID, ReceiverID, messageText
	 * 
	 * @return null
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public void getMessage(Message message) throws RemoteException {

		System.out.println("From " + message.senderID + ": " + "\""
				+ message.messageText + "\"");

	}

	/**
	 * Print a usage message.
	 */
	private static void usage() {
		System.err
				.println("Usage: java Start Receiver <host> <port> <servername> <receiverID>");
		System.err.println("<host> = Registry Server's host");
		System.err.println("<port> = Registry Server's port");
		System.err.println("<servername> = MQServer's name");
		System.err
				.println("<receiverID> = ID of the Receiver ( Must be an Integer )");
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
	 * @return Integer value of <TT>arg</TT>. * @exception
	 *         IllegalArgumentException Thrown if an IllegalArgumentException is
	 *         occurred.
	 */
	private static int parseInt(String arg, String name) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException exc) {
			System.out.println("Receiver: Invalid <" + name + ">: \"" + arg
					+ "\"");
			throw new IllegalArgumentException("MQServer: Invalid <" + name
					+ ">: \"" + arg + "\"");
		}
	}

}
