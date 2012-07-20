package com.dianping.swallow.producer.impl.internal;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.filequeue.DefaultFileQueueConfig.FileQueueConfigHolder;
import com.dianping.filequeue.DefaultFileQueueImpl;
import com.dianping.filequeue.FileQueue;
import com.dianping.filequeue.FileQueueClosedException;
import com.dianping.swallow.common.internal.packet.Packet;
import com.dianping.swallow.common.internal.producer.ProducerSwallowService;
import com.dianping.swallow.common.internal.threadfactory.DefaultPullStrategy;
import com.dianping.swallow.common.internal.threadfactory.MQThreadFactory;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.dianping.swallow.producer.ProducerHandler;

/**
 * Producer的异步模式消息处理类
 * 
 * @author tong.song
 */
public class HandlerAsynchroMode implements ProducerHandler {
   private static final Logger                   logger                 = LoggerFactory
                                                                              .getLogger(HandlerAsynchroMode.class);
   private static final MQThreadFactory          threadFactory          = new MQThreadFactory();                     //从FileQueue中获取消息的线程池

   private static final int                      DEFAULT_FILEQUEUE_SIZE = 512 * 1024 * 1024;                        //默认的filequeue切片大小，512MB
   private static final int                      DELAY_BASE_MULTI       = 5;                                        //超时策略倍数

   private static Map<String, FileQueue<Packet>> messageQueues          = new HashMap<String, FileQueue<Packet>>(); //当前TopicName与Filequeue对应关系的集合

   private final ProducerImpl                    producer;
   private final FileQueue<Packet>               messageQueue;                                                      //Filequeue
   private final int                             delayBase;                                                         //超时策略基数

   /**
    * 获取指定topicName及选项的FileQueue，如果已经存在则返回引用，如果不存在就创建新的FileQueue
    * 
    * @param topicName 消息目的地名称
    * @param sendMsgLeftLastSessions 是否重启续传
    * @return 指定参数的FileQueue
    */
   private synchronized static FileQueue<Packet> getMessageQueue(String topicName, boolean sendMsgLeftLastSessions) {
      //如果Map里已经存在该filequeue，在要求“不续传”的情况下， 忽略该请求
      if (messageQueues.containsKey(topicName))
         return messageQueues.get(topicName);

      FileQueueConfigHolder fileQueueConfig = new FileQueueConfigHolder();
      fileQueueConfig.setMaxDataFileSize(DEFAULT_FILEQUEUE_SIZE);
      //如果Map里不存在该filequeue，此handler又要求将之前的文件删除，则删除
      FileQueue<Packet> newQueue = new DefaultFileQueueImpl<Packet>(fileQueueConfig, topicName, sendMsgLeftLastSessions);
      messageQueues.put(topicName, newQueue);
      return messageQueues.get(topicName);
   }

   //构造函数
   public HandlerAsynchroMode(ProducerImpl producer) {
      this.producer = producer;
      delayBase = producer.getRemoteServiceTimeout();
      messageQueue = getMessageQueue(producer.getDestination().getName(), producer.getProducerConfig()
            .isSendMsgLeftLastSession());
      this.start();
   }

   /**
    * 异步处理只需将pkt放入filequeue即可，放入失败抛出异常
    */
   @Override
   public Packet doSendMsg(Packet pkt) throws SendFailedException {
      try {
         messageQueue.add(pkt);
      } catch (FileQueueClosedException e) {
         throw new SendFailedException("Add message to filequeue failed.", e);
      }
      return null;
   }

   //启动处理线程
   private void start() {
      int threadPoolSize = producer.getProducerConfig().getThreadPoolSize();
      for (int idx = 0; idx < threadPoolSize; idx++) {
         Thread t = threadFactory.newThread(new TskGetAndSend(), "swallow-AsyncProducer-");
         t.setDaemon(true);
         t.start();
      }
   }

   //从filequeue队列获取并发送Message
   private class TskGetAndSend implements Runnable {

      private final int              sendTimes      = producer.getProducerConfig().getRetryTimes() + 1;
      private int                    leftRetryTimes = sendTimes;
      private Packet                 message        = null;
      private ProducerSwallowService remoteService  = producer.getRemoteService();

      @Override
      public void run() {
         //异步模式下，每个线程单独有一个延时策略，以保证不同的线程不会互相冲突
         DefaultPullStrategy defaultPullStrategy = new DefaultPullStrategy(delayBase, DELAY_BASE_MULTI * delayBase);

         while (true) {
            //重置延时
            defaultPullStrategy.succeess();
            //从filequeue获取message，如果filequeue无元素则阻塞            
            message = messageQueue.get();
            //发送message，重试次数从Producer获取
            for (leftRetryTimes = sendTimes; leftRetryTimes > 0;) {
               leftRetryTimes--;
               try {
                  remoteService.sendMessage(message);
               } catch (Exception e) {
                  //如果剩余重试次数>0，超时重试
                  if (leftRetryTimes > 0) {
                     try {
                        defaultPullStrategy.fail(true);
                     } catch (InterruptedException ie) {
                        return;
                     }
                     //发送失败，重发
                     continue;
                  }
                  logger.error("Message sent failed: " + message.toString(), e);
               }
               //如果发送成功则跳出循环
               break;
            }
         }
      }
   }
}
