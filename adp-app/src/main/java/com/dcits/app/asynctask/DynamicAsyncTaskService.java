package com.dcits.app.asynctask;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 动态线程池
 * @author wuche
 * @version 暂时固定写死的，后期可以改造成基于spring建实例模式，或者基于懒加载模式创建
 */
public class DynamicAsyncTaskService {

	private static DynamicAsyncTaskService service;
	//最大线程数(可以放使用多个)
	public static int executorPoolSize =600;
	//最大线程数（可以放少的）
	public static int executorPoolSize2 =200;
	//线程前缀名
	public static String poolName ="one";
	//线程前缀名
	public static String poolName2 ="two";
	private  ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(executorPoolSize, setThreadFactory(poolName));
	private  ThreadPoolExecutor pool2 = (ThreadPoolExecutor) Executors.newFixedThreadPool(executorPoolSize2, setThreadFactory(poolName2));
	public static DynamicAsyncTaskService getInstance() {
		if (service == null) {
			service = new DynamicAsyncTaskService();
		}
		return service;
	}
    private ThreadFactory setThreadFactory(String poolName){
    	return new DefaultThreadFactory(poolName);
    }
    
    private ThreadPoolExecutor getPool(int i){
    	ThreadPoolExecutor newPool=null;
    	if(i==1){
    		newPool=pool;
    	}else{
    		newPool=pool2;
    	}
    	return newPool;
    }
    
    private int getPoolSize(int i){
		int poolSize=executorPoolSize;
		if(i==1){
			poolSize=executorPoolSize;
		}else{
			poolSize=executorPoolSize2;
		}
		return poolSize;
	}
    /**
     * 获取可以使用线程数
     * @param i
     * @return
     */
	public int getAvailableNum(int i) {
		return getPoolSize(i)-getPool(i).getActiveCount();
	}
	
	/**
	 * 获取活动线程数
	 * @param i
	 * @return
	 */
	public int getActiveNum(int i) {
		return getPool(i).getPoolSize();
	}
	/**
	 * 正在排队的线程数
	 * @return
	 */
	public int getQueueNum(int i) {
		return getPool(i).getQueue().size();
	}
	
	public void execute(Runnable command,int i) {
		getPool(i).execute(command);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Future submit(Callable task,int i) {
		return getPool(i).submit(task);
	}

	@SuppressWarnings("rawtypes")
	public Future submit(Runnable task, Object result,int i) {
		return getPool(i).submit(task, result);
	}
	
	/**
	 * 自定义的ThreadFactory，便于日志打印
	 * @author wuche
	 *
	 */
	static class DefaultThreadFactory implements ThreadFactory {  
	    private static final AtomicInteger poolNumber = new AtomicInteger(1);  
	    private final ThreadGroup group;  
	    private final AtomicInteger threadNumber = new AtomicInteger(1);  
	    private String namePrefix;  
	  
	    DefaultThreadFactory(String poolName) {  
	        SecurityManager s = System.getSecurityManager();  
	        group = (s != null) ? s.getThreadGroup() :  Thread.currentThread().getThreadGroup();  
	        namePrefix = "pool"+poolName +"-"+  poolNumber.getAndIncrement() +  "-thread-";  
	    }  
	    // 为线程池创建新的任务执行线程  
	    public Thread newThread(Runnable r) { 
	        // 线程对应的任务是Runnable对象r  
	        Thread t = new Thread(group, r,namePrefix + threadNumber.getAndIncrement(), 0);  
	        // 设为非守护线程  
	        if (t.isDaemon())  
	            t.setDaemon(false);  
	        // 将优先级设为Thread.NORM_PRIORITY  
	        if (t.getPriority() != Thread.NORM_PRIORITY)  
	            t.setPriority(Thread.NORM_PRIORITY);  
	        return t;  
	    }
		public String getNamePrefix() {
			return namePrefix;
		}
		public void setNamePrefix(String namePrefix) {
			this.namePrefix = namePrefix;
		}  
	    
	} 
}