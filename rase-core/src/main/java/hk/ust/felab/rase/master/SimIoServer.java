package hk.ust.felab.rase.master;

import hk.ust.felab.rase.SimInput;
import hk.ust.felab.rase.SimOutput;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class SimIoServer {
	private transient final Logger log = Logger.getLogger("master.server");

	private ServerBootstrap bootstrap;

	private CountDownLatch rasDone = new CountDownLatch(1);
	private CountDownLatch countDownLatch;

	public SimIoServer(final SimIoServerHelper helper, int agentCount) {

		countDownLatch = new CountDownLatch(agentCount);

		final Object lock = new Object();

		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new ObjectEncoder(),
						new ObjectDecoder(ClassResolvers
								.cacheDisabled(getClass().getClassLoader())),
						new SimpleChannelHandler() {
							@Override
							public void messageReceived(
									ChannelHandlerContext ctx, MessageEvent e)
									throws Exception {
								Object obj = e.getMessage();
								if (obj instanceof Integer) {
									synchronized (lock) {
										int n = (Integer) obj;
										n = Math.min(n, helper.simInputCount());
										SimInput[] simInputs = new SimInput[n];
										for (int i = 0; i < n; i++) {
											simInputs[i] = helper
													.takeSimInputNet();
										}
										e.getChannel().write(simInputs);
										log.debug("server input resp sent");
									}
								} else if (obj instanceof SimOutput[]) {
									SimOutput[] simOutputs = (SimOutput[]) obj;
									for (SimOutput simOutput : simOutputs) {
										helper.putSimOutputNet(simOutput);
									}
									e.getChannel().write(1);
									log.debug("server output resp sent");
								} else if (obj instanceof Boolean) {
									rasDone.await();
									countDownLatch.countDown();
									e.getChannel().write(obj);
									log.debug("server finish resp sent");
								} else {
									log.error(obj);
								}
							}

							@Override
							public void exceptionCaught(
									ChannelHandlerContext ctx, ExceptionEvent e) {
								log.fatal(e.getCause());
								e.getChannel().close();
							}
						});
			}
		});
	}

	public void start(int port) {
		bootstrap.bind(new InetSocketAddress(port));
	}

	public void shutdown() throws InterruptedException {
		bootstrap.shutdown();
		bootstrap.releaseExternalResources();
	}

	public void setRasDone() {
		rasDone.countDown();
	}

	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}
}
