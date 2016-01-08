package nubomedia.org.openxsp.user_registry_client.log;

/**
 * @author fsc
 */
import java.text.SimpleDateFormat;
import java.util.Date;

public class SysoLogger implements Logger {

    private String tag;
    private String subTag;
    private boolean activeTime;

    private Lvl localLevel;

    private static Lvl globalLevel = Lvl.DEBUG;

    public SysoLogger(String tag){
        this.tag = tag;
    }

    public SysoLogger(String tag, String subTag){
        this.tag = tag;
        this.subTag = subTag;
    }


    @Override
    public void v(String msg) {
        if(getLevel().value<=Lvl.VERBOSE.value)	System.out.println((activeTime?getTime()+"## ":"")+"VERBOSE ["+tag+((subTag!=null)?" -> "+subTag:"")+"]: "+msg);
    }


    @Override
    public void d(String msg) {
        if(getLevel().value<=Lvl.DEBUG.value) System.out.println((activeTime?getTime()+"## ":"")+"DEBUG ["+tag+((subTag!=null)?" -> "+subTag:"")+"]: "+msg);
    }

    @Override
    public void i(String msg) {
        if(getLevel().value<=Lvl.INFO.value) System.out.println((activeTime?getTime()+"## ":"")+"INFO ["+tag+((subTag!=null)?" -> "+subTag:"")+"]: "+msg);
    }

    @Override
    public void w(String msg) {
        if(getLevel().value<=Lvl.WARN.value) System.out.println((activeTime?getTime()+"## ":"")+"WARN ["+tag+((subTag!=null)?" -> "+subTag:"")+"]: "+msg+"\r\n");
    }

    @Override
    public void w(String msg, Throwable t) {
        if(getLevel().value<=Lvl.WARN.value) System.out.println((activeTime?getTime()+"## ":"")+"WARN ["+tag+((subTag!=null)?" -> "+subTag:"")+"]: "+msg+"\r\n");
        t.printStackTrace();
    }

    @Override
    public void e(String msg) {
        if(getLevel().value<=Lvl.ERROR.value) System.err.println((activeTime?getTime()+"## ":"")+"ERROR ["+tag+((subTag!=null)?" -> "+subTag:"")+"]: "+msg+"\r\n");
    }

    @Override
    public void e(String msg, Throwable t) {
        if(getLevel().value<=Lvl.ERROR.value){
            System.err.println((activeTime?getTime()+"## ":"")+"ERROR ["+tag+"]: "+msg+"\r\n");
            if(t!=null) t.printStackTrace();
        }
    }

    @Override
    public void setLocalLogLevel(Lvl level) {
        this.localLevel = level;
    }

    public void setGlobalLogLevel(Lvl level){
        globalLevel = level;
    }

    public void setSubTag(String subTag) {
        this.subTag = subTag;
    }

    public void setActiveTime(boolean activeTime) {
        this.activeTime = activeTime;
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

    @Override
    public Logger newInstance(String tag) {
        return new SysoLogger(tag);
    }

}
