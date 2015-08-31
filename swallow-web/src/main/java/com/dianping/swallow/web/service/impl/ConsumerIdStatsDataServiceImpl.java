package com.dianping.swallow.web.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.swallow.web.dao.ConsumerIdStatsDataDao;
import com.dianping.swallow.web.model.stats.ConsumerIdStatsData;
import com.dianping.swallow.web.service.ConsumerIdStatsDataService;
import com.dianping.swallow.web.service.ConsumerServerStatsDataService.StatsDataMapPair;

/**
 * 
 * @author qiyin
 *
 *         2015年8月3日 下午3:17:35
 */
@Service("consumerIdStatsDataService")
public class ConsumerIdStatsDataServiceImpl implements ConsumerIdStatsDataService {

	@Autowired
	private ConsumerIdStatsDataDao consumerIdStatsDataDao;

	@Override
	public boolean insert(ConsumerIdStatsData consumerIdstatsData) {
		return consumerIdStatsDataDao.insert(consumerIdstatsData);
	}

	@Override
	public boolean update(ConsumerIdStatsData consumerIdstatsData) {
		return consumerIdStatsDataDao.update(consumerIdstatsData);
	}

	@Override
	public int deleteById(String id) {
		return consumerIdStatsDataDao.deleteById(id);
	}

	@Override
	public ConsumerIdStatsData findById(String id) {
		return consumerIdStatsDataDao.findById(id);
	}

	@Override
	public List<ConsumerIdStatsData> findByTimeKey(long timeKey) {
		return consumerIdStatsDataDao.findByTimeKey(timeKey);
	}

	@Override
	public List<ConsumerIdStatsData> findByTopic(String topicName) {
		return consumerIdStatsDataDao.findByTopic(topicName);
	}

	@Override
	public List<ConsumerIdStatsData> findByTopicAndTime(String topicName, long timeKey) {
		return consumerIdStatsDataDao.findByTopicAndTime(topicName, timeKey);
	}

	@Override
	public List<ConsumerIdStatsData> findByTopicAndConsumerId(String topicName, String consumerId) {
		return consumerIdStatsDataDao.findByTopicAndConsumerId(topicName, consumerId);
	}

	@Override
	public List<ConsumerIdStatsData> findByTopicAndConsumerId(String topicName, String consumerId, int offset, int limit) {
		return consumerIdStatsDataDao.findByTopicAndConsumerId(topicName, consumerId, offset, limit);
	}

	@Override
	public List<ConsumerIdStatsData> findByTopicAndTimeAndConsumerId(String topicName, long timeKey, String consumerId) {
		return consumerIdStatsDataDao.findByTopicAndTimeAndConsumerId(topicName, timeKey, consumerId);
	}

	@Override
	public List<ConsumerIdStatsData> findSectionData(String topicName, String consumerId, long startKey, long endKey) {
		return consumerIdStatsDataDao.findSectionData(topicName, consumerId, startKey, endKey);
	}

	@Override
	public List<ConsumerIdStatsData> findSectionData(String topicName, long startKey, long endKey) {
		return consumerIdStatsDataDao.findSectionData(topicName, startKey, endKey);
	}

	@Override
	public Map<String, StatsDataMapPair> findSectionQpsData(String topicName, long startKey, long endKey) {
		List<ConsumerIdStatsData> consumerIdStatsDatas = consumerIdStatsDataDao.findSectionData(topicName, startKey,
				endKey);
		Map<String, StatsDataMapPair> consumerIdStatsDataMaps = null;
		if (consumerIdStatsDatas != null && !consumerIdStatsDatas.isEmpty()) {
			consumerIdStatsDataMaps = new HashMap<String, StatsDataMapPair>();
			for (ConsumerIdStatsData consumerIdStatsData : consumerIdStatsDatas) {
				if (consumerIdStatsDataMaps.containsKey(consumerIdStatsData.getConsumerId())) {
					StatsDataMapPair statsDataResult = consumerIdStatsDataMaps.get(consumerIdStatsData.getConsumerId());
					statsDataResult.getSendStatsData().put(consumerIdStatsData.getTimeKey(),
							consumerIdStatsData.getSendQps());
					statsDataResult.getAckStatsData().put(consumerIdStatsData.getTimeKey(),
							consumerIdStatsData.getAckQps());
					consumerIdStatsDataMaps.put(consumerIdStatsData.getConsumerId(), statsDataResult);
				} else {
					StatsDataMapPair statsDataResult = new StatsDataMapPair();
					NavigableMap<Long, Long> sendStatsData = new TreeMap<Long, Long>();
					sendStatsData.put(consumerIdStatsData.getTimeKey(), consumerIdStatsData.getSendQps());
					NavigableMap<Long, Long> ackStatsData = new TreeMap<Long, Long>();
					sendStatsData.put(consumerIdStatsData.getTimeKey(), consumerIdStatsData.getAckQps());
					statsDataResult.setSendStatsData(sendStatsData);
					statsDataResult.setAckStatsData(ackStatsData);
					consumerIdStatsDataMaps.put(consumerIdStatsData.getConsumerId(), statsDataResult);
				}
			}
		}
		return consumerIdStatsDataMaps;
	}

