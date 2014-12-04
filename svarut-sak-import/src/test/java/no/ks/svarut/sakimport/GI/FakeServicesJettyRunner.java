package no.ks.svarut.sakimport.GI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;


public class FakeServicesJettyRunner {

    private Server server;

    public void start() throws Exception {
        server = new Server(8102);

        String dir;

        if (new File("fakeServices/src/main/webapp").exists()) dir = "fakeServices/src/main/webapp";
        else dir = "../fakeServices/src/main/webapp";

        WebAppContext context = new WebAppContext(dir, "/");
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setDescriptor(dir+"/WEB-INF/web.xml");
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/javax.servlet-[^/]*\\.jar$|.*/servlet-api-[^/]*\\.jar$");
        context.setResourceBase(dir);
        context.setParentLoaderPriority(true);

        server.setHandler(context);
        server.start();
    }

    public void waitTillRunning(){
        while(!server.isStarted()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {}
        }
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void join() throws InterruptedException {
        server.join();
    }
}
