package com.dianping.swallow.common.internal.pool;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultThreadExceptionHandler{
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultThreadExceptionHandler.class);
	
	
	static{
		setExceptionCaughtHandler();
	}

	private static void setExceptionCaughtHandler(){
		
		if(logger.isInfoEnabled()){
			logger.info("[setExceptionCaughtHandler]");
		}

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				
				e.printStackTrace();
				logger.error("uncaught exception in thread:" + t, e);
				
			}
		});
		
	}

}
