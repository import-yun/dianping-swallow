package com.dianping.swallow.web.monitor.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dianping.swallow.web.common.Pair;
import com.dianping.swallow.web.dao.ConsumerIdResourceDao.ConsumerIdParam;
import com.dianping.swallow.web.dashboard.wrapper.ConsumerDataRetrieverWrapper;
import com.dianping.swallow.web.model.resource.ConsumerIdResource;
import com.dianping.swallow.web.model.resource.TopicResource;
import com.dianping.swallow.web.monitor.wapper.ProducerStatsDataWapper;
import com.dianping.swallow.web.service.ConsumerIdResourceService;
import com.dianping.swallow.web.service.TopicResourceService;

/**
 * @author mingdongli
 *
 *         2015年8月31日下午8:14:56
 */
@Component
public class ConsumerIdResourceCollector extends AbstractResourceCollector {

	@Resource(name = "consumerIdResourceService")
	private ConsumerIdResourceService consumerIdResourceService;

	@Resource(name = "topicResourceService")
	private TopicResourceService topicResourceService;

	@Resource(name = "producerStatsDataWapper")
	private ProducerStatsDataWapper producerStatsDataWapper;

	@Autowired
	ConsumerDataRetrieverWrapper consumerDataRetrieverWrapper;

	@Override
	protected void doInitialize() throws Exception {
		super.doInitialize();
		collectorName = getClass().getSimpleName();
		collectorInterval = 10;
		collectorDelay = 2;
	}

	@Override
	public void doCollector() {
		logger.info("[startCollectConsumerIdResource]");
		flushConsumerIdMetaData();
		flushTopicMetaData();
	}

	private void flushConsumerIdMetaData() {

		Set<String> topics = consumerDataRetrieverWrapper.getKeyWithoutTotal(ConsumerDataRetrieverWrapper.TOTAL);
		for (String topic : topics) {
			Set<String> consumerids = consumerDataRetrieverWrapper.getKeyWithoutTotal(
					ConsumerDataRetrieverWrapper.TOTAL, topic);
			for (String cid : consumerids) {
				Set<String> ips = consumerDataRetrieverWrapper.getKeyWithoutTotal(ConsumerDataRetrieverWrapper.TOTAL,
						topic, cid);
				if (valid(topic, cid)) {
					ConsumerIdParam consumerIdParam = new ConsumerIdParam();
					consumerIdParam.setConsumerId(cid);
					consumerIdParam.setTopic(topic);
					Pair<Long, List<ConsumerIdResource>> pair = consumerIdResourceService.find(consumerIdParam);

					if (pair.getFirst() == 0) {
						ConsumerIdResource consumerIdResource = consumerIdResourceService.buildConsumerIdResource(
								topic, cid);
						if (ips != null && !ips.isEmpty()) {
							consumerIdResource.setConsumerIps(new ArrayList<String>(ips));
						}
						consumerIdResourceService.insert(consumerIdResource);
					} else {
						if (ips != null && !ips.isEmpty()) {
							ConsumerIdResource consumerIdResource = pair.getSecond().get(0);
							List<String> originalIps = consumerIdResource.getConsumerIps();
							if (ips.containsAll(originalIps) && originalIps.containsAll(ips)) {
								continue;
							} else {
								consumerIdResource.setConsumerIps(new ArrayList<String>(ips));
								consumerIdResourceService.insert(consumerIdResource);
							}
						}
					}
				}
			}
		}
	}

	private void flushTopicMetaData() {

		Set<String> topics = producerStatsDataWapper.getTopics(false);
		if (topics != null) {
			for (String topic : topics) {
				Set<String> ips = producerStatsDataWapper.getTopicIps(topic, false);
				if (ips != null) {
					if (ips.contains(ConsumerDataRetrieverWrapper.TOTAL)) {
						ips.remove(ConsumerDataRetrieverWrapper.TOTAL);
					}
					TopicResource topicResource = topicResourceService.findByTopic(topic);
					List<String> newList = new ArrayList<String>(ips);

					if (topicResource == null) {
						topicResource = topicResourceService.buildTopicResource(topic);
						topicResource.setProducerIps(newList);
						topicResourceService.insert(topicResource);
					} else {
						List<String> originalList = topicResource.getProducerIps();

						if (newList.containsAll(originalList) && originalList.containsAll(newList)) {
							continue;
						} else {
							topicResource.setProducerIps(newList);
							topicResourceService.insert(topicResource);
						}

					}

				}
			}
		}
	}

	private boolean valid(String topic, String consumerId) {

		if (StringUtils.isNotBlank(topic) && StringUtils.isNotBlank(consumerId)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getCollectorDelay() {
		return collectorDelay;
	}

	@Override
	public int getCollectorInterval() {
		return collectorInterval;
	}

}