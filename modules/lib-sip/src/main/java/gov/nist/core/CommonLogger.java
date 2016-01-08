package gov.nist.core;


import org.apache.log4j.Logger;

import java.util.Properties;
/**
 * This class abstracts away single-instanct and multi0instance loggers
 * legacyLogger is the old-school one logger per stack reference
 * otherLogger is multiinstance logger
 * 
 * @author Vladimir Ralev
 *
 */
public class CommonLogger implements StackLogger{
	private String name;
	private StackLogger otherLogger;
	
	public static boolean useLegacyLogger = true;
	public static StackLogger legacyLogger;
	
	public CommonLogger(String name) {
		this.name = name;
	}
	
	private StackLogger logger() {
		if(useLegacyLogger) {
			if(legacyLogger == null) {
				return new CommonLoggerLog4j(Logger.getLogger(name));
			}
			return legacyLogger;
		} else {
			if(otherLogger == null) {
				otherLogger = new CommonLoggerLog4j(Logger.getLogger(name));
			}
			return otherLogger;
		}
	}
	
	public static StackLogger getLogger(String name) {
		return new CommonLogger(name);
	}
	public static StackLogger getLogger(Class clazz) {
		return getLogger(clazz.getName());
	}
	
	public static void init(Properties p) {
		
	}
	
	public void disableLogging() {
		logger().disableLogging();
	}
	
	public void enableLogging() {
		logger().enableLogging();
		
	}
	
	public int getLineCount() {
		
		return logger().getLineCount();
	}
	
	public String getLoggerName() {
		
		return logger().getLoggerName();
	}
	
	public boolean isLoggingEnabled() {
		
		return logger().isLoggingEnabled();
	}
	
	public boolean isLoggingEnabled(int logLevel) {
		
		return logger().isLoggingEnabled(logLevel);
	}

   public void log(int level, Object message) {
       switch (level) {
           case LogLevels.TRACE_DEBUG:
               if (logger().isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                   if (message.getClass() == String.class) {
                       logger().logDebug((String) message);
                   } else {
                       logger().logFatalError("FatalError: message class get :"
                               + message.getClass().toString()
                       + " expect : " + String.class.toString());
                   }
               }
               break;
           case LogLevels.TRACE_ERROR:
               if (logger().isLoggingEnabled(LogLevels.TRACE_ERROR)) {
                   if (message.getClass() == String.class) {
                       logger().logError((String)message);
                   } else {
                       logger().logFatalError("FatalError: message class get :"
                               + message.getClass().toString()
                               + " expect : " + String.class.toString());
                   }
               }
                break;
           case LogLevels.TRACE_EXCEPTION:
               if (logger().isLoggingEnabled(LogLevels.TRACE_EXCEPTION)) {
                   if (message.getClass() == Throwable.class) {
                       logger().logException((Throwable) message);
                   } else {
                       logger().logFatalError("FatalError: message class get :"
                               + message.getClass().toString()
                               + " expect : " + Throwable.class.toString());
                   }
               }
               break;
           case LogLevels.TRACE_FATAL:
               if (logger().isLoggingEnabled(LogLevels.TRACE_FATAL)) {
                   if (message.getClass() == String.class) {
                       logger().logFatalError((String)message);
                   } else {
                       logger().logFatalError("FatalError: message class get :"
                               + message.getClass().toString()
                               + " expect : " + String.class.toString());
                   }
               }
               break;
           case LogLevels.TRACE_MESSAGES:
           case LogLevels.TRACE_INFO:
               if (logger().isLoggingEnabled(LogLevels.TRACE_INFO)) {
                   if (message.getClass() == String.class) {
                       logger().logInfo((String)message);
                   } else {
                       logger().logFatalError("FatalError: message class get :"
                               + message.getClass().toString()
                               + " expect : " + String.class.toString());
                   }
               }
               break;
           case LogLevels.TRACE_TRACE:
               if (logger().isLoggingEnabled(LogLevels.TRACE_TRACE)) {
                   if (message.getClass() == String.class) {
                       logger().logTrace((String)message);
                   } else {
                       logger().logFatalError("FatalError: message class get :"
                               + message.getClass().toString()
                               + " expect : " + String.class.toString());
                   }
               }
               break;
           case LogLevels.TRACE_WARN:
               if (logger().isLoggingEnabled(LogLevels.TRACE_WARN)) {
                   if (message.getClass() == String.class) {
                       logger().logWarning((String)message);
                   } else {
                       logger().logFatalError("FatalError: message class get :"
                               + message.getClass().toString()
                               + " expect : " + String.class.toString());
                   }
               }
               break;
           case LogLevels.TRACE_NONE:
               break;
           default:
               logger().logError("Error, unknown log level " + level);
               break;
       }
   }
	
	public void logDebug(String message) {
		
		logger().logDebug(message);
	}
	
	public void logError(String message) {
		
		logger().logError(message);
	}
	
	public void logError(String message, Exception ex) {
		
		logger().logError(message, ex);
	}
	
	public void logException(Throwable ex) {
		
		logger().logException(ex);
	}
	
	public void logFatalError(String message) {
		
		logger().logFatalError(message);
	}
	
	public void logInfo(String string) {
		
		logger().logInfo(string);
	}
	
	public void logStackTrace() {
		
		logger().logStackTrace();
	}
	
	public void logStackTrace(int traceLevel) {
		
		logger().logStackTrace(traceLevel);
	}
	
	public void logTrace(String message) {
		
		logger().logTrace(message);
	}
	
	public void logWarning(String string) {
		
		logger().logWarning(string);
	}
	
	public void setBuildTimeStamp(String buildTimeStamp) {
		
		logger().setBuildTimeStamp(buildTimeStamp);
	}
	
	public void setStackProperties(Properties stackProperties) {
		
		legacyLogger.setStackProperties(stackProperties);
	}
}
