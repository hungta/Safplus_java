package msgReceiver;

import msgUtils.MsgFns;
import com.sun.jna.Pointer;
import com.sun.jna.*;
import com.sun.jna.ptr.LongByReference;
import java.nio.charset.StandardCharsets;
import saAis.SaAisLibrary.SaAisErrorT;
import saAis.SaAisLibrary.SaNameT;
import saAis.SaAisLibrary;
import saMsg.SaMsgMessageT;

public class Receiver implements Runnable {
  /*private String[] queues = new String[50];
  private boolean unblockNow = false;
  private String appName = new String(); 
  public Receiver(String[] queues, String appName, boolean unblockNow) {
    if (queues.length > this.queues.length)
      throw new IllegalArgumentException("Wrong array size !");
    this.queues = queues;
    this.unblockNow = unblockNow;
    this.appName = appName;
  }*/
  @Override
  public void run() {
    /*int count =0;
    String msg = new String();
    while (!unblockNow) {
        count++;        
        msg = String.format("Msg [%4d] from [%s]", count, appName);
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        Pointer p = new Memory(bytes.length);
        p.write(0, bytes, 0, bytes.length);
        //clprintf(CL_LOG_SEV_INFO,"csa104: Sending Message: %s",msg);
        for (int i=0; queues[i].length()>0;i++) {       
          MsgFns.msgSend(queues[i],p,bytes.length);
        }        
        MsgFns.msgSend("nonExistentQueue",p,bytes.length);
        try {
          Thread.sleep(2000); // sleep for 2000 ms = 2 second
        }catch (InterruptedException e) {
           Thread.currentThread().interrupt();
        }        
     }*/
    
    
    int rc;
    saAis.SaAisLibrary.SaNameT senderName = new saAis.SaAisLibrary.SaNameT();
    Pointer data = new Memory(1024);
    LongByReference sendTime = new LongByReference();
    LongByReference senderId = new LongByReference();
    int curQ=0;
    while (true) {       
        saMsg.SaMsgMessageT message = new saMsg.SaMsgMessageT();
        message.size       = 1024;
        message.senderName = senderName.getPointer();
        message.data       = data;        
       
        rc = msgUtils.MsgFns.saMsgLib.saMsgMessageGet (msgUtils.MsgFns.msgQueueHandle[curQ].getValue(), message, sendTime, senderId, 100);        
        if (saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK != rc) {
          //clprintf ( CL_LOG_SEV_ERROR, "Msg saMsgMessageGet failed [0x%X]\n\r", rc );
        }
        else {
          senderName.read();          
          if (senderName.length > 0) {
            String sender = new String(senderName.value, StandardCharsets.UTF_8);
            //clprintf ( CL_LOG_SEV_INFO, "Sender Name   : %s\n", sender);
          }          
          byte[] bytes = message.data.getByteArray(0,(int)message.size);
          String msg = new String(bytes, StandardCharsets.UTF_8);
          //clprintf ( CL_LOG_SEV_INFO, "Received Message on [%s]  : %s\n", MsgFns.queues[curQ], msg);         
        }

        curQ++;
        if (curQ>=MsgFns.numQueues) {
          curQ=0;
        }
    }
  }  
}

