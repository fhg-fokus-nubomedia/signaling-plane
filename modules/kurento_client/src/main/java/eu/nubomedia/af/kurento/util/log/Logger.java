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


public interface Logger {

	
	public enum Lvl {
		VERBOSE(0),
		DEBUG(1),
		INFO (2),
		WARN (4),
		ERROR (6);
		
		protected int value;
		
		Lvl(int value){
			this.value = value;
		}
		
	}
	
	/**
	 * Debug level log message
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
	
	void setLocalLogLevel(Lvl level);
	
	Logger newInstance(String tag);
}
