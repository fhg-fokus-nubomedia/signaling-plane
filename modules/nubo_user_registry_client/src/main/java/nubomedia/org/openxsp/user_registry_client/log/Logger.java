package nubomedia.org.openxsp.user_registry_client.log;

/**
 * Logger interface that can be implemented against different logging frameworks etc.
 * @author fsc
 *
 */
public interface Logger {


    public enum Lvl {
        VERBOSE(0),
        DEBUG(1),
        INFO (2),
        WARN (4),
        ERROR (6);

        public int value;

        Lvl(int value){
            this.value = value;
        }

    }

    /**
     * Verbose level log message
     * @param msg
     */
    void v(String msg);


    /**
     * Debug level log message
     * @param msg
     */
    void d(String msg);

    /**
     * Info level log message
     * @param msg
     */
    void i(String msg);

    /**
     * Warning level log message
     * @param msg
     */
    void w(String msg);

    /**
     * Warning level log message
     * @param msg
     * @param t
     */
    void w(String msg, Throwable t);

    /**
     * Error level log message
     */
    void e(String msg);

    /**
     * Error level log message
     * @param msg
     */
    void e(String msg, Throwable t);

    /**
     * sets a log level specific for this logger instance
     * @param level
     */
    void setLocalLogLevel(Lvl level);

    /**
     * sets a sub tag for this logger instance
     * @param subTag
     */
    public void setSubTag(String subTag);

    /**
     * sets a flag to enable the time logging for this logger instance
     * @param activeTime
     */
    public void setActiveTime(boolean activeTime);

    /*
     * Get a new logger with the given tag
     */
    Logger newInstance(String tag);

}
