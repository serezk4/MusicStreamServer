package com.serezka.server;

import com.serezka.server.engine.server.transfer.file.AudioFileServer;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Start {
    private static final Logger logger = LoggerFactory.getLogger("Start");
    private static Properties properties;

    public static Properties getProperties() {
        return properties;
    }

    // LOAD
    public static void main(String[] args) {
        // read app arguments
        for (String loadArgument : args) {
            if (loadArgument.equalsIgnoreCase("-loadconf")) {
                System.out.println(" = FULL LOAD CONFIGURATION = ");
                System.getProperties().forEach((k, v) -> System.out.printf("%s = %s%n", k, v));
                System.out.println();
            } else {
                System.out.printf("Unknown argument: %s.", loadArgument);
            }
        }

        // print small load inf (java ver, encoding....)
        System.out.printf(" = SHORT LOAD CONFIGURATION = %njava version: %s%nencoding: %s%ndebug: %s%n%n",
                System.getProperty("java.version"),
                System.getProperty("sun.jnu.encoding"),
                System.getProperty("jdk.debug"));

        // print hardware config
        System.out.printf(" = HARDWARE = %nCPU: %d threads %nRAM (total): %d MiB%n%n",
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().totalMemory() / 1048576); // 1mib = 1048576bytes

        // pre-load / configure logger
        BasicConfigurator.configure();
        logger.info("BasicConfigurator configured.");

        // create main dir
        Path mainDir = Paths.get(App.Files.MAIN_DIR);
        if (!isDirectoryExists(mainDir)) {
            try {
                Files.createDirectory(mainDir);
            } catch (IOException e) {
                logger.error("Can't create app dir! [{}]", mainDir.toString());
                logger.error(e.getMessage());
                return;
            }
        } else {
            logger.info("Main directory detected. [{}].", mainDir.toString());
        }

        // start main thread
        new Start().run(mainDir);
    }

    // START
    private void run(Path dir) {
        long start = System.currentTimeMillis();

        // LOAD
        // check dir exists
        if (isDirectoryExists(dir)) logger.info("Dir successfully loaded! [{}].", dir.toString());
        else return;

        // check files health
        if (!checkFiles(dir)) logger.info("All files restored.");
        else logger.info("All files are intact.");

        // load properties
        Path propertiesFile = Paths.get(String.format("%s\\%s", dir.toString(), App.Files.CONFIG.getName()));
        properties = loadProperties(propertiesFile);
        if (properties != null) logger.info("Properties intact.");
        else {
            logger.error("Properties are null!");
            System.out.println(" = PROPERTIES EXAMPLE = ");
            for (App.Config val : App.Config.values()) System.out.printf("%s=\n", val.getName());
            return;
        }

        // init
        logger.info("Server initialization...");
        Thread fileServer;
        try {
            fileServer = new Thread(new AudioFileServer(
                    properties.getProperty(App.Config.SERVER_IP.getName()),
                    Integer.parseInt(properties.getProperty(App.Config.SERVER_PORT_FILE.getName()))
            ));
        } catch (UnknownHostException e) {
            logger.error("Can't start file server! {}", e.getMessage());
            return;
        }


        // start
        fileServer.setName("Server");
        fileServer.start();
        logger.info("Server started successfully in {} ms! ", (System.currentTimeMillis() - start));
    }

    /**
     * @param file - get properties from this file
     * @return properties in selected file
     */
    private Properties loadProperties(Path file) {
        if (Files.notExists(file)) {
            logger.error("Properties file doesn't exist!");
            return null;
        }

        try {
            Properties properties = new Properties();
            properties.load(Files.newInputStream(file));

            boolean leak = false;
            for (App.Config configValue : App.Config.values()) {
                if (!properties.containsKey(configValue.getName())) {
                    logger.error("Missing properties: {}.", configValue.getName());
                    leak = true;
                }
            }

            if (leak) throw new NullPointerException("Properties load failed.");
            return properties;
        } catch (IOException | NullPointerException e) {
            logger.error(e.getMessage());
            return null;
        }

    }

    /**
     * @param dir - what to check
     * @return directory validity (bool)
     */
    private static boolean isDirectoryExists(Path dir) {
        if (dir == null) {
            logger.error("Path is null.");
            return false;
        } else if (!Files.isDirectory(dir)) { // check dir is created
            logger.error("Directory doesn't exist! [{}].", dir.toString());
            return false;
        } else return true; // all OK.
    }

    /**
     * @param dir - where to find files
     * @return is files damaged, damaged files - restored
     */
    private boolean checkFiles(Path dir) {
        boolean healthy = true;

        for (App.Files file : App.Files.values()) {
            Path path = Paths.get(String.format("%s\\%s", dir.toString(), file.getName()));
            if (Files.notExists(path)) { // check if file exists
                logger.error("Files damaged! Not exists {}: {}.", file.getType(), file.getName());
                try { // regenerate file
                    healthy = false;
                    if (file.getType() == App.Files.Type.FILE) Files.createFile(path);
                    else Files.createDirectory(path);
                } catch (IOException e) { // unknown error
                    logger.error(e.getMessage());
                }
            }
        }

        // return file status
        return healthy;
    }
}
