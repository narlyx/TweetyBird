package dev.narlyx.ftc.tweetybird.dashboard;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DashboardListener extends Thread {

    /**
     * Defines the port of the socket/dashboard
     */
    private int port;
    /**
     * Defines the webroot of the socket/dashboard
     */
    private String webroot;

    /**
     * Server Socket
     */
    ServerSocket serverSocket;

    /**
     * Constuctor
     */
    public DashboardListener(int port, String webroot) throws IOException {
        //Setting Variables
        this.port = port;
        this.webroot = webroot;

        //Open Socket
        serverSocket = new ServerSocket(this.port);
    }

    /**
     * Thread
     */
    @Override
    public void run() {
        try { //Open new connection
            while (serverSocket.isBound() && !serverSocket.isClosed()) { //Keep connections open
                //Accept incoming connections
                Socket socket = serverSocket.accept();

                //Sending to worker thread
                DashboardWorker workerThread = new DashboardWorker(socket);
                workerThread.start();

                //Note: socket will eventually be closed at the end of the opened thread
            }

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally { //Close Socket
            if (serverSocket!=null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {}
            }
        }
    }
}
