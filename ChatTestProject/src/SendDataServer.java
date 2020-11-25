import java.io.Serializable;

public class SendDataServer implements Serializable {
	private int userNo;
	private Chatserverstatus serverStatus;
	private String message;

	public SendDataServer(int userNo, Chatserverstatus serverStatus) {
		this.userNo = userNo;
		this.serverStatus = serverStatus;
		message = "";
	}

	public SendDataServer(int userNo, Chatserverstatus serverStatus, String message) {
		this.userNo = userNo;
		this.serverStatus = serverStatus;
		this.message = message;
	}

	public int getUserNo() {
		return userNo;
	}

	public Chatserverstatus getChatserverstatus() {
		return serverStatus;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		return "userNo:" + userNo + "    message:" + "Chatserverstatus:" + serverStatus+" \n";
	}
}
