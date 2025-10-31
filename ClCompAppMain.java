import com.sun.jna.*;
import com.sun.jna.ptr.*;
import saAmf.SaAmfLibrary;
import saAmf.SaAmfLibrary.*;
//import saAmf.SaAmfLibrary.SaAmfHAStateT.*;
import saAmf.SaAmfCallbacksT;
import saAmf.SaAmfCallbacksT.*;
import saAis.SaAisLibrary;
import saAis.SaAisLibrary.*;
import clEoLib.ClEoLibrary;
import clEoLib.ClEoLibrary.*;
import clUtils.*;
import clUtils.ClUtilsLibrary;
import libc.LibC;
import libc.LibC.*;
import libc.Errno;
import libc.Errno.*;
import java.lang.ProcessHandle;
//import errno.Errno;

public class ClCompAppMain {
  /*public static saAmf.SaAmfLibrary AMF_INSTANCE = SaAmfLibrary.INSTANCE;*/
  //public static saAmf.SaAmfLibrary UTILS =  SaAmfLibrary.UTILS_INST;
  //public static saAmf.SaAmfLibrary MW =  SaAmfLibrary.MW_INST;
  public static long mypid = 0;

  //public static long handleApp = 0;
  public static short svcId = 10;

  public static NativeLibrary mwNativeLib = clUtils.ClUtilsLibrary.MW_NATIVE_LIB;
  public static clUtils.ClUtilsLibrary utilsLib = clUtils.ClUtilsLibrary.UTILS_INSTANCE;
  public static NativeLibrary iocLib = NativeLibrary.getInstance("ClIoc");
  
  public static void clprintf(int severity, String fmtString, Object...varArgs)
  {
    StackTraceElement e = Thread.currentThread().getStackTrace()[1];
    //System.out.println(e.getFileName() + ":" + e.getLineNumber());
    Pointer symPtr = mwNativeLib.getGlobalVariableAddress("CL_LOG_HANDLE_APP");     
    long handleApp = symPtr.getLong(0);
    //short svcId = 10;
    //int clLogMsgWrite(long streamHdl, int severity, short serviceId, String pArea, String pContext, String pFileName, int lineNum, String pFmtStr, Object... varArgs1);
    utilsLib.clLogMsgWrite( handleApp,
                            severity,
                            svcId,
                            "---",
                            "---",
                            e.getFileName(),
                            e.getLineNumber(),
                            fmtString,
                            varArgs); 
    
  }
  public static saAmf.SaAmfLibrary saAmfLib = SaAmfLibrary.INSTANCE;

  //public static saAmf.SaAmfLibrary saAmfLibUtils = SaAmfLibrary.UTILS_INSTANCE;


  /*public static SaAisLibrary SaAisLibrary = SaAisLibrary.INSTANCE;*/
  public static clEoLib.ClEoLibrary eoLib = clEoLib.ClEoLibrary.INSTANCE;
  public static LongByReference amfHandle = new LongByReference();
  public static saAmf.SaAmfCallbacksT callbacks = new saAmf.SaAmfCallbacksT();
  public static boolean unblockNow = false;
  
  //public static saAis.SaAisLibrary.SaAisErrorT rc;
  
