package org.openxsp.modules.web;

import org.openxsp.java.OpenXSP;
import org.openxsp.modules.util.Logger;
import org.openxsp.modules.util.LoggerFactory;
import org.vertx.java.core.*;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.streams.Pump;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.UUID;

/**
 * Created by xsp on 2/20/15.
 */
public class UploadHandler implements Handler<HttpServerRequest> {

	private Logger log = LoggerFactory.getLogger(UploadHandler.class);
	
	public String uploadFolder = "mods2"+FileSystems.getDefault().getSeparator();
	
	private static final String UPLOADURL = "/upload/";
	
    private OpenXSP openXSP;

    public UploadHandler(OpenXSP openXSP, String uploadFolder){
        this.openXSP = openXSP;
        if(uploadFolder!=null){
        	this.uploadFolder = uploadFolder;
        	if(this.uploadFolder.endsWith(FileSystems.getDefault().getSeparator())){
        		this.uploadFolder += FileSystems.getDefault().getSeparator();
        	}
        }
    }

    @Override
    public void handle(final HttpServerRequest req) {

        log.i("Handling new file upload");
        // We first pause the request so we don't receive any data between now and when the file is opened
        
        //test if the upload folder exists
        if(!openXSP.fileSystem().existsSync(uploadFolder)){
        	log.d("Creating upload folder");
        	openXSP.fileSystem().mkdirSync(uploadFolder);
        }
        else log.d("Upload folder exists");
        
        req.pause();
        
        int start = req.path().indexOf(UPLOADURL)+UPLOADURL.length();
        
        final String filename = uploadFolder+"/" + req.path().substring(start, req.path().length());
        
        log.v("file name is "+filename);
        
        //test if the file already exists
        if(openXSP.fileSystem().existsSync(filename)){
        	log.d("Deleting existing file");
        	openXSP.fileSystem().deleteSync(filename);
        }
        
        try{
        	openXSP.fileSystem().open(filename, new AsyncResultHandler<AsyncFile>() {
                public void handle(AsyncResult<AsyncFile> ar) {
                    if (ar.failed()) {
                        ar.cause().printStackTrace();
                        return;
                    }
                    final AsyncFile file = ar.result();
                    final Pump pump = Pump.createPump(req, file);
                    final long start = System.currentTimeMillis();
                    req.endHandler(new VoidHandler() {
                        public void handle() {
                            file.close(new AsyncResultHandler<Void>() {
                                public void handle(AsyncResult<Void> ar) {
                                    if (ar.succeeded()) {
                                        req.response().end();
                                        long end = System.currentTimeMillis();
                                        log.v("Uploaded " + pump.bytesPumped() + " bytes to " + filename + " in " + (end - start) + " ms");
                                    } else {
                                        ar.cause().printStackTrace(System.err);
                                    }
                                }
                            });
                        }
                    });
                    pump.start();
                    req.resume();
                }
            });
        }catch(Exception e){
        	log.e(e.getMessage(),e);
        	req.resume();
        }
        
    }
}
