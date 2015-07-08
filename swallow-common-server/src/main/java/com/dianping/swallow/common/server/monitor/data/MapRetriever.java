package com.dianping.swallow.common.server.monitor.data;

import java.util.Set;

import com.dianping.swallow.common.server.monitor.data.statis.CasKeys;

/**
 * @author mengwenchao
 *
 * 2015年7月8日 下午6:09:14
 */
public interface MapRetriever {

	Set<String> getKeys(CasKeys keys, StatisType type);
	
	Object getValue(CasKeys keys, StatisType type);

	Set<String> getKeys(CasKeys keys);
	
	Object getValue(CasKeys keys);

}
