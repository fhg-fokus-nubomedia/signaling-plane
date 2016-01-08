package eu.nubomedia.af.kurento.util.log;

import org.openxsp.java.EventBus;
import org.openxsp.java.OpenXSP;
import org.openxsp.java.RPCResult;
import org.openxsp.java.RPCResultHandler;
import org.vertx.java.core.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xsp on 7/8/14.
 */
public class VertxLogger implements Logger{

    private String tag;

    private Lvl localLevel;

    private EventBus eb;
    private OpenXSP openxsp;

    private static final String EVENT = "org.openxsp.log";

    private RPCResultHandler resultHandler;

    public VertxLogger(OpenXSP openxsp, String tag){
        this.tag = tag;
        this.eb = openxsp.eventBus();
        this.openxsp = openxsp;
        
        resultHandler = new RPCResultHandler(){
            @Override
            public void handle(RPCResult rpcResult) { }
        };

    }

    public void v(String msg) {
        if(getLevel().value<=Lvl.VERBOSE.value){
            System.out.println(getTime() + " VERBOSE [" + tag +getMethodName()+ "] " + msg);
            eb.publish(EVENT, getJson("debug", getTime(), "[" + tag +getMethodName()+ "] " + msg));
        }
    }
    
    public void d(String msg) {
        if(getLevel().value<=Lvl.DEBUG.value){
            System.out.println(getTime() + " DEBUG [" + tag +getMethodName()+ "] " + msg);
            eb.publish(EVENT, getJson("debug", getTime(), "[" + tag +getMethodName()+ "] " + msg));
        }
    }

    public void i(String msg) {
        if(getLevel().value<=Lvl.INFO.value) {
            System.out.println(getTime()+" INFO ["+tag+getMethodName()+"]: "+msg);
            eb.publish(EVENT, getJson("info", getTime(), "[" + tag +getMethodName()+ "] " + msg));
        }
    }

    public void w(String msg) {
        if(getLevel().value<=Lvl.WARN.value){
            System.out.println(getTime()+" WARN ["+tag+getMethodName()+"]: "+msg+"\r\n");
            eb.invoke(EVENT, " ", getJson("warn", getTime(), "[" + tag +getMethodName()+ "] " + msg), resultHandler);
        }
    }

    public void w(String msg, Throwable t) {
        if(getLevel().value<=Lvl.WARN.value){
            System.out.println(getTime()+" WARN ["+tag+getMethodName()+"]["+getMethodName()+"]: "+msg+"\r\n");
            eb.publish(EVENT, getJson("debug", getTime(), "[" + tag +getMethodName()+ "] " + msg));
        }
        t.printStackTrace();
    }

    public void e(String msg) {
        if(getLevel().value<=Lvl.ERROR.value){
            System.err.println(getTime()+" ERROR ["+tag+getMethodName()+"]: "+msg+"\r\n");
            eb.publish(EVENT, getJson("error", getTime(), "[" + tag +getMethodName()+ "] " + msg));
        }
    }

    public void e(String msg, Throwable t) {
        if(getLevel().value<=Lvl.ERROR.value){
            System.err.println(getTime()+" ERROR ["+tag+getMethodName()+"]: "+msg+"\r\n");
            if(t!=null) t.printStackTrace();
            eb.publish(EVENT, getJson("debug", getTime(), "[" + tag +getMethodName()+ "] " + msg, t.toString()));
        }
    }

    public void setLocalLogLevel(Lvl level) {
        this.localLevel = level;
    }

    private String getTime(){

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy hh:mm:ss:SSS");
        String formattedDate = formatter.format(new Date());

        return formattedDate;
    }

    private Lvl getLevel(){

        if(this.localLevel!=null) return localLevel;
        else return LoggerFactory.getLogLevel();
    }

    private JsonObject getJson(String lvl, String timestamp, String message){

    	JsonObject logMsg = new JsonObject();
        logMsg.putString("lvl",lvl);
        logMsg.putString("msg",message);
        logMsg.putString("timestamp",timestamp);
        logMsg.putString("modulename", "kurento_client");

        return logMsg;
    }

    private JsonObject getJson(String lvl, String timestamp, String message, String extra){

    	JsonObject logMsg = new JsonObject();
        logMsg.putString("lvl",lvl);
        logMsg.putString("msg", message);
        logMsg.putString("timestamp",timestamp);
        logMsg.putString("modulename", "rtc_control");
        logMsg.putString("extra", extra);

        return logMsg;
    }
    
    private String getMethodName(){
    	
    	try{
    		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        	
    		if(ste.length>=9) return ":"+ste[ste.length - 9].getMethodName()+"()";
    	}catch(Exception e){
    		System.err.println(e.getMessage());
    	}
    	
    	return "";
    }

    @Override
	public Logger newInstance(String tag) {
		return new VertxLogger(this.openxsp, tag);
	}
}
