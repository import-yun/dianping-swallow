package com.dianping.swallow.web.service;

import java.util.List;

import com.dianping.swallow.web.model.statis.ConsumerIdStatsData;

/**
 *
 * @author qiyin
 *
 */
public interface ConsumerIdStatisDataService {

	public boolean insert(ConsumerIdStatsData statisData);

	public boolean update(ConsumerIdStatsData statisData);

	public int deleteById(String id);

	public ConsumerIdStatsData findById(String id);

	public List<ConsumerIdStatsData> findByTimeKey(long timeKey);

	public List<ConsumerIdStatsData> findByTopic(String topicName);

	public List<ConsumerIdStatsData> findByTopicAndTime(String topicName, long timeKey);

	public List<ConsumerIdStatsData> findByTopicAndConsumerId(String topicName, String consumerId);

	public List<ConsumerIdStatsData> findByTopicAndTimeAndConsumerId(String topicName, long timeKey, String consumerId);

	List<ConsumerIdStatsData> findSectionData(String topicName, String consumerId, long startKey, long endKey);
}