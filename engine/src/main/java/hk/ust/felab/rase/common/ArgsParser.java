package hk.ust.felab.rase.common;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ArgsParser {
	private static final Log log = LogFactory.getLog(ArgsParser.class);
	private static final Gson gson = new GsonBuilder().serializeNulls()
			.setPrettyPrinting().create();

	public static void parse(String[] args) {
		Options options = new Options();
		options.addOption("p", true, "jetty port");
		options.addOption("c", true, "jetty context path");
		options.addOption("w", true, "jetty war");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			log.fatal(e.getMessage());
			System.exit(1);
		}

		Conf.jettyPort = Integer.parseInt(cmd.getOptionValue('p'));
		Conf.jettyContextPath = cmd.getOptionValue('c');
		Conf.jettyWar = cmd.getOptionValue('w');

		log.info("args: " + gson.toJson(new Conf()));
	}
}
