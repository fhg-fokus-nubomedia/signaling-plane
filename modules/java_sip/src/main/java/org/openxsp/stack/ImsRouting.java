package org.openxsp.stack;

public enum ImsRouting {
	
	MO("mo"),
	MT("mt"),
	ORIG("orig"),
	TERM("term");
	
	private String value;
	
	ImsRouting(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
