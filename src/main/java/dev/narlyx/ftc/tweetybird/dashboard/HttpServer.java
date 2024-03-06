package dev.narlyx.ftc.tweetybird.dashboard;

import java.io.IOException;

public class HttpServer {

    public static void main(String[] args) throws IOException {
        DashboardListener dashboardListener = new DashboardListener(8080,"tweetybird");
        dashboardListener.start();
    }
}
