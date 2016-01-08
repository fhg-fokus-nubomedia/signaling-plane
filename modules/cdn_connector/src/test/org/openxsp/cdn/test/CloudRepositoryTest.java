package org.openxsp.cdn.test;

import java.io.File;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.junit.Test;
import org.openxsp.cdn.connector.repo.CloudRepository;
import org.openxsp.cdn.connector.repo.DeleteCallback;
import org.openxsp.cdn.connector.repo.DownloadCallback;
import org.openxsp.cdn.connector.repo.MongoSyncCloudRepository;
import org.openxsp.cdn.connector.repo.RepositoryFactory;
import org.openxsp.cdn.connector.repo.RetrieveCallback;
import org.openxsp.cdn.connector.repo.UploadCallback;
import org.openxsp.cdn.connector.util.log.Logger;
import org.openxsp.cdn.connector.util.log.LoggerFactory;
import org.vertx.java.core.json.JsonObject;

public class CloudRepositoryTest extends TestCase {
	
	Logger log = LoggerFactory.getLogger(CloudRepositoryTest.class);

	private CloudRepository repo;

	public void init() {
		if (repo == null) {

			log.d("initializing DB");
			JsonObject config = new JsonObject().putString(MongoSyncCloudRepository.PARAM_IP, "80.96.122.58")
					.putNumber(MongoSyncCloudRepository.PARAM_PORT, 27020)
					.putString(MongoSyncCloudRepository.PARAM_PW, "*4admin#"/*"nub0m3d1@"*/)
					.putString(MongoSyncCloudRepository.PARAM_USER, "admin"/*"nubomedia"*/);

			repo = RepositoryFactory.getRepository(config);
		}
	}

	private boolean success = false;

	@Test
	public void testUpload() {
		init();

		
		final CountDownLatch latch = new CountDownLatch(1);
		final File file = new File("sample-video.mp4");

		if (!file.exists()) {
			fail("File " + file.getAbsolutePath() + " does not exist");
		}
		
		//clean up
		log.d("resetting database - deleting existing file");
		final CountDownLatch latch0 = new CountDownLatch(1);
		success = false;

		repo.deleteFile(file.getName(), new DeleteCallback() {
			
			@Override
			public void onDeleted(String fileName) {
				assertEquals(file.getName(), fileName);
				success = true;
				latch0.countDown();
			}
			
			@Override
			public void onDeleteError(String fileName, String errorMessage) {
				fail("Could not delete file "+fileName+": "+errorMessage);
			}
		});

		try {
			latch0.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertTrue(success);
		log.d("Success");

		log.d("Uploading file");
		///// try to upload a file  /////
		repo.uploadFile(file.getName(), new UploadCallback() {

			@Override
			public void onUploadError(String path, String error) {
				fail(error);

			}

			@Override
			public void onFileUploaded(String id) {
				success = true;

				latch.countDown();
			}
		});

		try {
			latch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			fail();
			e.printStackTrace();
		}
		assertTrue(success);
		log.d("Success");

		///// check if the file is in the repo /////
		log.d("Getting file list from the database");
		success = false;

		final CountDownLatch latch2 = new CountDownLatch(1);

		repo.getFileNames(new RetrieveCallback() {

			@Override
			public void onRetrieveError(String message) {
				fail(message);
			}

			@Override
			public void onRetrieve(Set<String> files) {

				assertEquals(1, files.size());
				success = true;

			}
		});

		try {
			latch2.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		assertTrue(success);
		log.d("Success");

		///// try to download the file  /////
		log.d("Trying to download file "+file.getName());
		final CountDownLatch latch3 = new CountDownLatch(1);
		success = false;

		repo.downloadFile(file.getName(), new DownloadCallback() {

			@Override
			public void onFileDownloaded(File download) {
				assertNotNull(download);
				assertTrue(download.exists());
				assertEquals(file.getName(), download.getName());
				success = true;
				latch3.countDown();
			}

			@Override
			public void onDownloadError(String fileName, String error) {
				fail("Could not download file: " + error);
				latch3.countDown();
			}
		});

		try {
			latch3.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.e("",e);
			fail(e.getMessage());
		}

		assertTrue(success);
		log.d("Success");

		log.d("Trying to delete file");
		// try to delete the file
		final CountDownLatch latch4 = new CountDownLatch(1);
		success = false;

		repo.deleteFile(file.getName(), new DeleteCallback() {
			
			@Override
			public void onDeleted(String fileName) {
				assertEquals(file.getName(), fileName);
				success = true;
				latch4.countDown();
			}
			
			@Override
			public void onDeleteError(String fileName, String errorMessage) {
				fail("Could not delete file "+fileName+": "+errorMessage);
			}
		});

		try {
			latch4.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertTrue(success);
		log.d("Success");
	}
}
