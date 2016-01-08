package org.openxsp.modules.repository;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.openxsp.java.OpenXSP;
import org.openxsp.modules.util.Logger;
import org.openxsp.modules.util.LoggerFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.json.JsonObject;

public class LocalRepositoryMonitor implements RepositoryMonitor {

	private Logger log = LoggerFactory.getLogger(LocalRepositoryMonitor.class);

	private Path moduleFolder;

	private WatchService watcher;
	private Map<WatchKey, String> keyModuleMap;
	private final String address;

	private ModuleDeploymentListener listener;
	
	private FileSystem fs;

	/**
	 * 
	 * @param local_event_address
	 *            The event address of the vertx module which holds the
	 *            repository
	 */
	public LocalRepositoryMonitor(OpenXSP openxsp, String local_event_address) {
		this.address = local_event_address;
		
		fs = openxsp.fileSystem();
	}

	public void start(JsonObject config, ModuleDeploymentListener listener) {

		log.d("Starting repository monitor");

		String modulePath = "mods2";
		if (config.containsField("module_folder"))
			modulePath = config.getString("module_folder");

		Path workingDir = Paths.get(System.getProperty("user.dir"));

		this.moduleFolder = workingDir.resolve(modulePath);

		log.d("Looking for modules in " + this.moduleFolder);

		File f = this.moduleFolder.toFile();
		if (!f.exists()) {
			// Modules Directory does not exist
			// throw new Exception("Modules Directory %s Does Not Exist!");
			if (listener != null)
				listener.onError("Modules directory " + f.toString() + " does not exist!");

			// openXSP.fileSystem().mkdirSync(uploadFolder);
		}

		this.listener = listener;

		try {
			this.watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			if (listener != null) {
				listener.onError(e.getMessage());
			}
			log.e(e.getMessage(), e);
			return;
		}
		this.keyModuleMap = new HashMap<>();

		loadExistingModules();
		setupWatchService();
	}

	public void stop() {
		if (watcher != null) {

			log.d("Stopping watch service");

			try {
				watcher.close();
			} catch (IOException e) {
				log.e(e.getMessage(), e);
			}
		}
	}

