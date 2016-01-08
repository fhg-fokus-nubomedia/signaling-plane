package org.openxsp.modules.util;

import org.openxsp.modules.util.Logger.Lvl;

public class LoggerFactory {
	
	private static Lvl log_level = Lvl.VERBOSE;
	
	
	public static Logger logger;
	
	public static Logger getLogger(String tag){
		
		if(logger==null) setLogger(new SysoLogger(""));
		
		return logger.newInstance(tag);
	}
	
	public static Logger getLogger(Class<?> c){
		
		return getLogger(c.getSimpleName());
	}
	
	public static void setLogger(Logger log){
		logger = log;
	}
	
	public void setLogLevel(Lvl level){
		log_level = level;
	}
	
	public static Lvl getLogLevel(){
		return log_level;
	}
}
