package com.dianping.swallow.common.internal.util.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 封装task，持续运行，直到明确停止
 * @author mengwenchao
 *
 * 2015年2月13日 上午11:05:46
 */
public abstract class AbstractEternalTask implements Runnable{
	
	protected Logger logger = LogManager.getLogger(getClass());
	
	@Override
	public void run(){
		
		while(!stop()){
			try{
				doRun();
				sleep();
			}catch(Throwable th){
				logger.error("[run]", th);
			}
		}
		
		doOnThreadExit();
		
		if(logger.isInfoEnabled()){
			logger.info("[run][exit]" + Thread.currentThread());
		}
	}

	protected void doOnThreadExit() {
		
	}

	protected  void sleep() {
		
	}

	protected abstract void doRun();

	protected abstract boolean stop();
	
}
