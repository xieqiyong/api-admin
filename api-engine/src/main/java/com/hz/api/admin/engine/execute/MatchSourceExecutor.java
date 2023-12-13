package com.hz.api.admin.engine.execute;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * 匹配数据源线程处理
 */
@Slf4j
public class MatchSourceExecutor {

	private          ThreadGroup                     group;
	private          LinkedBlockingQueue<Runnable>[] queues;
	private volatile boolean                         isRun = true;

	public MatchSourceExecutor(int threadSize, int queueSize, final String name) {
		group = new ThreadGroup(name);
		queues = new LinkedBlockingQueue[threadSize];
		for (int i = 0; i < threadSize; i++) {
			queues[i] = new LinkedBlockingQueue<>(queueSize);
			final int index = i;
			new Thread(group, () -> {
				while (isRun) {
					Runnable runnable = null;
					try {
						runnable = queues[index].take();
						runnable.run();
					} catch (Throwable e) {
						log.error("执行任务 {} 出错:", runnable, e);
					}
				}
			}).start();
		}
	}

	public void execute(long hash, Runnable runnable, RejectedExecutionPolicy policy) {
		int index = Math.abs((int)(hash % queues.length));
		switch (policy) {
			case Abort:
				if (!queues[index].offer(runnable)) {
					throw new RejectedExecutionException("Task " + runnable + " rejected from Queued[size=" + queues[index].size() + ", remaining="
							+ queues[index].remainingCapacity() + "]");
				}
				break;
			case Block:
				try {
					queues[index].put(runnable);
				} catch (InterruptedException e) {
					log.error("提交任务 {} 线程中断", runnable);
				}
				break;
		}
	}

	public void stop() {
		this.isRun = false;
		group.interrupt();
	}

}
