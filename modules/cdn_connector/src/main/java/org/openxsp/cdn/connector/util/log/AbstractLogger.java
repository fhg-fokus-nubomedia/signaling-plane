package org.openxsp.cdn.connector.util.log;


public abstract class AbstractLogger implements Logger{
	
	protected Lvl localLevel;
	
	@Override
	public void v(String msg) {
		log(msg, Lvl.VERBOSE);
	}

	@Override
	public void d(String msg) {
		log(msg, Lvl.DEBUG);
	}

	@Override
	public void i(String msg) {
		log(msg, Lvl.INFO);
	}

	@Override
	public void w(String msg) {
		log(msg, Lvl.WARN);
	}

	@Override
	public void w(String msg, Throwable t) {
		log(msg, Lvl.WARN, t);
	}

	@Override
	public void e(String msg) {
		log(msg, Lvl.ERROR);
	}

	@Override
	public void e(String msg, Throwable t) {
		log(msg, Lvl.ERROR, t);
	}
	
	@Override
	public void setLocalLogLevel(Lvl level) {
		this.localLevel = level;
	}

	protected Lvl getLevel() {

		if (this.localLevel != null)
			return localLevel;
		else
			return LoggerFactory.getLogLevel();
	}
	
	
	protected abstract void log(String msg, Lvl lvl);
	
	protected abstract void log(String msg, Lvl lvl, Throwable t);
}
