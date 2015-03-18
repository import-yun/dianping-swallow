package com.dianping.swallow.example.consumer;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.bson.types.BSONTimestamp;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.dianping.swallow.common.internal.util.MongoUtils;
import com.dianping.swallow.common.message.Destination;
import com.dianping.swallow.common.message.Message;
import com.dianping.swallow.consumer.Consumer;
import com.dianping.swallow.consumer.ConsumerConfig;
import com.dianping.swallow.consumer.MessageListener;
import com.dianping.swallow.consumer.impl.ConsumerFactoryImpl;

public class GetFromSpecificTimeConsumerExample {

	private Logger logger = Logger.getLogger(getClass());
	
    private long lastMessageId = 0; 
    
    private Map<Long, AtomicInteger> messageCount = new ConcurrentHashMap<Long, AtomicInteger>();

    public static void main(String[] args) {

    	new GetFromSpecificTimeConsumerExample().start();
    }

	private void start() {
		
        ConsumerConfig config = new ConsumerConfig();
        config.setThreadPoolSize(1);
        
        final Date beginTime = getBeginTime(100);
        final long beginMessageId =  getMessageId(beginTime);
//        config.setStartMessageId(beginMessageId);
        
        System.err.println("begin Time:" + beginTime);
        
        		
        Consumer c = ConsumerFactoryImpl.getInstance().createConsumer(Destination.topic("example"), "myId2", config);
        c.setListener(new MessageListener() {

            @Override
            public void onMessage(Message msg) {
            	
                System.out.println("延迟" + (System.currentTimeMillis() - msg.getGeneratedTime().getTime()) + "ms");
                if(logger.isInfoEnabled()){
                	logger.info(msg.getMessageId());
                	logger.info(msg.getContent());
                }

            	long currentMessageId = msg.getMessageId();
            	if(currentMessageId < lastMessageId){
            		logger.error("[Current messageId < lastMessageId]" + currentMessageId + "," + lastMessageId);
            	}
                if(msg.getMessageId() <= beginMessageId){
                	logger.error("[Wrong message!!][current messageId < beginMessageId]" );
                }
                
                lastMessageId = currentMessageId;
                
                if(messageCount.get(currentMessageId) == null){
                	messageCount.put(currentMessageId, new AtomicInteger());
                }
                int count = messageCount.get(currentMessageId).incrementAndGet();
                if(count > 1){
                	logger.error("[message occur multi times]" + count);
                }
            }
        });
        c.start();
		
	}

	private static Date getBeginTime(int secondsBefore) {
		
		long current = System.currentTimeMillis()/1000;
		long beginTime = current - secondsBefore;
		return new Date(beginTime*1000);
	}

	private static long getMessageId(Date beginTime) {
		
		return MongoUtils.BSONTimestampToLong(new BSONTimestamp((int)(beginTime.getTime()/1000), 0));
	}
}