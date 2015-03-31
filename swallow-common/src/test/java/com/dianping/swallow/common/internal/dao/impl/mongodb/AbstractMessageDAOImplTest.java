package com.dianping.swallow.common.internal.dao.impl.mongodb;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bson.types.BSONTimestamp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.swallow.common.internal.dao.MessageDAO;
import com.dianping.swallow.common.internal.message.SwallowMessage;
import com.dianping.swallow.common.internal.util.MongoUtils;

/**
 * @author mengwenchao
 * 
 *         2015年3月26日 下午2:40:39
 */
public abstract class AbstractMessageDAOImplTest extends AbstractDAOImplTest {

	@Autowired
	protected MessageDAO messageDAO;

	protected String consumerId = "consumer1";

	@Before
	public void beforeMessageDAOImplTest() {
		messageDAO.cleanMessage(TOPIC_NAME, null);
		messageDAO.cleanMessage(TOPIC_NAME, consumerId);
	}

	@Test
	public void testDeleteMessage() {
		
		Assert.assertEquals(0, messageDAO.count(TOPIC_NAME, getConsumerId()));
		
		int count1 = 100, count2 = 150;
		
		insertMessage(count1, TOPIC_NAME, getConsumerId());
		
		Assert.assertEquals(count1, messageDAO.count(TOPIC_NAME, getConsumerId()));
		
		Long currentMaxId = messageDAO.getMaxMessageId(TOPIC_NAME, getConsumerId());
		
		insertMessage(count2, TOPIC_NAME, getConsumerId());
		
		Assert.assertEquals(count2 + count1, messageDAO.count(TOPIC_NAME, getConsumerId()));
		
		int del = messageDAO.deleteMessage(TOPIC_NAME, getConsumerId(), currentMaxId + 1);
		
		Assert.assertEquals(count1, del);
		
		Assert.assertEquals(count2, messageDAO.count(TOPIC_NAME, getConsumerId()));
		
	}


	protected abstract String getConsumerId();

	protected void insertMessage(int count, String topicName) {
		insertMessage(count, topicName, null);
	}

	protected void insertMessage(int count, String topicName, String consumerId) {

		for (int i = 0; i < count; i++) {

			messageDAO.saveMessage(topicName, consumerId, createBackupMessage());
		}
	}

	public static SwallowMessage createMessage() {

		SwallowMessage message = new SwallowMessage();
		message.setContent("this is a SwallowMessage");
		message.setGeneratedTime(new Date());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("property-key", "property-value");
		message.setProperties(map);
		message.setSha1("sha-1 string");
		message.setVersion("0.6.0");
		message.setType("feed");
		message.setSourceIp("localhost");
		return message;

	}

	private AtomicInteger inc = new AtomicInteger();

	protected SwallowMessage createBackupMessage() {
		SwallowMessage message = createMessage();
		message.setMessageId(MongoUtils.BSONTimestampToLong(new BSONTimestamp(
				(int) (System.currentTimeMillis() / 1000), inc.incrementAndGet())));
		return message;

	}

}