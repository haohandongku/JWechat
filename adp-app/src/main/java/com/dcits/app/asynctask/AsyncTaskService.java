package com.dcits.app.asynctask;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncTaskService {

	private static AsyncTaskService service;
	public static int executorPoolSize = 600;
	private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors
			.newFixedThreadPool(executorPoolSize);

	public static AsyncTaskService getInstance() {
		if (service == null) {
			service = new AsyncTaskService();
		}
		return service;
	}

	public void execute(Runnable command) {
		pool.execute(command);
	}
	public int getAvailableNum() {
		return executorPoolSize-pool.getActiveCount();
	}
	
	public int getActiveNum() {
		return pool.getActiveCount();
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Future submit(Callable task) {
		return pool.submit(task);
	}

	@SuppressWarnings("rawtypes")
	public Future submit(Runnable task, Object result) {
		return pool.submit(task, result);
	}

}