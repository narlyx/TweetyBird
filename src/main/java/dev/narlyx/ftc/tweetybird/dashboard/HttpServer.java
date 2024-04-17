package dev.narlyx.ftc.tweetybird.dashboard;

import java.io.IOException;

import dev.narlyx.ftc.tweetybird.dashboard.server.DashboardListener;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        DashboardListener listener = new DashboardListener(8080,"tweetybird");
        listener.start();
    }
}