	@Override
	public Map<String, StatsDataMapPair> findSectionDelayData(String topicName, long startKey, long endKey) {
		List<ConsumerIdStatsData> consumerIdStatsDatas = consumerIdStatsDataDao.findSectionData(topicName, startKey,
				endKey);
		Map<String, StatsDataMapPair> consumerIdStatsDataMaps = null;
		if (consumerIdStatsDatas != null && !consumerIdStatsDatas.isEmpty()) {
			consumerIdStatsDataMaps = new HashMap<String, StatsDataMapPair>();
			for (ConsumerIdStatsData consumerIdStatsData : consumerIdStatsDatas) {
				if (consumerIdStatsDataMaps.containsKey(consumerIdStatsData.getConsumerId())) {
					StatsDataMapPair statsDataResult = consumerIdStatsDataMaps.get(consumerIdStatsData.getConsumerId());
					statsDataResult.getSendStatsData().put(consumerIdStatsData.getTimeKey(),
							consumerIdStatsData.getSendDelay());
					statsDataResult.getAckStatsData().put(consumerIdStatsData.getTimeKey(),
							consumerIdStatsData.getAckDelay());
					consumerIdStatsDataMaps.put(consumerIdStatsData.getConsumerId(), statsDataResult);
				} else {
					StatsDataMapPair statsDataResult = new StatsDataMapPair();
					NavigableMap<Long, Long> sendStatsData = new TreeMap<Long, Long>();
					sendStatsData.put(consumerIdStatsData.getTimeKey(), consumerIdStatsData.getSendDelay());
					NavigableMap<Long, Long> ackStatsData = new TreeMap<Long, Long>();
					sendStatsData.put(consumerIdStatsData.getTimeKey(), consumerIdStatsData.getAckDelay());
					statsDataResult.setSendStatsData(sendStatsData);
					statsDataResult.setAckStatsData(ackStatsData);
					consumerIdStatsDataMaps.put(consumerIdStatsData.getConsumerId(), statsDataResult);
				}
			}
		}
		return consumerIdStatsDataMaps;
	}

	@Override
	public Map<String, NavigableMap<Long, Long>> findSectionAccuData(String topicName, long startKey, long endKey) {
		List<ConsumerIdStatsData> consumerIdStatsDatas = consumerIdStatsDataDao.findSectionData(topicName, startKey,
				endKey);
		Map<String, NavigableMap<Long, Long>> consumerIdStatsDataMaps = null;
		if (consumerIdStatsDatas != null && !consumerIdStatsDatas.isEmpty()) {
			consumerIdStatsDataMaps = new HashMap<String, NavigableMap<Long, Long>>();
			for (ConsumerIdStatsData consumerIdStatsData : consumerIdStatsDatas) {
				if (consumerIdStatsDataMaps.containsKey(consumerIdStatsData.getConsumerId())) {
					NavigableMap<Long, Long> accuStatsDatas = consumerIdStatsDataMaps.get(consumerIdStatsData
							.getConsumerId());
					accuStatsDatas.put(consumerIdStatsData.getTimeKey(), consumerIdStatsData.getAccumulation());

					consumerIdStatsDataMaps.put(consumerIdStatsData.getConsumerId(), accuStatsDatas);
				} else {
					NavigableMap<Long, Long> accuStatsDatas = new TreeMap<Long, Long>();
					accuStatsDatas.put(consumerIdStatsData.getTimeKey(), consumerIdStatsData.getAccumulation());
					consumerIdStatsDataMaps.put(consumerIdStatsData.getConsumerId(), accuStatsDatas);
				}
			}
		}
		return consumerIdStatsDataMaps;
	}

}
