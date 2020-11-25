import java.io.Serializable;

public class SendDataClient implements Serializable{

	private String name;
	private int userNo;
	private String message;
	private Chatinstruction instruction;
	private Clientstatus clientStatus;

	public SendDataClient (String name, int userNo, Chatinstruction instruction, Clientstatus clientStatus) {
		this.name = name;
		this.userNo = userNo;
		message = "";
		this.instruction = instruction;
		this.clientStatus = clientStatus;
	}

	public SendDataClient(String name, int userNo, String message, Chatinstruction instruction, Clientstatus clientStatus) {
		this.name = name;
		this.userNo = userNo;
		this.message = message;
		this.instruction = instruction;
		this.clientStatus = clientStatus;
	}

	public String getName() {
		return name;
	}

	public int getUserNo() {
		return userNo;
	}

	public String getMessage() {
		return message;
	}

	public Chatinstruction getChatinstruction() {
		return instruction;
	}

	public Clientstatus getClientstatus() {
		return clientStatus;
	}

	public String toString() {
		return "name:" + name + "userNo:" + userNo + "     message:" + message + "     Chatinstruction:" + instruction +
				"     clientStatus:" + clientStatus + " \n";
	}
}
