package no.ks.sakimport.fake.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;

public class Run {


    public static void main(String... args) throws Exception {
		Server server = new Server(8102);

		String dir;
		if (new File("src/main/webapp").exists()) dir = "src/main/webapp";
		else dir = "fakeServices/src/main/webapp";

        WebAppContext context = new WebAppContext(dir, "/");
		context.setClassLoader(Thread.currentThread().getContextClassLoader());
		context.setDescriptor(dir+"/WEB-INF/web.xml");
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/javax.servlet-[^/]*\\.jar$|.*/servlet-api-[^/]*\\.jar$");
		context.setResourceBase(dir);
		context.setParentLoaderPriority(true);

        server.setHandler(context);
		server.start();
		server.join();

    }
}