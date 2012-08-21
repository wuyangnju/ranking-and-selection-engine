package hk.ust.felab.rase.runner;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbededJettyRunner {

	/**
	 * There're so few start-up parameters that if not so I prefer parsing alone
	 * configure file location from "run.sh" to "args", then load arguments from
	 * configure file as Properties to Conf as Java fields.
	 * 
	 * @param args
	 *            "port" "contextPath" "warPath"
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(args[1]);
		webapp.setWar(args[2]);

		Server server = new Server(Integer.parseInt(args[0]));
		server.setHandler(webapp);
		server.start();
		server.join();
	}
}
