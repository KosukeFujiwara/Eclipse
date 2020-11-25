import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
	static final int PORTNUM = 49154;
	static final int DEFAULT_USER_NO = 9999;

	static int userNum = 0;
	static String chatMessage = "";

	static Chatserverstatus status = Chatserverstatus.USER0;

	public static void main(String...args) {
		System.out.println("サーバ起動開始");

		try(ServerSocket server = new ServerSocket(PORTNUM);) {
			while(true) {
				Socket client = server.accept();

				Thread thread = new Thread(new Runnable() {
					//Socket client;
					public void run() {
						try(ObjectInputStream in = new ObjectInputStream(client.getInputStream());
							ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
							){
							SendDataClient sdInput = (SendDataClient)in.readObject();
							SendDataServer sdOutput;
							Chatinstruction instruction = sdInput.getChatinstruction();

							//問い合わせ内容に応じて処理を実行
							if(instruction == Chatinstruction.CONNECSERVERT){
								userNum++;
								switch(status) {
								case USER0:
									status = Chatserverstatus.USER1;
									break;
								case USER1:
									status = Chatserverstatus.CHAT;
									break;
								}

								sdOutput = new SendDataServer(userNum, status);
								out.writeObject(sdOutput);
							}else if(instruction == Chatinstruction.SENDMESSAGE) {
								//チャット用の文字列を作成して追加
								String addMessage = sdInput.getName() + "  ＞  " + sdInput.getMessage() + " \n";
								chatMessage = chatMessage + addMessage;
								sdOutput = new SendDataServer(userNum, status, chatMessage);
								out.writeObject(sdOutput);
							}else if(instruction == Chatinstruction.CHECKSTATIONARY) {
								//現在のステータスとメッセージを送信
								sdOutput = new SendDataServer(userNum, status, chatMessage);
								out.writeObject(sdOutput);
							}
							client.close();
						}catch(Exception e) {
							System.err.println("ServerThreadでエラーが発生しました:" + e);
						}
					}
				});
							thread.start();
			}
		}catch (IOException e) {
			System.err.println("ServerSocketでエラーが発生しました:" + e);
		}
	}
}
