package com.example.wuziqi;


import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;




public class server {

    //private BufferedReader in;
    private ObjectInputStream inObj;
    private PrintWriter out;
    private List<Socket> sockets=new ArrayList<Socket>();
    private Map<Socket,Socket> maps=new HashMap<Socket, Socket>();
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new server();
    }
    public server(){
        try {
            ServerSocket serverSocket=new ServerSocket(1888);
            System.out.println("伺服器運行成功");
            Socket client=null;
            BufferedReader in=null;
            while(true){
                client=serverSocket.accept();
                sockets.add(client);
                in=new BufferedReader(new InputStreamReader(client.getInputStream(),"UTF-8"));
                System.out.println("ip:"+client.getRemoteSocketAddress().toString());
                new Thread(new ReceiveThread(client,in)).start();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("伺服器運行異常");
            e.printStackTrace();
        }
    }
    class ReceiveThread implements Runnable{
        private Socket socket;
        private BufferedReader br;
        public ReceiveThread(Socket socket,BufferedReader br){
            this.socket=socket;
            this.br=br;

        }
        @RequiresApi(api = Build.VERSION_CODES.N)
        public void run() {
            // TODO Auto-generated method stub

            String msg=null;

            while(true){
                try {
                    while((msg=br.readLine())!=null){
                        System.out.println("收到客戶端訊息:"+msg);
                        handleClientMessage(msg,socket);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handleClientMessage(String msg, Socket socket){
        int index=-1;
        Socket value=null;
        if(msg.equals("match")){

            value=getValidUser(socket);
            if(value==null){
                send(socket, "没有匹配到玩家");
            }else{

                maps.put(socket, value);

                int r=getRandom(2);
                System.out.println("r="+r);
                if(r==0){
                    send(socket,"你先手");
                    send(value,"對方先手");
                }
                else if(r==1){
                    send(socket, "對方先手");
                    send(value, "你先手");
                }
            }

        }else if(msg.equals("match_failed")){
            sockets.remove(socket);
        }else if(msg.equals("exit") || msg.equals("leave")){

            send(getMyMatch(socket), msg);
            deleteMatch(socket);
            sockets.remove(socket);
            Thread.currentThread().interrupt();//终止线程

        }else if((index=msg.indexOf(","))>-1){
            send(getMyMatch(socket), msg.substring(0, index)+","+msg.substring(index+1));

        }else if(msg.startsWith("chat")){

            send(getMyMatch(socket), msg);
        }else if(msg.equals("timeout_no_play")){
            send(getMyMatch(socket), "oppo_timeout");
        }else if(msg.equals("continue")){
            int r=getRandom(2);
            System.out.println("r="+r);
            if(r==0){
                send(socket,"你先手");
                send(getMyMatch(socket),"對方先手");
            }
            else if(r==1){
                send(socket, "對方先手");
                send(getMyMatch(socket), "你先手");
            }
        }

    }

    private Socket getMyMatch(Socket socket){
        for(Map.Entry<Socket, Socket> entry : maps.entrySet()){
            if(entry.getKey()==socket){
                return entry.getValue();
            }else if(entry.getValue()==socket){
                return entry.getKey();
            }
        }
        return null;
    }
    private int getRandom(int max){
        Random r=new Random();
        return r.nextInt(max);
    }
    private Socket getValidUser(Socket socket){
        Socket value=null;
        Socket temp=null;

        for(int i=0;i<sockets.size();i++){
            temp=sockets.get(i);
            if(temp!=socket && isMatchOther(temp)==false && isMatchOther(socket)==false){
                value=temp;
                break;
            }
        }
        return value;
    }


    private boolean isMatchOther(Socket socket){
        for(Map.Entry<Socket, Socket> entry : maps.entrySet()){
            if(entry.getKey()==socket || entry.getValue()==socket){
                return true;
            }


        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteMatch(Socket socket){

        for(Map.Entry<Socket, Socket> entry : maps.entrySet()){
            Socket key=entry.getKey();
            Socket value=entry.getValue();
            if(key!=null && key==socket){

                maps.remove(key,value);


            }
            if(value!=null && value==socket){

                maps.remove(key,value);

            }

        }

    }

    private void send(Socket socket,String msg){
        try {

            if(socket!=null){
                System.out.println("發送->"+msg);
                out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8")));

                out.println(msg);
                out.flush();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
