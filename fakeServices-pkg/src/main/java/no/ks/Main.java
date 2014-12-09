package no.ks;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.ProtectionDomain;

public class Main {

    private final int port;
    private final String contextPath;
    private final String workPath;
    private final String secret;
    private Server srv;

    public static void main(String[] args) throws Exception {
        Main sc = new Main();
		sc.start().join();
    }

    public Main() {
        try {
            String configFile = System.getProperty("config", "jetty.properties");
            System.getProperties().load(new FileInputStream(configFile));
        } catch (Exception ignored) {}

        port = Integer.parseInt(System.getProperty("jetty.port", "8102"));
        contextPath = System.getProperty("jetty.contextPath", "/");
        workPath = System.getProperty("jetty.workDir", null);
        secret = System.getProperty("jetty.secret", "eb27fb2e61ed603363461b3b4e37e0a0");
    }

    public Server start() {
        // Start a Jetty server with some sensible(?) defaults
        try {
        	System.setProperty("org.eclipse.jetty.util.log.INFO","true");

            srv = new Server(port);
            srv.setStopAtShutdown(true);

            // Get the war-file
            ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
            String warFile = protectionDomain.getCodeSource().getLocation().toExternalForm();
            String currentDir = new File(protectionDomain.getCodeSource().getLocation().getPath()).getParent();

            // Add the warFile (this jar)
            WebAppContext context = new WebAppContext(warFile, contextPath);
            context.setServer(srv);
            resetTempDirectory(context, currentDir);

            // Add the handlers
            HandlerList handlers = new HandlerList();
            handlers.addHandler(context);
            srv.setHandler(handlers);

            srv.start();
            return srv;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void resetTempDirectory(WebAppContext context, String currentDir) throws IOException {
        File workDir;
        if (workPath != null) {
            workDir = new File(workPath);
        } else {
            workDir = new File(currentDir, "work");
        }
        FileUtils.deleteDirectory(workDir);
        context.setTempDirectory(workDir);
    }


    public void waitTillRunning() {
        while(!srv.isStarted()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }
    }

    public void stop() {
        try {
            srv.stop();
        } catch (Exception e) {}
    }
}
