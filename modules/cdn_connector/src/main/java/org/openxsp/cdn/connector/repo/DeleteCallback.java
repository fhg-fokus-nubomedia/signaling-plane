package org.openxsp.cdn.connector.repo;

public interface DeleteCallback {

	void onDeleted(String fileName);
	
	void onDeleteError(String fileName, String errorMessage);
}
