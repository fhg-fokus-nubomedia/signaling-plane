package org.openxsp.cdn.connector.repo;

import java.util.Set;

public interface CloudRepository {

	void downloadFile(String fileName, DownloadCallback handler);

	void uploadFile(String filePath, UploadCallback handler);
	
	void getFileNames(RetrieveCallback retrieve);
	
	void deleteFile(String fileName, DeleteCallback cb);
}
