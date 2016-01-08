package org.openxsp.cdn.connector.repo;

public interface UploadCallback {
	
	void onFileUploaded(String id);

	void onUploadError(String path, String error);
}
