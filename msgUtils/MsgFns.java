package msgUtils;

import com.sun.jna.Pointer;
import saMsg.SaMsgCallbacksT;
import saAis.SaAisLibrary;
import saAis.SaAisLibrary.SaVersionT;
import saAis.SaAisLibrary.SaNameT;
import saAis.SaAisLibrary.SaAisErrorT;
import saMsg.SaMsgQueueCreationAttributesT;
import java.nio.charset.StandardCharsets;
import saMsg.SaMsgClientLibrary;
import com.sun.jna.ptr.LongByReference;
import saMsg.SaMsgClientLibrary.SaMsgQueueGroupChangesT;

public class MsgFns {
  //private static final String ACTIVE_COMP_QUEUE = "csa104msgqueue";
  public static final int QUEUE_LENGTH = 2048;
  public static final String queues[] = {"test1","test2", "abcd", "efghigke"};
  public static saMsg.SaMsgClientLibrary saMsgLib = saMsg.SaMsgClientLibrary.INSTANCE;
  private static LongByReference msgLibraryHandle = new LongByReference();
  public static LongByReference msgQueueHandle[] = new LongByReference[200];
  public static int numQueues = 0;
  
  public static int msgInitialize() {
    int rc;
    saMsg.SaMsgCallbacksT msgCallbacks = new saMsg.SaMsgCallbacksT(null,null,null,null);
    saAis.SaAisLibrary.SaVersionT ver = new saAis.SaAisLibrary.SaVersionT();
    ver.releaseCode = 'B';
    ver.majorVersion = 01;
    ver.minorVersion = 01;
    ver.write();
	  rc = saMsgLib.saMsgInitialize (msgLibraryHandle, msgCallbacks, ver.getPointer());
	  if (saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK != rc) {
		  assert false : String.format("Msg init failed [0x%X]", rc);      
    }
    return rc;
  }
  public static int msgOpen(String queueName, int bytesPerPriority) {
    int rc = saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK;
    saAis.SaAisLibrary.SaNameT saQueueName = new saAis.SaAisLibrary.SaNameT((short)queueName.length(), queueName.getBytes(StandardCharsets.UTF_8));
    saMsg.SaMsgQueueCreationAttributesT creationAttributes = new saMsg.SaMsgQueueCreationAttributesT();    
    
    int openFlags = saMsg.SaMsgClientLibrary.SA_MSG_QUEUE_CREATE;
    
    creationAttributes.creationFlags = 0;
    int i;
    for (i=0;i<saMsg.SaMsgClientLibrary.SA_MSG_MESSAGE_LOWEST_PRIORITY;i++) {
      creationAttributes.size[i] = bytesPerPriority;
    }
    creationAttributes.retentionTime = 0;

    rc = saMsgLib.saMsgQueueOpen(msgLibraryHandle.getValue(), saQueueName.getPointer(), creationAttributes, openFlags, saAis.SaAisLibrary.SA_TIME_END, msgQueueHandle[numQueues]);    
    numQueues++;
    return rc;
  }
  public static int msgSend(String queueName, Pointer buffer, int length) {
    int rc = saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK;
    saAis.SaAisLibrary.SaNameT saQueueName = new saAis.SaAisLibrary.SaNameT((short)queueName.length(), queueName.getBytes(StandardCharsets.UTF_8));
    saMsg.SaMsgMessageT message = new saMsg.SaMsgMessageT();   

    /* Load the SAF message structure */
    message.type = 0;
    message.version.releaseCode = 0;
    message.version.majorVersion=0;
    message.version.minorVersion=0;
    message.senderName = null;  /* You could put a SaNameT* in here if you wanted to pass a reply queue (for example) */
    message.size = length;
    message.data = buffer;
    message.priority = saMsg.SaMsgClientLibrary.SA_MSG_MESSAGE_HIGHEST_PRIORITY;
    rc = saMsgLib.saMsgMessageSend (msgLibraryHandle.getValue(), saQueueName.getPointer(), message, saAis.SaAisLibrary.SA_TIME_END);
    
    return rc;
  }
  /*public void msgReceiverLoop() {
    
  }*/
}
