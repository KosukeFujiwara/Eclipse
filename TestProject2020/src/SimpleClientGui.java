import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class SimpleClientGui extends Application{

	static final int PORTNUM = 49153; //サーバ側で設定したポート番号
	Label lblMsg;

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception{
		stage.setTitle("Simple Client");
		stage.setWidth(240);
		stage.setHeight(300);

		lblMsg = new Label("");
		lblMsg.setFont(new Font(12));

		Button btnConn = new Button("サーバに接続");
		btnConn.setPrefWidth(120);
		btnConn.setOnAction(event -> connectServer());

		VBox root = new VBox();
		root.setAlignment(Pos.TOP_CENTER);
		root.setPadding(new Insets(10,10,10,10));
		root.setSpacing(10.0);
		root.getChildren().addAll(btnConn, lblMsg);

		stage.setScene(new Scene(root));
		stage.show();

	}

	private void connectServer() { //サーバーと通信する
		new TaskThread().start();
	}

	private void updateLabael(String str) {
		String brfStr = lblMsg.getText();
		lblMsg.setText(brfStr + "\n" + str);
	}

	class TaskThread extends Thread{
		@Override
		public void run() {
			System.out.println("スレッド　開始");

			try(Socket soc = new Socket("127.0.0.1"/* サーバのIPアドレス*/, PORTNUM/*サーバ側で設定したポート番号*/);
					){ //オートクローズ

						Scanner sc = new Scanner(soc.getInputStream());

						while(sc.hasNext()) {
						String message = sc.nextLine();

						System.out.println("Server : " + message);

						//別スレッドからJavaFXのUI（ここではラベル）を更新するひと手間

						Runnable updater = new Runnable() {
							@Override
							public void run() {
								updateLabael(message); //ここにUI更新の処理を書く
							}
						};

						Platform.runLater(updater); //ここまで

						if(message.equals("Good bye!")) {
							break;
						}
					}
					sc.close();
			}catch(UnknownHostException e) {
				System.err.println("ホストのIPアドレスが判定できません : " + e);
			}catch(IOException e) {
				System.err.println("エラーが発生しました : " + e);
			}finally {
				System.out.println("スレッド　終了");
			}
		}
	}

}
