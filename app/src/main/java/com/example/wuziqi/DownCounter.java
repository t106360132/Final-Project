package com.example.wuziqi;

import android.os.CountDownTimer;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.StaticLayout;
import android.util.Log;
import android.widget.TextView;
import server.Match;

public class DownCounter {
	private static CountDownTimer timer;
	private static TextView mtvw;
	private static TextView mtvb;
	public static void newCounter(final TextView tvw,final TextView tvb){

		mtvw=tvw;
		mtvb=tvb;
		timer=new CountDownTimer(30000,1000) {
			
			@Override
			public void onTick(long arg0) {
				// TODO Auto-generated method stub	

				int n=(int)arg0/1000;
				String str=n+"";					
				if(n<10)
					str="0"+str;
				if(Match.isMatchSuccess()){					
					boolean isMe=Match.isMePlay();
					boolean isBlack=Match.isBlack();
					if((isBlack && isMe) || (isBlack==false && isMe==false))
						tvb.setText(str);
					else if((isBlack==false && isMe) || (isBlack && isMe==false))
						tvw.setText(str);										
				}else{					
					if(WZQUtils.getmIsWhite())
						tvw.setText(str);
					else
						tvb.setText(str);				
				}				
			}			
			@Override
			public void onFinish() {
				// TODO Auto-generated method stub

				if(Match.isMatchSuccess()){
					if(Match.isMePlay()){
						WZQUtils.setmIsGamOver(true);
						if(Match.isBlack()){
							WZQUtils.showWinDialog(WuziqiPanel.getmContext(), "你超時了，白棋勝利",false);
						}
						else{
							WZQUtils.showWinDialog(WuziqiPanel.getmContext(), "你超時了，黑棋勝利",false);
						}
						Match.sendSomething("timeout_no_play");
					}
				}else{
					if (WZQUtils.getmIsWhite()){					
						WZQUtils.setmIsGamOver(true);
						WuziqiPanel.setIsWitheWinner(false);					
						WZQUtils.showWinDialog(WuziqiPanel.getmContext(), "黑棋勝利，白棋超時",true);
						//cancelCounter();				
					}else {
						WZQUtils.setmIsGamOver(true);
						WuziqiPanel.setIsWitheWinner(true);
						WZQUtils.showWinDialog(WuziqiPanel.getmContext(), "白棋勝利，黑棋超時",true);
						//cancelCounter();
					}
				}
				cancelCounter();
				//timer.cancel();
			}
		};
		
	}
	public static void startCounter(){
		timer.start();
	}
	public static void cancelCounter(){		
		mtvw.setText("29");
		mtvb.setText("29");
		timer.cancel();
	}
	private static int getWhiteCount(){
		return Integer.parseInt(mtvw.getText().toString());
	}
	private static int getBlackCount(){
		return Integer.parseInt(mtvb.getText().toString());
	}
	
}
