package com.serezka.server.engine.server.transfer.file;

import com.serezka.server.App;
import com.serezka.server.engine.audio.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("RequestHandler");

    protected final Socket clientSocket;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            try (OutputStream clientOutput = clientSocket.getOutputStream();
                 BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {

                // check file status
                String fileName = clientReader.readLine().trim();
                Path destFile = Paths.get(String.format("%s\\%s\\%s.%s", App.Files.MAIN_DIR, App.Files.TRACKS.getName(), fileName, App.Files.AUDIO_FILE_EXTENSION));
                if (Files.notExists(destFile)) {
                    clientOutput.write(App.Exceptions.FILE_NOT_EXISTS.getBytes());
                    return;
                }
                FileInputStream in  = new FileInputStream(destFile.toFile());


                long start = System.currentTimeMillis();
                int count;
                byte[] buffer = new byte[4096]; // or 4096, or more
                while ((count = in.read(buffer)) > 0) {
                    clientOutput.write(buffer, 0, count);
                }

                logger.info("transfer completed in {} ms.", System.currentTimeMillis() - start);
            }

        } catch (Exception e) {
            //report exception somewhere.
            logger.error("Exception! {}.", e.getMessage());

        }
    }


}