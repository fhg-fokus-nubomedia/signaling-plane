package org.openxsp.modules.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of the {@link Logger} interface to use the Java console for logging messages
 * @author fsc
 *
 */
public class SysoLogger extends LoggerStub {

	private final String TAG;

	public SysoLogger(String tag) {
		this.TAG = tag;
	}

	protected void log(String msg, Lvl lvl, Throwable t){
		if (getLevel().value <= lvl.value) {
			System.out.println(getTime() + " "+lvl+getSpaces(lvl)+" [" + TAG + "]: " + msg);
			if (t != null)
				t.printStackTrace();
		}
	}

	private String getTime() {

		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy hh:mm:ss:SSS");
		String formattedDate = formatter.format(new Date());

		return formattedDate;
	}

	/*
	 * For nicer formatting
	 */
	private String getSpaces(Lvl lvl){
		String spaces = "";
		
		for(int i = 0; i< Lvl.VERBOSE.toString().length()-lvl.toString().length(); i++){
			spaces += " ";
		}
		
		return spaces;
	}

	@Override
	public Logger newInstance(String tag) {
		return new SysoLogger(tag);
	}
}
