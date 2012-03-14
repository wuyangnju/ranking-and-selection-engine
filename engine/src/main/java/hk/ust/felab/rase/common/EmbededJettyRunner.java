package hk.ust.felab.rase.common;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbededJettyRunner {
	public static void main(String[] args) throws Exception {
		ArgsParser.parse(args);

		Server server = new Server(Conf.jettyPort);
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(Conf.jettyContextPath);
		webapp.setWar(Conf.jettyWar);
		server.setHandler(webapp);
		server.start();
		server.join();
	}
}