  public static saAmf.SaAmfLibrary.SaAmfCSISetCallbackT csiSetCb = (invocation, compName, haState, csiDescriptor) -> {
    //clprintf (CL_LOG_SEV_INFO, "Component [%.*s] : PID [%d]. CSI Set Received\n", 
    //          compName->length, compName->value, mypid);
    
    //clCompAppAMFPrintCSI(csiDescriptor, haState);
    /*saAis.SaAisLibrary.SaNameT comp = new saAis.SaAisLibrary.SaNameT(compName);
    comp.read();
    String msg = String.format("Component [%s] : PID [%d]. CSI Set Received\n", 
                                Native.toString(comp.value), mypid);*/
    //String msg = String.format("Component [%s] : PID [%d]. CSI Set Received\n", 
    //                            Native.toString(compName.value), mypid);                            
    //clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, msg);
    /*
     * Take appropriate action based on state
     */
    String msg = String.format("Component [%s] : PID [%d]. CSI Set Received\n", 
                                Native.toString(compName.value), mypid);
    clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, msg);
    switch ( haState ) {
        case saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_ACTIVE:
        {
            /*
             * AMF has requested application to take the active HA state 
             * for the CSI.
             */        	
            saAmfLib.saAmfResponse(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);
            break;
        }

        case saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_STANDBY:
        {
            /*
             * AMF has requested application to take the standby HA state 
             * for this CSI.
             */       	

            saAmfLib.saAmfResponse(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);
            break;
        }

        case saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_QUIESCED:
        {
            /*
             * AMF has requested application to quiesce the CSI currently
             * assigned the active or quiescing HA state. The application 
             * must stop work associated with the CSI immediately.
             */
        	
            saAmfLib.saAmfResponse(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);
            break;
        }

        case saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_QUIESCING:
        {
            /*
             * AMF has requested application to quiesce the CSI currently
             * assigned the active HA state. The application must stop work
             * associated with the CSI gracefully and not accept any new
             * workloads while the work is being terminated.
             */
        	
        	  saAmfLib.saAmfCSIQuiescingComplete(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);
            break;
        }

        default:
        {
            assert false : "Invalid haState";
            break;
        }
    }

