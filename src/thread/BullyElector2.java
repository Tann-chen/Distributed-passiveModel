package thread;

import helper.Timeout;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BullyElector2 extends Thread{

    private int myBullyPort;
    private DatagramSocket datagramSocket;
    private InetAddress host;
    private final int FE_PORT=5000;


    public BullyElector2(int bullyPort){
        this.myBullyPort =bullyPort;
        try {
            datagramSocket = new DatagramSocket(bullyPort);
            host = InetAddress.getByName("localhost");
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1000];
            while(true){
                DatagramPacket bullyMessage = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(bullyMessage);
                String message=new String(bullyMessage.getData());

                if(message.trim().equals("$ELECTION")){//be notify to start election
                    if(bullyMessage.getPort()<myBullyPort && bullyMessage.getPort()!=FE_PORT)
                        sentMessage("$NO",bullyMessage.getPort());

                    String electionMessage="$ELECTION";
                    sentMessage(electionMessage,6003);

                    if (waiting()){
                        sentMessage("$PRIMARY",FE_PORT);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("-------");
        }finally {
            if(datagramSocket != null)
                datagramSocket.close();
        }
    }


    public void sentMessage(String content, int targetBullyPort){
        try {
            byte[] message = content.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(message, message.length, host,targetBullyPort);
            datagramSocket.send(replyPacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean waiting(){
        boolean flag=true;

        Timeout timeout=new Timeout(1000);
        timeout.startUp();
        while (timeout.flag){
            try{
                byte[] buffer = new byte[1000];
                DatagramPacket message = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(message);
                if(message.getPort()>myBullyPort)
                    flag=false;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return flag;
    }
}
