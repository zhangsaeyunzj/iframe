package com.nuonuo.iframe.netty;

import com.nuonuo.iframe.handler.ActionHandler;
import com.nuonuo.iframe.utils.ResourceBundle;
import com.xiaoleilu.hutool.util.DateUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.ThreadFactory;

/**
 * LoServer starter<br>
 * 用于启动服务器的主对象<br>
 * 使用LoServer.start()启动服务器<br>
 * 服务的Action类和端口等设置在ServerSetting中设置
 *
 * @author Looly
 */
public class IServer {
    private static final Logger log = LogManager.getLogger(IServer.class.getName());

    /**
     * 启动服务
     *
     * @param port 端口
     * @throws InterruptedException
     */
    public void start(int port) throws InterruptedException {
        long start = System.currentTimeMillis();

        ThreadFactory acceptThreadFactory = new DaemonThreadFactory("Boss");
        ThreadFactory workThreadFactory = new DaemonThreadFactory("Work");

        // Configure the server.
        final EventLoopGroup bossGroup = new NioEventLoopGroup(1,
                acceptThreadFactory);
        final EventLoopGroup workerGroup = new NioEventLoopGroup(200,
                workThreadFactory);

        try {
            final ServerBootstrap b = new ServerBootstrap();
            final Integer timeOut = Integer.parseInt((String)ResourceBundle.loadMap.get("server.time.out"));
            b.group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline()
                                    .addLast("readTimeOutSeconds", new ReadTimeoutHandler(timeOut==null?20:timeOut))
                                    .addLast(new HttpServerCodec())
                                    .addLast(new StringDecoder(Charset.forName("UTF-8")))
                                    // 把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse
                                    .addLast(new HttpObjectAggregator(655360000))
                                    // 压缩Http消息
                                    // .addLast(new
                                    // HttpChunkContentCompressor())
                                    // 大文件支持
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new ActionHandler());

                        }
                    });

            final Channel ch = b.bind(port).sync().channel();
            log.info(
                    "***** Welcome To LoServer on port [{}], startting spend {}ms *****",
                    port, DateUtil.spendMs(start));
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
