package com.serezka.server.engine.server.transfer.file;

import com.serezka.server.engine.audio.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.net.Socket;

/**
 *
 */
public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("RequestHandler");

    protected final Socket clientSocket;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            try (OutputStream clientSocketOutputStream = clientSocket.getOutputStream()) {

                File file = new File("C:\\MusicStreamServer\\tracks\\sss.wav");
                AudioInputStream in = AudioManager.getAudioInputStream(file);

                long start = System.currentTimeMillis();
                byte[] buffer = new byte[4096];
                int c;
                while (true) {
                    assert in != null;
                    if ((c = in.read(buffer, 0, buffer.length)) == -1) break;
                    clientSocketOutputStream.write(buffer, 0, c);
                }

                logger.info("transfer completed in {} ms.", System.currentTimeMillis() - start);
            }

        } catch (Exception e) {
            //report exception somewhere.
            logger.error("Exception! {}.", e.getMessage());

        }
    }


}