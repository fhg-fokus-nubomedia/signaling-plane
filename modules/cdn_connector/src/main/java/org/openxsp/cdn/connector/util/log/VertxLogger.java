package org.openxsp.cdn.connector.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.openxsp.java.EventBus;
import org.openxsp.java.OpenXSP;
import org.openxsp.java.RPCResult;
import org.openxsp.java.RPCResultHandler;
import org.vertx.java.core.json.JsonObject;

/**
 * Created by xsp on 7/8/14.
 */
public class VertxLogger extends AbstractLogger{

    private String tag, module;

    private EventBus eb;
    private OpenXSP openxsp;

    private static final String EVENT = "org.openxsp.log";

    private RPCResultHandler resultHandler;

    public VertxLogger(OpenXSP openxsp, String tag, String moduleName){
        this.tag = tag;
        this.eb = openxsp.eventBus();
        this.openxsp = openxsp;
        this.module = moduleName;
        
        resultHandler = new RPCResultHandler(){
            @Override
            public void handle(RPCResult rpcResult) { }
        };

    }
    
    protected void log(String msg, Lvl lvl){
		log(msg, lvl, null);
	}
	
	protected void log(String msg, Lvl lvl, Throwable e){
		if(getLevel().value<=lvl.value){
            System.out.println(getTime()+" "+lvl+" ["+tag+"]: "+msg+"\r\n");
            if(e!=null) e.printStackTrace();
            eb.publish(EVENT, getJson(lvl.toString(), getTime(), "[" + tag + "] " + msg, e));
        }
	}
	

    private String getTime(){

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy hh:mm:ss:SSS");
        String formattedDate = formatter.format(new Date());

        return formattedDate;
    }


    private JsonObject getJson(String lvl, String timestamp, String message, Throwable t){

    	JsonObject logMsg = new JsonObject();
        logMsg.putString("lvl",lvl);
        logMsg.putString("msg", message);
        logMsg.putString("timestamp",timestamp);
        logMsg.putString("modulename", module);
        if(t!=null) logMsg.putString("extra", t.getMessage());

        return logMsg;
    }
    
//    private String getMethodName(){
//    	
//    	try{
//    		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
//        	
//    		if(ste.length>=9) return ":"+ste[ste.length - 9].getMethodName()+"()";
//    	}catch(Exception e){
//    		System.err.println(e.getMessage());
//    	}
//    	
//    	return "";
//    }

    @Override
	public Logger newInstance(String tag) {
		return new VertxLogger(this.openxsp, tag, module);
	}
}
