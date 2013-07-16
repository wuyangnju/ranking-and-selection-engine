package hk.ust.felab.rase.agent;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class SimIoClient {
	private ClientBootstrap bootstrap;
	protected Channel channel;

	public SimIoClient(final SimpleChannelHandler handler) {
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newFixedThreadPool(1),
				Executors.newFixedThreadPool(1)));

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(
						new ObjectEncoder(),
						new ObjectDecoder(ClassResolvers
								.cacheDisabled(getClass().getClassLoader())),
						handler);
			}
		});
	}

	public void start(String server, int port) {
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(server,
				port));
		channel = future.awaitUninterruptibly().getChannel();
	}

	public void shutdown() throws InterruptedException {
		bootstrap.shutdown();
		bootstrap.releaseExternalResources();
	}
}
