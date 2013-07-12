import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface ReceiverRef specifies the Java RMI remote interface for a
 * distributed Receiver object in the Message Queue system.
 */
public interface ReceiverRef extends Remote {
	/**
	 * This method is called by the MQserver program to forward the messages
	 * received from the sender
	 * 
	 * @param message
	 *            the message object which sender sends to the receiver.
	 * 
	 * @return null
	 * 
	 * @exception RemoteException
	 *                Thrown if a remote error occurred.
	 */
	public void getMessage(Message message) throws RemoteException;

}