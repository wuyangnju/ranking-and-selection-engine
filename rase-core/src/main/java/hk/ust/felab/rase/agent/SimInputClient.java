package hk.ust.felab.rase.agent;

import hk.ust.felab.rase.SimInput;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SimInputClient extends SimIoClient {
	private static transient final Logger log = Logger
			.getLogger("slave.client.input");

	public static int count = 0;

	public SimInputClient(final SimIoClientHelper helper) {
		super(new SimpleChannelHandler() {
			@Override
			public void messageReceived(ChannelHandlerContext ctx,
					MessageEvent e) throws InterruptedException {
				SimInput[] simInputs = (SimInput[]) e.getMessage();
				for (SimInput simInput : simInputs) {
					helper.putSimInputNet(simInput);
				}
				log.debug("input recv");
				count += simInputs.length;
				helper.unsetWaitingInput();
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx,
					ExceptionEvent e) {
				e.getChannel().close();
			}
		});
	}

	public void takeSimInputs(int n) {
		channel.write(n);
	}

}
