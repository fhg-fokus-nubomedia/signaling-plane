/*******************************************************************************
 *  Copyright (C) 2014 FhG FOKUS, Institute for Open Communication Systems
 *
 *  This file is part of the OpenXSP platform
 *
 *   The FOKUS OpenXSP platform is proprietary software that is licensed
 *    under the FhG FOKUS "SOURCE CODE LICENSE".
 *    You should have received a copy of the license along with this
 *    program; if not, write to Fraunhofer Institute FOKUS, Kaiserin-
 *    Augusta Allee 31, 10589 Berlin, GERMANY
 *  
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *    It has to be noted that this software is not intended to become
 *    or act as a product in a commercial context. It is a PROTOTYPE
 *    IMPLEMENTATION for NGN technology testing and application
 *    development for research purposes, typically performed in
 *    testbeds. See the attached license for more details.
 *  
 *    For a license to use this software under conditions
 *    other than those described here, please contact Fraunhofer FOKUS
 *    via e-mail at the following address:
 *        info@fokus.fraunhofer.de
 *******************************************************************************/
package eu.nubomedia.af.kurento.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SysoLogger implements Logger {

	private String tag;

	private Lvl localLevel;

	public SysoLogger(String tag) {
		this.tag = tag;
	}

	@Override
	public void v(String msg) {
		if (getLevel().value <= Lvl.VERBOSE.value)
			System.out.println(getTime() + " DEBUG [" + tag + "][" + getMethodName() + "]: " + msg);
	}

	@Override
	public void d(String msg) {
		if (getLevel().value <= Lvl.DEBUG.value)
			System.out.println(getTime() + " DEBUG [" + tag + "][" + getMethodName() + "]: " + msg);
	}

	@Override
	public void i(String msg) {
		if (getLevel().value <= Lvl.INFO.value)
			System.out.println(getTime() + " INFO [" + tag + "][" + getMethodName() + "]: " + msg);
	}

	@Override
	public void w(String msg) {
		if (getLevel().value <= Lvl.WARN.value)
			System.out.println(getTime() + " WARN [" + tag + "][" + getMethodName() + "]: " + msg + "\r\n");
	}

	@Override
	public void w(String msg, Throwable t) {
		if (getLevel().value <= Lvl.WARN.value)
			System.out.println(getTime() + " WARN [" + tag + "][" + getMethodName() + "]: " + msg + "\r\n");
		t.printStackTrace();
	}

	@Override
	public void e(String msg) {
		if (getLevel().value <= Lvl.ERROR.value)
			System.err.println(getTime() + " ERROR [" + tag + "][" + getMethodName() + "]: " + msg + "\r\n");
	}

	@Override
	public void e(String msg, Throwable t) {
		if (getLevel().value <= Lvl.ERROR.value) {
			System.err.println(getTime() + " ERROR [" + tag + "]: " + msg + "\r\n");
			if (t != null)
				t.printStackTrace();
		}
	}

	@Override
	public void setLocalLogLevel(Lvl level) {
		this.localLevel = level;
	}

	private String getTime() {

		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yy hh:mm:ss:SSS");
		String formattedDate = formatter.format(new Date());

		return formattedDate;
	}

	private Lvl getLevel() {

		if (this.localLevel != null)
			return localLevel;
		else
			return LoggerFactory.getLogLevel();
	}

	private String getMethodName() {

		try {
			final StackTraceElement[] ste = Thread.currentThread().getStackTrace();

			return ste[ste.length - 2].getMethodName();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return "";
	}

	@Override
	public Logger newInstance(String tag) {
		return new SysoLogger(tag);
	}
}
