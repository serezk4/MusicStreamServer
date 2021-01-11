package com.serezka.server.engine.server.data;

import com.serezka.server.App;
import com.serezka.server.Start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

public class TcpDataServer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TcpDataServer.class.getSimpleName());

    protected final int serverPort;
    protected final InetAddress serverAddress;

    protected boolean isStopped = false;
    protected Thread runningThread = null;

    protected ServerSocket socket;

    public TcpDataServer(String serverAddress, int serverPort) throws UnknownHostException {
        this.serverPort = serverPort;
        this.serverAddress = InetAddress.getByName(serverAddress);
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();
        while (!isStopped()) {
            try {
                // receive and process
                Socket client = socket.accept();
                new Thread(new TcpRequestHandler(client, socket)).start();
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

    public synchronized void stop() throws IOException {
        this.isStopped = true;
        this.socket.close();
    }

    private void openServerSocket() {
        try {
            this.socket = new ServerSocket(serverPort,
                    Integer.parseInt(Start.getProperties().getProperty(App.Config.SERVER_DATA_MAX_CONNECTIONS.getName())),
                    serverAddress);
        } catch (IOException e) {
            logger.error("Port {} is busy. {}.", this.serverPort, e.getMessage());
        }
    }
}
