package com.serezka.server.engine.server.transfer.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class AudioFileServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger("FileServer");

    protected final int serverPort;
    protected final InetAddress address;

    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected Thread runningThread = null;

    public AudioFileServer(String address, int port) throws UnknownHostException {
        this.serverPort = port;
        this.address = InetAddress.getByName(address);
    }

    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();
        while (!isStopped()) {
            try {
                Socket clientSocket = this.serverSocket.accept();
                new Thread(new RequestHandler(clientSocket)).start();
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
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            logger.error("Can't stop server! {}.", e.getMessage());
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            logger.error("Port {} is busy. {}.", this.serverPort, e.getMessage());
        }
    }
}

