import java.io.Serializable;

/**
 * Class Message is used to create message objects
 * 
 */
public class Message implements Serializable {

	int senderID;
	int receiverID;
	String messageText;

	/**
	 * Constructor to set the SenderID, ReceiverID and the MessageText
	 */
	Message(int senderID, int receiverID, String messageText) {
		this.senderID = senderID;
		this.messageText = messageText;
		this.receiverID = receiverID;

	}

}
