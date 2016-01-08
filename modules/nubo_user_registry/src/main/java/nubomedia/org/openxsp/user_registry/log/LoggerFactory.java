package nubomedia.org.openxsp.user_registry.log;

/**
 * @author fsc
 */


public class LoggerFactory {

    private static Logger logger;
    private static Logger.Lvl lvl = Logger.Lvl.DEBUG;

    public static Logger getLogger(String tag){
        if(logger!=null){
            return logger.newInstance(tag);
        }
        Logger sysoLogger = new SysoLogger(tag);
        return sysoLogger;
    }

    public static void setLogger(Logger log){
        logger = log;
    }

    public static void setLogLevel(Logger.Lvl level){
        lvl = level;
    }

    public static Logger.Lvl getLogLevel(){
        return lvl;
    }
}
