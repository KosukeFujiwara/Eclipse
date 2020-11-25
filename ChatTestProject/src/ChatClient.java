import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ChatClient extends Application{
	static final String SERVERIP = "192.168.199.111"; /* localhost */ //127.0.0.1←自分自身に送る場合
	static final int PORTNUM = 49154;
	static final int DEFAULT_USER_NO = 9999;

	String handleName;
	int userNo = DEFAULT_USER_NO;
	Clientstatus status = Clientstatus.NONE;

	TextField textFieldHN = new TextField();
	Button btnJoin;
	TextField textFieldMessage = new TextField();
	Label lblStatus;
	TextArea textArea;

	Timer timer;
	CheckTimerTask timerTask;

	public static void main(String...args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception{
		stage.setTitle("Chat Client");
		stage.setWidth(600);
		stage.setHeight(600);

		lblStatus = new Label("未接続");
		lblStatus.setFont(new Font(30));

		//ハンドルネームの入力(ラベルとテキストフィールド)
		Label lblHN = new Label("ハンドルネーム：");
		textFieldHN.setPrefWidth(100);

		//
		textFieldHN.setPrefWidth(300);
		textFieldHN.setFont(new Font(30));

		HBox boxHN = new HBox();
		boxHN.getChildren().addAll(lblHN, textFieldHN);

		//ボタン「サーバーに接続」
		btnJoin = new Button("サーバに接続");
		btnJoin.setPrefWidth(240);
		btnJoin.setFont(new Font(30));
		btnJoin.setOnAction(event -> connectServer());

		//メッセージの入力（ラベルとテキストフィールド）
		Label lblMessage = new Label("メッセージ：");
		textFieldMessage.setPrefWidth(400);
		textFieldMessage.setFont(new Font(30));

		HBox boxMessage = new HBox();
		boxMessage.getChildren().addAll(lblMessage, textFieldMessage);

		//ボタン「メッセージ送信」
		Button btnMessage = new Button("メッセージ送信");
		btnMessage.setPrefWidth(240);
		btnMessage.setFont(new Font(30));
		btnMessage.setOnAction(event -> sendMessage());

		//会話の表示（テキストエリア）
		textArea = new TextArea();
		textArea.setMaxWidth(500);
		textArea.setMaxHeight(400);
		textArea.setFont(new Font(30));
		textArea.setEditable(false);

		VBox root = new VBox();
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(10,10,10,10));
		root.setSpacing(10.0);
		root.getChildren().addAll(lblStatus, boxHN, btnJoin, boxMessage, btnMessage, textArea);

		stage.setScene(new Scene(root));
		stage.show();
	}


	//サーバと通信してチャットに参加する
	private void connectServer() {
		handleName = textFieldHN.getText();

		if(!handleName.equals("") && status == Clientstatus.NONE)
			new TaskThread(Chatinstruction.CONNECSERVERT).start();
	}

	//サーバにメッセージを送る
	private void sendMessage() {
		if(status == Clientstatus.CHAT) {
			new TaskThread(Chatinstruction.SENDMESSAGE).start();
		}
	}

	//テキストエリア（会話内容）の更新
	private void updateTextArea(String str) {
		textArea.setText(str);
		textArea.positionCaret(str.length());
	}

	private void updateStatus(Clientstatus status) {
		//まずステータス更新
		this.status = status;

		//次にステータス更新に伴う処理を実行
		switch(this.status) {
		case WAIT:
			textFieldHN.setEditable(false);
			btnJoin.setDisable(false);
			lblStatus.setText("通信相手を探しています");
			startTimer();
			break;

		case CHAT:
			lblStatus.setText("通信が可能になりました。");
			break;

		}
	}

	private void startTimer() {
		timer = new Timer();
		timerTask = new CheckTimerTask();

		timer.schedule(timerTask, 1000 /*1秒おいて*/, 5000 /*5秒ごとにサーバへアクセス*/);
	}


	class CheckTimerTask extends TimerTask{
		@Override
		public void run() {
			new TaskThread(Chatinstruction.CHECKSTATIONARY).start();
		}
	}


	class TaskThread extends Thread{
		Chatinstruction inst;

		public TaskThread(Chatinstruction inst) {
			this.inst = inst;
		}

		@Override
		public void run() {
			System.out.println("スレッド　開始");

			switch(inst) {
			case CONNECSERVERT:
				connectServer();
				break;
			case CHECKSTATIONARY:
				checkStationary();
				break;
			case SENDMESSAGE:
				sendMessage();
				break;
			default:
			}

			System.out.println("スレッド　終了");
		}

			//サーバと通信してチャットに参加する
			private void connectServer() {
				try(Socket soc = new Socket(SERVERIP /*IPアドレス*/, PORTNUM/*ポート番号*/);){

					//サーバへのデータ送信
					ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
					SendDataClient DataOut = new SendDataClient(handleName, userNo, Chatinstruction.CONNECSERVERT, status);
					out.writeObject(DataOut);

					//サーバからのデータ受信
					ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
					SendDataServer DataIn = (SendDataServer) in.readObject();
					userNo = DataIn.getUserNo();
					System.out.println("userNo:" + userNo);

					//ステータス変更＆サーバの状況確認用スレッドの開始（の関数を呼ぶ）
					Runnable updater = new Runnable() {
						@Override
						public void run() {
							updateStatus(Clientstatus.WAIT);
						}
					};

					Platform.runLater(updater);

					out.close();
					in.close();
					soc.close();
				}catch (UnknownHostException e) {
					System.err.println("ホストのIPアドレスが判定できません:" + e);
				}catch (ClassNotFoundException e) {
					System.err.println("クラスが見つかりません:" + e);
				}catch (IOException e) {
					System.err.println("エラーが発生しました:" + e);
				}finally {
					System.out.println("スレッド　終了");
				}
			}

			//サーバへの定期的なアクセス
			private void checkStationary() {
				try(Socket soc = new Socket(SERVERIP /*locakhost*/, PORTNUM/*ポート番号*/);){

					//サーバへのデータ送信
					ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
					SendDataClient DataOut = new SendDataClient(handleName, userNo, Chatinstruction.CHECKSTATIONARY, status);
					out.writeObject(DataOut);

					//サーバからのデータ受信
					ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
					SendDataServer DataIn = (SendDataServer) in.readObject();
					String chatData = DataIn.getMessage();
					Chatserverstatus serverStatus = DataIn.getChatserverstatus();

					if(status == Clientstatus.CHAT && serverStatus == Chatserverstatus.CHAT) {

						//チャット中のとき、テキストエリアを更新する
						Runnable updater = new Runnable() {
							@Override
							public void run() {
								updateTextArea(chatData);
							}
						};

						Platform.runLater(updater);
					}else if(status == Clientstatus.WAIT && serverStatus == Chatserverstatus.CHAT) {

						//通信待ち状態で、サーバがチャット可能状態になったとき、ステータスをCHATに変更する
						Runnable updater = new Runnable() {
							@Override
							public void run() {
								updateStatus(Clientstatus.CHAT);
							}
						};

						Platform.runLater(updater);
					}

					//それ以外のときはなにもしない
					out.close();
					in.close();
					soc.close();

				}catch (UnknownHostException e) {
					System.err.println("ホストのIPアドレスが判定できません:" + e);
				}catch (ClassNotFoundException e) {
					System.err.println("クラスが見つかりません:" + e);
				}catch (IOException e) {
					System.err.println("エラーが発生しました:" + e);
				}finally {
					System.out.println("スレッド　終了");
				}
			}

			private void sendMessage() {
				try(Socket soc = new Socket(SERVERIP /*localhost*/, PORTNUM/*ポート番号*/);){

					//サーバへのデータ送信
					ObjectOutputStream out = new ObjectOutputStream(soc.getOutputStream());
					SendDataClient DataOut = new SendDataClient(handleName, userNo, textFieldMessage.getText(), Chatinstruction.SENDMESSAGE, status);
					out.writeObject(DataOut);

					//サーバからのデータ受信
					ObjectInputStream in = new ObjectInputStream(soc.getInputStream());
					SendDataServer DataIn = (SendDataServer)in.readObject();
					String chatData = DataIn.getMessage();
					Runnable updater = new Runnable() {
						@Override
						public void run() {
							updateTextArea(chatData);
						}
					};

					Platform.runLater(updater);

					out.close();
					in.close();
					soc.close();
				}catch (UnknownHostException e) {
					System.err.println("ホストのIPアドレスが判定できません:" + e);
				}catch (ClassNotFoundException e) {
					System.err.println("クラスが見つかりません:" + e);
				}catch (IOException e) {
					System.err.println("エラーが発生しました:" + e);
				}finally {
					System.out.println("スレッド　終了");
				}
			}
		}
	}


