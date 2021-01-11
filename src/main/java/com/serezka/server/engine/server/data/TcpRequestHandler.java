package com.serezka.server.engine.server.data;

import com.serezka.server.App;
import com.serezka.server.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TcpRequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TcpRequestHandler.class.getSimpleName());

    private final Socket client;
    private final ServerSocket server;

    public TcpRequestHandler(Socket client, ServerSocket server) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        // create in-out obj
        try (BufferedOutputStream clientOut = new BufferedOutputStream(client.getOutputStream());
             BufferedInputStream clientIn = new BufferedInputStream(client.getInputStream())) {

            // check file
            String trackName = new String(clientIn.readAllBytes()).trim();
            Path file = Paths.get(String.format("%s\\%s\\%s.%s", App.Files.MAIN_DIR, App.Files.TRACKS.getName(), trackName, App.Files.AUDIO_FILE_EXTENSION));
            if (Files.notExists(file)) {
                clientOut.write("track_not_exists".getBytes());
                return;
            }

            // send file
            try (FileInputStream fileIn = new FileInputStream(file.toFile())) {
                byte[] buffer = new byte[Integer.parseInt(Start.getProperties().getProperty(App.Config.SERVER_AUDIO_PACKET_SIZE.getName()))];
                int c;
                while ((c = fileIn.read(buffer)) != -1) {
                    clientOut.write(buffer, 0, c);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        logger.info("Request completed in {} ms", (System.currentTimeMillis()-start));
    }
}
