package org.openxsp.modules.util;


public abstract class LoggerStub implements Logger{

	Lvl localLevel;
	
	@Override
	public void setLocalLogLevel(Lvl level) {
		this.localLevel = level;
	}
	
    public Lvl getLevel(){

        if(this.localLevel!=null) return localLevel;
        else return LoggerFactory.getLogLevel();
    }
	
	@Override
	public void v(String msg) {
		log(msg, Lvl.VERBOSE, null);
	}

	@Override
	public void d(String msg) {
		log(msg, Lvl.DEBUG, null);
	}

	@Override
	public void i(String msg) {
		log(msg, Lvl.INFO, null);
	}

	@Override
	public void w(String msg) {
		log(msg, Lvl.WARN, null);
	}

	@Override
	public void w(String msg, Throwable t) {
		log(msg, Lvl.WARN, t);
	}

	@Override
	public void e(String msg) {
		log(msg, Lvl.ERROR, null);
	}

	@Override
	public void e(String msg, Throwable t) {
		log(msg, Lvl.ERROR, t);
	}
	
	abstract void log(String msg, Lvl level, Throwable t);
}
