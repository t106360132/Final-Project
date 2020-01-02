package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.wuziqi.ClickEvent;
import com.example.wuziqi.DownCounter;
import com.example.wuziqi.MainActivity;
import com.example.wuziqi.WZQUtils;
import com.example.wuziqi.WuziqiPanel;

import android.R.bool;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Match {
	private static Socket socket=null;
	private static PrintWriter pw;
	private static BufferedReader in;
	private static String ip="170.20.10.9";
	private static int port=5050;
	private static boolean matchSuccess=false;
	private static boolean mePlay=false;
	private static boolean isBlack=false;
	private static int index=-1;
	private static Point point_rec;
	private static ExecutorService sendThreadPool=null;
	//private static boolean connectSuccess=false;
	private static String[] chats={"我誰，我瘋子","是在哈囉","全國發大財","拍攝者不救","禿子跟著月亮走","貨賣得出去，錢進得來","放眼世界征服宇宙","抱歉了高雄市民，我先選總統了"};
	public static boolean isMatchSuccess() {
		return matchSuccess;
	}
	public static void setMatchSuccess(boolean b) {
		matchSuccess=b;
	}
	
	public static boolean isMePlay() {
		return mePlay;
	}
	public static void setMePlay(boolean b) {
		mePlay=b;
	}
	public static boolean isBlack() {
		return isBlack;
	}

	public Match(Socket socket){	
	
	};
	
	public static void startMatch(String str){
		ip=str;
		if(socket==null){			
			new Thread(new ConnectThread()).start();								
		}

		
		for(int i=0;i<10;i++){			
			if(socket!=null) break;		
			SystemClock.sleep(1000);
		}
		if(socket==null){

			MainActivity.setPbVisibility(View.GONE);
			WZQUtils.showAlertDialog(WZQUtils.getmContext(), "匹配失敗", "無法連接服務器");
		}else{
			MainActivity.setPbVisibility(View.VISIBLE);
			Toast.makeText(WZQUtils.getmContext(), "匹配中......", 1).show();
			new Thread(new MatchThread()).start();				
			new Thread(new ReceiveThread()).start();


		}
				
	}
	public static void sendSomething(String msg){
		if(sendThreadPool==null){
			sendThreadPool=Executors.newSingleThreadExecutor();//发送消息的线程池	
		}		
		sendThreadPool.execute(new SendThread(msg));
		
	
	}

	static class SendThread implements Runnable{
		private String msg;
		public SendThread(String msg) {
			// TODO Auto-generated constructor stub
			this.msg=msg;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			send(msg);
		}		
	}
	
	static class ConnectThread implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub			
			try {
				socket=new Socket(ip,port);				
				pw=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8")));
				in=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			} 									
					
		}
		
	}
	static Handler handler=new Handler(){
		public void handleMessage(Message message){
			switch (message.what) {
			case 10:
				MainActivity.setPbVisibility(View.GONE);
				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "匹配失败","没有匹配到玩家");
				sendSomething("match_failed");
				break;
			case 11:
				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "匹配","匹配成功");
				matchSuccess=true;
				break;
			case 12:
				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "匹配","你已经在匹配中");
				break;
			case 13:
				matchSuccess=false;
				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "提示","对方离线");
				break;
			case 14:				
				WZQUtils.setmIsGamOver(false);
				mePlay=true;
				isBlack=true;
				matchSuccess=true;
				//System.out.println("我是黑棋");
				MainActivity.setPbVisibility(View.GONE);
				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "遊戲開始","你先手");
				WZQUtils.clearStone();
				WZQUtils.clearCurrentPoint();
				MainActivity.getwV().invalidate();
				//开始计时
				DownCounter.cancelCounter();
				DownCounter.startCounter();
				break;
			case 15:
				WZQUtils.setmIsGamOver(false);
				matchSuccess=true;
				isBlack=false;		
				mePlay=false;
				//System.out.println("我是白棋");
				MainActivity.setPbVisibility(View.GONE);
				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "遊戲開始","對方先手");
				WZQUtils.clearStone();
				WZQUtils.clearCurrentPoint();
				MainActivity.getwV().invalidate();
				break;
			case 16:				
				if(isBlack){
					WZQUtils.addWhiteArray(point_rec);				
				}
				else{
					WZQUtils.addBlackArray(point_rec);					
				}
				mePlay=true;
				DownCounter.cancelCounter();
				DownCounter.startCounter();
				MainActivity.getwV().invalidate();
				break;	
			case 17:				
				Toast.makeText(WZQUtils.getmContext(), chats[message.arg1], 1).show();
				break;
			case 18:	
				WZQUtils.setmIsGamOver(true);
				mePlay=false;
				WZQUtils.showWinDialog(WZQUtils.getmContext(), "對方超時", true);
				WZQUtils.writeCoins(1);
				break;
			case 19:	
				matchSuccess=false;

				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "提示", "對方離開");
				break;
			default:
				break;
			}
		}
	};
	static class MatchThread implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub	
			send("match");				
		}
		
	}
	static Handler handlerShow=new Handler(){
		public void handleMessage(Message message){
			switch (message.what) {
			case 20:
				WZQUtils.showAlertDialog(WZQUtils.getmContext(), "匹配失败", "無法連接伺服器");
				break;

			default:
				break;
			}
		}
	};
	static class ReceiveThread implements Runnable{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String msg=null;
			while(true){

				try {					

															
					while((msg=in.readLine())!=null){						
						System.out.println("收到伺服器訊息:"+msg);
						handleReceive(msg);

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}
	public static void handleReceive(String msg){
		Message message=new Message();
		if(msg.equals("沒有匹配到玩家")){


			for(int i=0;i<20;i++){
				SystemClock.sleep(1000);
				System.out.println(matchSuccess);
				if(matchSuccess) break;
			}
			if(matchSuccess==false) {
				message.what=10;
			}

		}

		else if(msg.equals("exit")){
			message.what=13;
		}
		else if(msg.equals("你先手")){																
			message.what=14;
		}
		else if(msg.equals("對方先手")){
			message.what=15;
		}
		else if((index=msg.indexOf(","))>-1){
			point_rec=new Point(Integer.parseInt(msg.substring(0, index)),Integer.parseInt(msg.substring(index+1)));
			System.out.println(point_rec);			
			message.what=16;
		}
		else if(msg.startsWith("chat")){
			message.what=17;
			message.arg1=Integer.parseInt(msg.substring(4));
		}
		else if(msg.equals("oppo_timeout")){
			message.what=18;
		}
		else if(msg.equals("leave")){
			message.what=19;
		}
	handler.sendMessage(message);
	}
	public static void send(String msg){	
		try {		

			if(socket!=null){
				pw.println(msg);
				pw.flush();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static Socket getSocket(){return socket;}
}
