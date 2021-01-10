package com.serezka.server.engine.server.data;

import com.serezka.server.App;
import com.serezka.server.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

public class UdpDataServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("FileServer");

    protected final int serverPort;
    protected final InetAddress serverAddress;

    protected boolean isStopped = false;
    protected Thread runningThread = null;

    protected final int maxPacketSize = Integer.parseInt(Start.getProperties().getProperty(App.Config.SERVER_PACKET_SIZE.getName()));
    protected final byte[] buffer = new byte[maxPacketSize];
    protected DatagramSocket datagramSocket;

    public UdpDataServer(String address, int port) throws UnknownHostException {
        this.serverPort = port;
        this.serverAddress = InetAddress.getByName(address);
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();
        while (!isStopped()) {
            try {
                // receive and process
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(receivedPacket);
                new Thread(new UdpRequestHandler(datagramSocket, receivedPacket)).start();
            } catch (IOException e) {
                if (isStopped()) {
                    logger.info("Server stopped!");
                    return;
                }
                logger.error("Error accepting client connection! {}.", e.getMessage());
            }
        }
        logger.info("Server stopped!");
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        this.isStopped = true;
        this.datagramSocket.close();
    }

    private void openServerSocket() {
        try {
            this.datagramSocket = new DatagramSocket(this.serverPort, this.serverAddress);
        } catch (IOException e) {
            logger.error("Port {} is busy. {}.", this.serverPort, e.getMessage());
        }
    }
}

