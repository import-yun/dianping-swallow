package com.dianping.swallow.consumer.internal.netty;


import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.swallow.common.internal.action.SwallowCatActionWrapper;
import com.dianping.swallow.common.internal.consumer.ConsumerMessageType;
import com.dianping.swallow.common.internal.packet.PktConsumerMessage;
import com.dianping.swallow.common.internal.processor.ConsumerProcessor;
import com.dianping.swallow.common.internal.util.IPUtil;
import com.dianping.swallow.consumer.internal.ConsumerImpl;
import com.dianping.swallow.consumer.internal.task.DefaultConsumerTask;
import com.dianping.swallow.consumer.internal.task.TaskChecker;

/**
 * <em>Internal-use-only</em> used by Swallow. <strong>DO NOT</strong> access
 * this class outside of Swallow.
 *
 * @author zhang.yu
 */
public class MessageClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageClientHandler.class);

    private final ConsumerImpl  			consumer;
    private ConsumerProcessor 				processor;
    private SwallowCatActionWrapper 		actionWrapper;
    private String 							connectionDesc;
    private TaskChecker 					taskChecker;

    public MessageClientHandler(ConsumerImpl consumer, ConsumerProcessor processor, TaskChecker taskChecker, SwallowCatActionWrapper actionWrapper) {
    	
        this.consumer = consumer;
        this.processor = processor;
        this.actionWrapper = actionWrapper;
        this.taskChecker = taskChecker;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        PktConsumerMessage consumerMessage = new PktConsumerMessage(ConsumerMessageType.GREET, consumer.getConsumerId(),
                consumer.getDest(), consumer.getConfig().getConsumerType(), consumer.getConfig().getThreadPoolSize(),
                consumer.getConfig().getMessageFilter());
        consumerMessage.setMessageId(consumer.getConfig().getStartMessageId());
        connectionDesc = IPUtil.getConnectionDesc(e);
        e.getChannel().write(consumerMessage);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelDisconnected(ctx, e);
        if(logger.isInfoEnabled()){
        	logger.info("Channel(remoteAddress=" + e.getChannel().getRemoteAddress() + ") disconnected");
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e) {
    	
        if (logger.isDebugEnabled()) {
            logger.debug("MessageReceived from " + connectionDesc);
        }

        //如果已经close，接收到消息时，不回复ack，而是关闭连接。
        if(consumer.isClosed()){
            logger.info("Message receiced, but it was rejected because consumer was closed.");
            ctx.getChannel().close();
            return;
        }
        
        this.consumer.submit(new DefaultConsumerTask(ctx, e, consumer, processor, actionWrapper, taskChecker));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        Channel channel = e.getChannel();
        logger.error("Error from channel(" + connectionDesc  + ")", e);
        channel.close();
    }
}
