package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.SimOutput;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SimOutputClient extends SimIoClient {
	private static transient final Logger log = Logger
			.getLogger("slave.client.output");

	public static int count = 0;

	public SimOutputClient(final SimIoClientHelper helper) {
		super(new SimpleChannelHandler() {
			@Override
			public void messageReceived(ChannelHandlerContext ctx,
					MessageEvent e) {
				log.debug("output recv");
				helper.unsetWaitingOutput();
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx,
					ExceptionEvent e) {
				e.getChannel().close();
			}
		});
	}

	public void putSimOutputs(SimOutput[] simOutputs) {
		count += simOutputs.length;
		channel.write(simOutputs);
	}

}
