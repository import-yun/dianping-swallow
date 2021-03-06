package com.dianping.swallow.web.dao.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.dianping.swallow.web.dao.ConsumerTopicStatsDataDao;
import com.dianping.swallow.web.model.stats.ConsumerTopicStatsData;

/**
 * 
 * @author qiyin
 *
 *         2015年8月24日 上午11:41:16
 */
@Service("consumerTopicStatsDataDao")
public class DefaultConsumerTopicStatsDataDao extends AbstractStatsDao implements ConsumerTopicStatsDataDao {

	private static final String CONSUMERTOPICSTATSDATA_COLLECTION = "CONSUMER_TOPIC_STATS_DATA";

	private static final String TIMEKEY_FIELD = "timeKey";

	private static final String TOPICNAME_FIELD = "topicName";

	@Override
	public boolean insert(ConsumerTopicStatsData topicStatsData) {
		try {
			mongoTemplate.save(topicStatsData, CONSUMERTOPICSTATSDATA_COLLECTION);
			return true;
		} catch (Exception e) {
			logger.error("Error when save consumer topic statsdata." + topicStatsData, e);
		}
		return false;
	}

	@Override
	public boolean insert(List<ConsumerTopicStatsData> topicStatsDatas) {
		try {
			mongoTemplate.insert(topicStatsDatas, CONSUMERTOPICSTATSDATA_COLLECTION);
			return true;
		} catch (Exception e) {
			logger.error("Error when save consumer topic statsdata.", e);
		}
		return false;
	}

	public boolean removeLessThanTimeKey(long timeKey) {
		try {
			Query query = new Query(Criteria.where(TIMEKEY_FIELD).lt(timeKey));
			mongoTemplate.remove(query, CONSUMERTOPICSTATSDATA_COLLECTION);
			return true;
		} catch (Exception e) {
			logger.error("[removeLessThanTimeKey] remove less than timeKey error.", e);
		}
		return false;
	}

	@Override
	public List<ConsumerTopicStatsData> findSectionData(String topicName, long startKey, long endKey) {
		Query query = new Query(Criteria.where(TOPICNAME_FIELD).is(topicName).and(TIMEKEY_FIELD).gte(startKey)
				.lte(endKey)).with(new Sort(new Sort.Order(Direction.ASC, TIMEKEY_FIELD)));
		List<ConsumerTopicStatsData> statisDatas = mongoTemplate.find(query, ConsumerTopicStatsData.class,
				CONSUMERTOPICSTATSDATA_COLLECTION);
		return statisDatas;
	}

	@Override
	public ConsumerTopicStatsData findOldestData() {
		Query query = new Query();
		query.skip(0).limit(1).with(new Sort(new Sort.Order(Direction.ASC, TIMEKEY_FIELD)));
		ConsumerTopicStatsData statsData = mongoTemplate.findOne(query, ConsumerTopicStatsData.class,
				CONSUMERTOPICSTATSDATA_COLLECTION);
		return statsData;
	}

}
