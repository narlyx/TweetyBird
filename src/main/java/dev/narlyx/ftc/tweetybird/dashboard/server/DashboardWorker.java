package dev.narlyx.ftc.tweetybird.dashboard.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Creates a new thread to send data to a connection
 */
public class DashboardWorker extends Thread {

    /**
     * Reference to the current socket used
     */
    private Socket socket;

    /**
     * Constructor
     */
    public DashboardWorker(Socket socket) {
        //Setting Variable
        this.socket = socket;
    }

    /**
     * Thread
     */
    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            //IO steams
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            //Html reference
            String html = "<html><head><title>Test</title></head><body><h1>Yippe</h1></body></html>";

            //Short cut to "define a new line"
            final String CRLF = "\n\r"; // 13, 10

            //Response reference
            String response = "HTTP/1.1 200 OK " + CRLF +
                    " Content-Length: " + html.getBytes().length + CRLF +
                    CRLF +
                    html +
                    CRLF +
                    CRLF;

            //Output the response
            outputStream.write(response.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally { //Close Connection
            if (inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
            if (socket!=null) {
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
    }
}
