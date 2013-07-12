import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEvent;
import edu.rit.ds.registry.RegistryEventFilter;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryProxy;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class Log is a client program for logging the MQServer's activities. Any
 * number of instances may run simultaneously. The Log program displays the
 * MQServer name and the number of incoming and outgoing messages on that server
 * 
 * Usage: java Log <I>host</I> <I>port</I><BR>
 * <I>host</I> = Registry Server's host <BR>
 * <I>port</I> = Registry Server's port <BR>
 * 
 */
public class Log {

	private static RegistryProxy registry;
	private static RegistryEventListener registryListener;
	private static RegistryEventFilter registryFilter;
	private static RemoteEventListener<ServerEvent> serverListener;

	public static void main(String[] args) throws Exception {
		// Parse command line arguments.
		if (args.length != 2)
			usage();
		String host = args[0];
		int port = parseInt(args[1], "port");

		try {
			// Get proxy for the Registry Server.
			registry = new RegistryProxy(host, port);

			// Export a remote event listener object for receiving notifications
			// from the Registry Server.

			registryListener = new RegistryEventListener() {
				public void report(long seqnum, RegistryEvent event) {
					listenToMQS(event.objectName());

				}
			};
			UnicastRemoteObject.exportObject(registryListener, 0);

			// Export a remote event listener object for receiving notifications
			// from MQserver objects.
			serverListener = new RemoteEventListener<ServerEvent>() {
				public void report(long seqnum, ServerEvent event) {
					// Print log report on the console.
					System.out.printf(event.serverName + ": " + event.incoming
							+ " incoming, " + event.outgoing + " outgoing\n");
				}
			};
			UnicastRemoteObject.exportObject(serverListener, 0);

			// Tell the Registry Server to notify us when a new MQServer object is
			// bound.
			registryFilter = new RegistryEventFilter().reportType("MQServer")
					.reportBound();
			registry.addEventListener(registryListener, registryFilter);

			// Tell all MQServer objects to notify us of its activity.
			for (String objectName : registry.list("MQServer")) {
				listenToMQS(objectName);
			}
		} catch (RemoteException e) {
			System.err.println("Registry Server Not found");
		}
	}

	/**
	 * Tell the given MQserver object to notify us of MQServer name and the count of
	 * incoming and outgoing messages.
	 * 
	 * @param objectName
	 *            MQServer object's name.
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	private static void listenToMQS(String objectName) {
		try {
			MQServerRef MQServerRefObj = (MQServerRef) registry.lookup(objectName);
			MQServerRefObj.addListener(serverListener);
		} catch (NotBoundException exc) {
		} catch (RemoteException exc) {
		}
	}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage() {
		System.err.println("Usage: java Log <host> <port>");
		System.err.println("<host> = Registry Server's host");
		System.err.println("<port> = Registry Server's port");
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
			System.err.println("Log: Invalid " +name+" "+ arg);
			usage();
			return 0;
		}
	}
}