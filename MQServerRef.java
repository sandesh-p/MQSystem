import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;

/**
 * Interface MQServerRef specifies the Java RMI remote interface for a
 * distributed MQServer object in the Message Queue system.
 */
public interface MQServerRef extends Remote {
	/**
	 * This method is called by the sender program to send messages
	 * 
	 * @param message
	 *            the message which sender sends to the receiver.
	 * 
	 * @return null
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public void messageSender(Message message) throws RemoteException;

	/**
	 * This method is called by the Receiver to receive messages
	 * 
	 * 
	 * @param Message
	 *            The message object which contains SenderID, ReceiverID and the
	 *            message
	 * 
	 * @return null
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public void messageReceiver(ReceiverRef RecObject, Message message)
			throws RemoteException;

	/**
	 * Add the given remote event listener to the MQServer. Whenever a activity
	 * is forwarded to this MQServer, this Server will report a ServerEvent to
	 * the given listener.
	 * 
	 * @param listener
	 *            Remote event listener.
	 */
	public Lease addListener(RemoteEventListener<ServerEvent> listener)
			throws RemoteException;

}