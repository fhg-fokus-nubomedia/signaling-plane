package org.openxsp.modules.util;

import org.openxsp.java.EventBus;
import org.openxsp.java.OpenXSP;
import org.openxsp.java.RPCResult;
import org.openxsp.java.RPCResultHandler;
import org.vertx.java.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fsc on 7/8/14.
 */
public class VertxLogger extends LoggerStub{

    private String tag, module;

    private EventBus eb;
    private OpenXSP openxsp;

    private static final String EVENT = "org.openxsp.log";

    private RPCResultHandler resultHandler;

    public VertxLogger(OpenXSP openxsp, String tag, String module){
        this.tag = tag;
        this.eb = openxsp.eventBus();
        this.openxsp = openxsp;
        this.module = module;
        
        resultHandler = new RPCResultHandler(){
            @Override
            public void handle(RPCResult rpcResult) { }
        };
    }


    @Override
	void log(String msg, Lvl level, Throwable t) {
    	if(getLevel().value <= level.value){
            System.out.println(getTime()+" "+level+" ["+tag+"]: "+msg);
            if(t!=null) t.printStackTrace();
            eb.publish(EVENT, getJson(level+"", getTime(), "[" + tag + "] " + msg, t));
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
	
    
    @Override
	public Logger newInstance(String tag) {
		return new VertxLogger(this.openxsp, tag, this.module);
	}
}
