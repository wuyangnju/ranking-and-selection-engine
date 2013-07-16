package hk.ust.felab.rase.agent;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SimFinishClient extends SimIoClient {
	protected static CountDownLatch countDownLatch = new CountDownLatch(1);

	public SimFinishClient() {
		super(new SimpleChannelHandler() {
			@Override
			public void messageReceived(ChannelHandlerContext ctx,
					MessageEvent e) throws InterruptedException {
				countDownLatch.countDown();
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx,
					ExceptionEvent e) {
				e.getChannel().close();
			}
		});
	}

	public void isFinish() {
		channel.write(new Boolean(true));
	}

	public static CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}

}
