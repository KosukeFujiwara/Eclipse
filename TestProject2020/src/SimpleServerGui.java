import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServerGui {

	static final int PORTNUM = 49153;

	public static void main(String[] args) {

		System.out.println("クライアントの接続を待っています。");

		try(ServerSocket server = new ServerSocket(PORTNUM);
				Socket client = server.accept();
				PrintWriter out = new PrintWriter(client.getOutputStream(),true);)
		{
			System.out.println("クライアントが接続しました。");
			out.println("Hello, client!");
			out.println("接続ありがとうございます。");
			out.println("サーバーからメッセージをお送りします。");
			out.println("Good bye!");
			client.close();
		}catch(IOException e) {
			System.out.println("エラーが発生しました : " + e);
		}
	}
}
