package com.serezka.server.engine.server.audio;


import com.serezka.server.App;
import com.serezka.server.Start;
import com.serezka.server.engine.audio.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class UdpRequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UdpRequestHandler.class.getSimpleName());

    private final DatagramSocket socket;
    private final DatagramPacket packet;

    public UdpRequestHandler(DatagramSocket socket, DatagramPacket packet) {
        this.socket = socket;
        this.packet = packet;
    }

    public void run() {
        long start = System.currentTimeMillis();

        try {
            // find file
            String trackName = new String(packet.getData()).trim();
            Path file = Paths.get(String.format("%s\\%s\\%s.%s", App.Files.MAIN_DIR, App.Files.TRACKS.getName(), trackName, App.Files.AUDIO_FILE_EXTENSION));
            if (Files.notExists(file)) {
                byte[] errResponse = "track_not_exists".getBytes();
                DatagramPacket response = new DatagramPacket(errResponse, errResponse.length, this.packet.getAddress(), this.packet.getPort());
                socket.send(response);
                return;
            }

            // file exists
            // send audio input stream via udp
            AudioInputStream audioInputStream = AudioManager.getAudioInputStream(file.toFile());
            byte[] buffer = new byte[Integer.parseInt(Start.getProperties().getProperty(App.Config.SERVER_AUDIO_PACKET_SIZE.getName()))];
            int c;
            int packetDelay = Integer.parseInt(Start.getProperties().getProperty(App.Config.SERVER_AUDIO_PACKET_DELAY.getName())); // ms
            while (true) {
                assert audioInputStream != null;
                if ((c = audioInputStream.read(buffer)) == -1) break;
                DatagramPacket data = new DatagramPacket(buffer, c, this.packet.getAddress(), this.packet.getPort());
                socket.send(data);

                // packet delay
                Thread.sleep(packetDelay);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        logger.info("Transfer completed in {} ms.", (System.currentTimeMillis()-start));
    }
}