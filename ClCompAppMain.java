import com.sun.jna.*;
import com.sun.jna.ptr.*;
import saAmf.SaAmfLibrary;
import saAmf.SaAmfLibrary.*;
//import saAmf.SaAmfLibrary.SaAmfHAStateT.*;
import saAmf.SaAmfCallbacksT;
import saAmf.SaAmfCallbacksT.*;
import saAmf.SaAmfCSIDescriptorT;
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
  //public static saAmf.SaAmfCallbacksT callbacks = new saAmf.SaAmfCallbacksT();
  public static saAmf.SaAmfCallbacksT.ByReference callbacks = new saAmf.SaAmfCallbacksT.ByReference();
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
    try {
      csiDescriptor.read();
    /*if (compName!=null) compName.read();
    String compStr = (compName != null)
      ? new String(compName.value, 0, Math.min(compName.length, compName.value.length), StandardCharsets.UTF_8)
      : "<null>";
    String msg = String.format("Component [%s] : PID [%d]. CSI Set Received\n", compStr, mypid);*/
    compName.read();
    csiDescriptor.csiName.read();
    
    String msg = String.format("Component [%s] : PID [%d]. CSI Set Received\n", Native.toString(compName.value), mypid);             
    clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, msg); 
    //logger.info(msg);
    csiDescriptor.csiStateDescriptor.setType(
            (haState == saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_ACTIVE)
                ? saAmf.SaAmfCSIActiveDescriptorT.class//saAmf.SaAmfCSIStateDescriptorT.SaAmfCSIActiveDescriptorT.class
                : saAmf.SaAmfCSIStandbyDescriptorT.class
        );        
    /*saAmf.SaAmfCSIDescriptorT csiDescriptor = null;
    if (csiDescriptorPtr != null) {
        csiDescriptor = new SaAmfCSIDescriptorT(csiDescriptorPtr);
        csiDescriptor.read();
        clCompAppAMFPrintCSI(csiDescriptor, haState);
    } else {
        clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, "CSISet Callback: csiDescriptor is NULL");
    }*/
    
    csiDescriptor.csiStateDescriptor.read();
    clCompAppAMFPrintCSI(csiDescriptor, haState);
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
    }catch (Throwable t) {
        t.printStackTrace(); // don't let exceptions escape native callback
    }
    return;
  };
  public static saAmf.SaAmfLibrary.SaAmfCSIRemoveCallbackT csiRemoveCb = (invocation, compName, csiName, csiFlags) -> {
    //clprintf (CL_LOG_SEV_INFO, "Component [%.*s] : PID [%d]. CSI Remove Received\n", 
    //          compName->length, compName->value, mypid);

    //clprintf (CL_LOG_SEV_INFO, "   CSI                     : %.*s\n", csiName->length, csiName->value);
    //clprintf (CL_LOG_SEV_INFO, "   CSI Flags               : 0x%d\n", csiFlags);
    //saAis.SaAisLibrary.SaNameT comp = new saAis.SaAisLibrary.SaNameT(compName);
    //if (compName != null) compName.read();
    compName.read();
    //csiName.read();
    clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Component [%s] : PID [%d]. CSI Remove Received\n", Native.toString(compName.value), mypid));
    /*
     * Add application specific logic for removing the work for this CSI.
     */
    try {
      //logger.info("csiRemoveCb: calling saAmfResponse");
      saAmfLib.saAmfResponse(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);
    }catch (Throwable t) {
        t.printStackTrace(); // avoid letting exceptions escape native callback
    }
    return;
  };
  
  public static saAmf.SaAmfLibrary.SaAmfComponentTerminateCallbackT termCb = (invocation, compName) -> {
    try {
      int rc = saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK;    
      clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Component [%s] : PID [%d]. Terminating\n", Native.toString(compName.value), mypid));    
      /*
       * Unregister with AMF and respond to AMF saying whether the
       * termination was successful or not.
       */    
      compName.read();
      rc = saAmfLib.saAmfComponentUnregister(amfHandle.getValue(), compName.getPointer(), null);
      if (rc == saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK)
      {
        saAmfLib.saAmfResponse(amfHandle.getValue(), invocation, saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK);
        clprintf(clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Component [%s] : PID [%d]. Terminated\n", Native.toString(compName.value), mypid)); 
        unblockNow = true;
      }
    }catch (Throwable t) {
        t.printStackTrace();
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
    callbacks.saAmfProxiedComponentInstantiateCallback = null;
    callbacks.saAmfProxiedComponentCleanupCallback = null;
    callbacks.write(); // sync struct
    //Pointer callbacksPtr = callbacks.getPointer();
    //Pointer versionPtr = version.getPointer();
    int rc = saAmfLib.saAmfInitialize(amfHandle, callbacks, version);
    /* --- Step 5: Retrieve the handle (if rc == saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK)
        Pointer amfHandle = amfHandleRef.getValue();
        System.out.println("Got handle pointer: " + amfHandle);
    */
    if (rc != saAis.SaAisLibrary.SaAisErrorT.SA_AIS_OK) {
      exit(-1);
    }
    FDSet readfds = new FDSet();
    readfds.FD_ZERO();
    LongByReference dispatch_fd = new LongByReference();
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
  
  public static String haStateToString(int haState) {
    //String strHaState;
    switch (haState) {
      case saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_ACTIVE: 
        return "Active";
      case saAmf.SaAmfLibrary.SaAmfHaStateT. SA_AMF_HA_STANDBY:
         return "Standby";
      case saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_QUIESCED:
         return "Quiesced";
      case saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_QUIESCING:
        return "Quiescing";
      default:
        return "Unknown";
    }
  }
  
  public static String csiFlagsToString(int csiFlags) {
    if ((csiFlags & saAmf.SaAmfLibrary.SA_AMF_CSI_ADD_ONE) != 0)
      return "Add One";
    if ((csiFlags & saAmf.SaAmfLibrary.SA_AMF_CSI_TARGET_ONE) != 0)
      return "Target One";
    if ((csiFlags & saAmf.SaAmfLibrary.SA_AMF_CSI_TARGET_ALL) != 0)
      return "Target All";
    return "Unknown";
  }
  
  public static void clCompAppAMFPrintCSI(saAmf.SaAmfCSIDescriptorT csiDescriptor,
                          int haState) {
    //csiDescriptor.read();
    clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO,
              "CSI Flags : [%s]",
              csiFlagsToString(csiDescriptor.csiFlags));

    if (saAmf.SaAmfLibrary.SA_AMF_CSI_TARGET_ALL != csiDescriptor.csiFlags)
    {
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("CSI Name : [%s]", 
                  Native.toString(csiDescriptor.csiName.value)));
    }

    if (saAmf.SaAmfLibrary.SA_AMF_CSI_ADD_ONE == csiDescriptor.csiFlags)
    {
        //int i = 0;
        /*csiDescriptor.csiAttr.attr.read();
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, "Name value pairs :");
        //Pointer ptr = MyLib.INSTANCE.createItems(3);
        //saAmf.SaAmfCSIAttributeT first = new saAmf.SaAmfCSIAttributeT(csiDescriptor.csiAttr.attr);
        saAmf.SaAmfCSIAttributeT[] csiAttrs = (saAmf.SaAmfCSIAttributeT[])csiDescriptor.csiAttr.attr.toArray(csiDescriptor.csiAttr.number);
        
        //MyStruct[] arr = (MyStruct[]) ref.toArray(5);
        //for (i = 0; i < csiDescriptor.csiAttr.number; i++)
        for (saAmf.SaAmfCSIAttributeT attr:csiAttrs)
        {*/
            /*clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Name : [%s]",
                      Native.toString(csiDescriptor.csiAttr.
                      attr[i].attrName)));
            clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Value : [%s]",
                      Native.toString(csiDescriptor.csiAttr.
                      attr[i].attrValue)));*/
            /*attr.read();
            clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Name : [%s]",
                      Native.toString(attr.attrName)));
            clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Value : [%s]",
                      Native.toString(attr.attrValue)));
        }*/
        /*saAmf.SaAmfCSIAttributeListT list = csiDescriptor.csiAttr;
        list.read();

        int n = list.number;
        Pointer base = list.attr;

        for (int i = 0; i < n; i++) {
          saAmf.SaAmfCSIAttributeT attr = new saAmf.SaAmfCSIAttributeT(base.share(i * new saAmf.SaAmfCSIAttributeT().size()));
          attr.read();
          String name = Native.toString(attr.attrName);
          String value = Native.toString(attr.attrValue);//attr.attrValue == null ? "<null>" : attr.attrValue.getString(0);
          //System.out.println("Attr: " + name + " = " + value);
          clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Name : [%s]",name));                      
          clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Value : [%s]", value));           
       }*/
    /*saAmf.SaAmfCSIAttributeListT list = csiDescriptor.csiAttr;
    list.read(); // CRITICAL

    int count = list.number;
    //System.out.println("Attribute count: " + count);

    if (count > 0 && list.attr != null) {
        saAmf.SaAmfCSIAttributeT attrArray =
                new saAmf.SaAmfCSIAttributeT(list.attr);

        saAmf.SaAmfCSIAttributeT[] attrs =
                (saAmf.SaAmfCSIAttributeT[]) attrArray.toArray(count);

        for (int i = 0; i < count; i++) {
            attrs[i].read(); // ensure initialized*/

            /*String name = attrs[i].attributeName.toString();
            //String value = attrs[i].attributeValue == null ?
                           null :
                           attrs[i].attributeValue.getString(0);

            System.out.printf("  Attribute[%d]: %s = %s%n", i, name, value);*/
            
        int count = csiDescriptor.csiAttr.number;
        Pointer ptr = csiDescriptor.csiAttr.attr;

        if (ptr == null || count == 0) {
          System.out.println("  (No CSI attributes)");
          return;
        }

        saAmf.SaAmfCSIAttributeT[] attrs =
            (saAmf.SaAmfCSIAttributeT[]) new saAmf.SaAmfCSIAttributeT(ptr).toArray(count);

        for (int i = 0; i < count; i++) {
          attrs[i].read();
          //System.out.printf("  Attribute[%d]: %s = %s\n",
          //      i, attrs[i].getName(), attrs[i].getValue());
            
            
            //String name = Native.toString(attrs[i].attrName);
            //String value = Native.toString(attrs[i].attrValue);
            clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Name :[%s]",attrs[i].getName()));                      
            clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Value : [%s]", attrs[i].getValue()));
        }
    }
       /*saAmf.SaAmfCSIAttributeListT attr = csiDescriptor.csiAttr;
       attr.read();

       for (int i = 0; i < attr.number; i++) {*/
            /*SaStringT key = attr.nameValue[i].name;
            SaStringT val = attr.nameValue[i].value;

            key.read();
            val.read();

            logger.info("  Attribute: " + key.toString() + " = " + val.toString());*/
            
            /*String name = Native.toString(attr.attrName);
            String value = Native.toString(attr.attrValue);
            clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Name : [%s]",name));                      
            clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Value : [%s]", value));  
        }*/

    
    
    clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("HA state : [%s]",
              haStateToString(haState)));

    if (saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_ACTIVE == haState)
    {
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, "Active Descriptor :");
        /*clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO,
                  String.format("Transition Descriptor : [%d]",
                  csiDescriptor.csiStateDescriptor.
                  activeDescriptor.transitionDescriptor));
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO,
                  String.format("Active Component : [%s]",
                  Native.toString(csiDescriptor.csiStateDescriptor.
                  activeDescriptor.activeCompName.value)));*/
        saAmf.SaAmfCSIActiveDescriptorT act = csiDescriptor.csiStateDescriptor.activeDescriptor;
        act.read();
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO,
                  String.format("Transition Descriptor : [%d]",
                  act.transitionDescriptor));
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO,
                  String.format("Active Component : [%s]",
                  Native.toString(act.activeCompName.value)));
    }
    else if (saAmf.SaAmfLibrary.SaAmfHaStateT.SA_AMF_HA_STANDBY == haState)
    {
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, "Standby Descriptor :");
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO,
                  String.format("Standby Rank : [%d]",
                  csiDescriptor.csiStateDescriptor.
                  standbyDescriptor.standbyRank));
        clprintf (clUtils.ClUtilsLibrary.ClLogSeverityT.CL_LOG_SEV_INFO, String.format("Active Component : [%s]",
                  Native.toString(csiDescriptor.csiStateDescriptor.
                  standbyDescriptor.activeCompName.value)));
    }
  }
}
