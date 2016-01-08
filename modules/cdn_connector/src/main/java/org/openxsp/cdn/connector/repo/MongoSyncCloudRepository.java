package org.openxsp.cdn.connector.repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;


public class MongoSyncCloudRepository implements CloudRepository{
	
	private static Logger log = LoggerFactory.getLogger(MongoSyncCloudRepository.class);

	
	public static final String
		PARAM_IP = "ip",
		PARAM_PORT = "port",
		PARAM_USER = "user",
		PARAM_PW = "password",
		PARAM_COLLECTION_NAME = "collection_name",
		PARAM_DATABASENAME = "database_name",
		PARAM_DOWNLOADFOLDER = "downloadfolder";
	
	//default names (can be overridden in the configuration)
	private String
		dbName = "admin",
		collectionName = "gridfs",
		downloadFolder = "RepositoryDownloads";
	
	private DB db;
	
	public MongoSyncCloudRepository(String ip, int port, String user, String pw){
		init(ip, port, user, pw);
	}
	
	public MongoSyncCloudRepository(JsonObject config){
		
		dbName = config.getString(PARAM_DATABASENAME, dbName);
		collectionName = config.getString(PARAM_COLLECTION_NAME, collectionName);
		downloadFolder = config.getString(PARAM_DOWNLOADFOLDER, downloadFolder);
		String ip = config.getString(PARAM_IP);
		int port = config.getInteger(PARAM_PORT);
		String user = config.getString(PARAM_USER);
		String pw = config.getString(PARAM_PW);
		
		if(ip==null){
			log.w("The IP address of the database is not configured");
			return;
		}
		if(port==0){
			log.i("The port configuration of the database is invalid");
		}
		
		File folder = new File(downloadFolder);
		if(!folder.exists()){
			log.v("Download folder does not exist - creating new folder "+folder.getAbsolutePath());
			if(!folder.mkdir()){
				log.w("Could not create download folder "+folder.getAbsolutePath());
			}
		}else{
			log.v("Download folder is "+folder.getAbsolutePath());
		}
		
		init(ip, port, user, pw);
	}
	
	private void init(String ip, int port, String user, String pw){
		MongoClient mc = null;
		
		if(user !=null && pw!=null){
			log.v("Creating credentials for "+user+":"+pw);
			MongoCredential c = MongoCredential.createCredential(user, dbName, pw.toCharArray());
			
			mc = new MongoClient(new ServerAddress(ip, port), Arrays.asList(c));
		}
		else{
			String host = port==0 ? ip : ip+":"+port;
			mc= new MongoClient(host);
		}
		
		db = mc.getDB("test"); //FIXME
	}
	
	public void downloadFile(String fileName, DownloadCallback handler){
		GridFS gridfs = new GridFS(db, collectionName);
		
		GridFSDBFile file = gridfs.findOne(fileName);
		 
		// save it into a new image file
		File folder = new File(downloadFolder);
		File download = new File(folder, fileName);
		try {
			file.writeTo(download);
		} catch (IOException e) {
			log.e("Could not write file to downloadFolder");
			if(handler!=null) handler.onDownloadError(fileName, "Could not store file into folder "+downloadFolder);
		}
		
		if(handler!=null){
			handler.onFileDownloaded(download);
		}
	}
	
	
	public void uploadFile(String filePath, UploadCallback handler){
		
		File file = new File(filePath);
 
		if(!file.exists()){
			log.w("File "+filePath+" does not exist");
			
			handler.onUploadError(filePath, "File not found");
			return;
		}
		
		if(!db.collectionExists(collectionName)){
			DBObject options = BasicDBObjectBuilder.start().add("capped", true).add("size", 1000000000).get();
			db.createCollection(collectionName, options);
		}
		
		// Store the file to MongoDB using GRIDFS
		GridFS gridfs = new GridFS(db, collectionName);
		GridFSInputFile gfsFile;
		try {
			gfsFile = gridfs.createFile(new FileInputStream(file), file.getName(), true);
			gfsFile.setFilename(file.getName());
			gfsFile.setId(file.getName());
			gfsFile.save();
			
			if(handler!=null){
				handler.onFileUploaded(file.getName());
			}
		} catch (IOException e) {
			log.e("Could not upload file",e);
			if(handler!=null){
				handler.onUploadError(filePath, e.getMessage());
			}
		}
		
	}

	@Override
	public void getFileNames(RetrieveCallback retrieve) {
		Set<String> fileNames = new HashSet<>();
		GridFS gfs= new GridFS(db, collectionName);
		DBCursor cur = gfs.getFileList();
		
		while (cur.hasNext()){
			DBObject obj = cur.next();
			//log.v("Got object: "+ obj.toString());
			String id = obj.get("_id").toString();
			
			if(id!=null) fileNames.add(id);
		}
		
		retrieve.onRetrieve(fileNames);
	}

	@Override
	public void deleteFile(String fileName, DeleteCallback cb) {
		GridFS gfs = new GridFS(db, collectionName);
		gfs.remove(gfs.findOne(fileName));
		
		if(cb!=null){
			cb.onDeleted(fileName);
		}
	}
}