	private void loadExistingModules() {

		log.v("Loading existing modules");

		try {
			Files.walkFileTree(this.moduleFolder, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

					log.d("Found directory " + dir.toString());

					/*
					 * Do a non-recursive watch on the root folder to detect
					 * module installation/uninstallation
					 */
					if (dir.equals(moduleFolder)) {
						registerRoot();
						return FileVisitResult.CONTINUE;
					}

					/*
					 * Recursively watch module directory to detect module
					 * modification
					 */
					String moduleName = moduleFolder.relativize(dir).getName(0).toString();
					registerAll(dir, moduleName);
					if (listener != null)
						listener.moduleInstalled(new Module(moduleName, address, null));

					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) throws IOException {

					log.d("Found file " + file.getFileName());

					if (!(file.toString().endsWith(".zip") || file.toString().endsWith(".jar"))) {
						return super.visitFile(file, attrs);
					}

					// try to find config file
					Path configFile = Paths.get(file.toString()+".json");

					log.d("Looking for config file " + configFile);

					File f = configFile.toFile();
					if (f.exists()) {
												
						fs.readFile(configFile.toString(), new Handler<AsyncResult<Buffer>>() {
							
							@Override
							public void handle(AsyncResult<Buffer> res) {
								
								if(res.succeeded() && res.result()!=null){
									String config = new String(res.result().getBytes());
									
									JsonObject configJson = null;
									
									try {
										configJson = new JsonObject(config);
									} catch (Exception e) {
										log.w("Could not read config for module "+config,e);
										configJson = new JsonObject();
									}
									
									if (listener != null)
										listener.moduleInstalled(new Module(file.getFileName().toString(), address, configJson));
								}
							}
						});
					}
					else{
						log.d("Config file does not exist");
						if (listener != null)
							listener.moduleInstalled(new Module(file.getFileName().toString(), address, new JsonObject()));
					}
					

					return super.visitFile(file, attrs);
				}
			});

		} catch (IOException e) {
			log.e(e.getMessage(), e);
		}
	}

	/* WatchService methods */

	/**
	 * Sets up the Watch Service
	 */
	private void setupWatchService() {

		log.v("Starting monitor service");

		new Thread(new Runnable() {
			@Override
			public void run() {
				watch();
			}
		}).start();
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void registerRoot() throws IOException {
		WatchKey key = this.moduleFolder.register(watcher, ENTRY_CREATE, ENTRY_DELETE);
		this.keyModuleMap.put(key, "");

	}

	private void register(Path dir, String moduleName) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
		this.keyModuleMap.put(key, moduleName);

		log.v("Registering " + moduleName + "," + key + "->" + dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService, linked to a ModuleName.
	 */
	private void registerAll(final Path start, final String moduleName) throws IOException {
		// register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				/* Register the Directory */
				register(dir, moduleName);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void watch() {
		boolean exit = false;
		while (!exit) {
			WatchKey key;

			try {
				key = this.watcher.take();
			} catch (InterruptedException e) {
				log.e(e.getMessage());
				return;
			}

			String moduleName = this.keyModuleMap.get(key);

			try {
				if (moduleName.equals("")) {
					handleRootEvent(key);
				} else {
					handleModuleEvent(key, moduleName);
				}
			} catch (IOException e) {
				log.e(e.getMessage(), e);
			}
		}
	}

	private void handleRootEvent(WatchKey key) throws IOException {

		for (WatchEvent<?> event : key.pollEvents()) {

			if (event.kind() == OVERFLOW) {
				continue;
			}

			String moduleName = event.context().toString();
			Path targetPath = this.moduleFolder.resolve(moduleName);

			/* Add new module to watchservice, notify handler of installation */
			if (event.kind() == ENTRY_CREATE) {
				/* Ignore non-module files. Modules must be directories */
				if (!Files.isDirectory(targetPath, NOFOLLOW_LINKS)) {

					if (!(moduleName.endsWith(".zip") || moduleName.endsWith(".json") || moduleName.endsWith(".jar"))) {
						log.v("Unsupported file uploaded " + moduleName);
						return;
					}

					log.d("new file added: " + moduleName);
					// TODO add zip and jar file modules
				}

				this.registerAll(targetPath, moduleName);
				this.listener.moduleInstalled(new Module(moduleName, address, null));

			} else /* notify handler of deletion */
			{
				/*
				 * if not ENTRY_CREATE then must be ENTRY_DELETE as we're not
				 * tracking ENTRY_MODIFY
				 */
				/* Notify */
				this.listener.moduleUninstalled(new Module(moduleName, address, null));
			}
		}

		/* Returns False if key is invalidated */
		if (!key.reset()) {
			/*
			 * TODO: Exception Handling... this key is for root, should never be
			 * invalidated
			 */
			log.w("Bad Error: Root WatchKey Invalidated");
			this.keyModuleMap.remove(key);
		}
	}

	private void handleModuleEvent(WatchKey key, String moduleName) throws IOException {

		for (WatchEvent<?> event : key.pollEvents()) {

			if (event.kind() == OVERFLOW) {
				continue;
			}

			Path targetPath = this.moduleFolder.resolve(moduleName);

			/* Register any new directories for watching */
			if (event.kind() == ENTRY_CREATE) {
				/* Ignore non-directories */
				if (!Files.isDirectory(targetPath, NOFOLLOW_LINKS)) {
					continue;
				}

				this.registerAll(targetPath, moduleName);
			}

			/* Notify */
			this.listener.moduleModified(new Module(moduleName, address, null));
		}

		/* Returns False if key is invalidated */
		if (!key.reset()) {
			log.v("Key Invalidated: " + moduleName);
			this.keyModuleMap.remove(key);
		}
	}
}