    return;
  };
  public static saAmf.SaAmfLibrary.SaAmfCSIRemoveCallbackT csiRemoveCb = (invocation, compName, csiName, ciFlags) -> {
    //clprintf (CL_LOG_SEV_INFO, "Component [%.*s] : PID [%d]. CSI Remove Received\n", 
    //          compName->length, compName->value, mypid);

    //clprintf (CL_LOG_SEV_INFO, "   CSI                     : %.*s\n", csiName->length, csiName->value);
    //clprintf (CL_LOG_SEV_INFO, "   CSI Flags               : 0x%d\n", csiFlags);

    /*
     * Add application specific logic for removing the work for this CSI.
     */

    saAmfLib.saAmfResponse(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);

    return;
  };
  public static saAmf.SaAmfLibrary.SaAmfComponentTerminateCallbackT termCb = (invocation, compName) -> {
    int rc = saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK;
    //clprintf (CL_LOG_SEV_INFO, "Component [%.*s] : PID [%d]. Terminating\n",
    //          compName->length, compName->value, mypid);

    
    /*
     * Unregister with AMF and respond to AMF saying whether the
     * termination was successful or not.
     */
    saAis.SaAisLibrary.SaNameT.ByReference compNameRef = new saAis.SaAisLibrary.SaNameT.ByReference();
    System.arraycopy(compName.value, 0, compNameRef.value, 0, compName.length);
    compNameRef.write();   
    rc = saAmfLib.saAmfComponentUnregister(amfHandle.getValue(), compNameRef, null); 
    if (rc == saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK)
    {
      saAmfLib.saAmfResponse(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);
      //clprintf (CL_LOG_SEV_INFO, "Component [%.*s] : PID [%d]. Terminated\n",
      //        compName->length, compName->value, mypid);
      unblockNow = true;
    }
    return;
  };
   
  public static void exit(int status) {
    //TODO: log like clprintf (CL_LOG_SEV_ERROR, "Component [%.*s] : PID [%d]. Initialization error [0x%x]\n",
    //          appName.length, appName.value, mypid, rc);
    System.exit(status);
  }
  
  public void errorexit()
  {
    //clprintf (CL_LOG_SEV_ERROR, "Component [%.*s] : PID [%d]. Termination error [0x%x]\n",
    //          compName->length, compName->value, mypid, rc);
    return;
  }
  
  public static void main(String argv[])
  { 
    //Pointer symPtr = mwNativeLib.getGlobalVariableAddress("CL_LOG_HANDLE_APP");     
    //handleApp = symPtr.getLong(0);

    mypid = ProcessHandle.current().pid();
    
    saAis.SaAisLibrary.SaVersionT.ByReference version = new saAis.SaAisLibrary.SaVersionT.ByReference();
    version.releaseCode = 'B';
    version.majorVersion = 01;
    version.minorVersion = 01;
    version.write();
    
    callbacks.saAmfHealthcheckCallback = null;
    callbacks.saAmfComponentTerminateCallback = termCb;
    callbacks.saAmfCSISetCallback = csiSetCb;
    callbacks.saAmfCSIRemoveCallback = csiRemoveCb;
    callbacks.saAmfProtectionGroupTrackCallback = null;
    callbacks.write(); // sync struct
    Pointer callbacksPtr = callbacks.getPointer();
    Pointer versionPtr = version.getPointer();
    int rc = saAmfLib.saAmfInitialize(amfHandle, callbacksPtr, versionPtr);
    /* --- Step 5: Retrieve the handle (if rc == saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK)
        Pointer amfHandle = amfHandleRef.getValue();
        System.out.println("Got handle pointer: " + amfHandle);
    */
    if (rc != saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK) {
      exit(-1);
    }
    FDSet readfds = new FDSet();
    readfds.FD_ZERO();
    IntByReference dispatch_fd = new IntByReference();
    rc = saAmfLib.saAmfSelectionObjectGet(amfHandle.getValue(), dispatch_fd);
    if (rc != saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK) {
      exit(-1);
    }
    readfds.FD_SET(dispatch_fd.getValue());
    saAis.SaAisLibrary.SaNameT.ByReference appName = new saAis.SaAisLibrary.SaNameT.ByReference();
    rc = saAmfLib.saAmfComponentNameGet(amfHandle.getValue(), appName);
    if (rc != saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK) {
      exit(-1);
    }
    appName.read();
    rc = saAmfLib.saAmfComponentRegister(amfHandle.getValue(), appName.getPointer(), null);
    if (rc != saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK) {
      exit(-1);
    }
    IntByReference iocPort = new IntByReference();
    eoLib.clEoMyEoIocPortGet(iocPort);
    //TODO: logs like:
    /*clprintf (CL_LOG_SEV_INFO, "Component [%.*s] : PID [%d]. Initializing\n", appName.length, appName.value, mypid);
    clprintf (CL_LOG_SEV_INFO, "   IOC Address             : 0x%x\n", clIocLocalAddressGet());
    clprintf (CL_LOG_SEV_INFO, "   IOC Port                : 0x%x\n", iocPort);
    */
    //saAis.SaAisLibrary.SaNameT comp = new saAis.SaAisLibrary.SaNameT(compName);
    //comp.read();
    //System.out.println("a=" + info.a + " b=" + info.b);    
    clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Component [%s] : PID [%d]. Initializing\n", Native.toString(appName.value), mypid));
    Function iocLocalAddrGetFunc = iocLib.getFunction("clIocLocalAddressGet");
    int myIocAddr = iocLocalAddrGetFunc.invokeInt(new Object[]{});  
    clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("   IOC Address             : 0x%x\n", myIocAddr));
    clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("   IOC Port                : 0x%x\n",iocPort.getValue()));
    //System.out.print(msg);
    do {
        int err = libc.LibC.INSTANCE.select(dispatch_fd.getValue()+1, readfds, null, null, null);
        if (err < 0) {            
            if (libc.Errno.EINTR == Native.getLastError()) {
                continue;
            }
		        //clprintf (CL_LOG_SEV_ERROR, "Error in select()");
			      //perror("");
            break;
        }
        saAmfLib.saAmfDispatch(amfHandle.getValue(), saAis.SaAisLibrary.SaDispatchFlagsT.SA_DISPATCH_ALL);        
    }while(!unblockNow); 
    
    rc = saAmfLib.saAmfFinalize(amfHandle.getValue());
    if (rc != saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK) {
      exit(-1);
    }
  }
}
