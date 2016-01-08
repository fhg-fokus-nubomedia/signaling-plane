package org.openxsp.cdn.connector.repo;

import java.io.File;

public interface DownloadCallback {
	
	void onFileDownloaded(File file);
	
	void onDownloadError(String fileName, String error);
}
