package org.pititom.core.controller;

import java.util.concurrent.BlockingQueue;

import org.pititom.core.extersion.Notifier;

public class QueueNotifierController<Value> extends Thread {
	private final BlockingQueue<Value> queue;
	private final Notifier<Value> notifier;
	private boolean isKilled;

	public QueueNotifierController(BlockingQueue<Value> packetQueue, Notifier<Value> notifier) {
		this.queue = packetQueue;
		this.notifier = notifier;
	}
	
	public void run() {
		this.isKilled = false;
		this.queue.clear();
		try {
			while (!this.isKilled)
				this.notifier.notifyListener(this.queue.take());
		} catch (InterruptedException exception) {
        } finally {
			synchronized (queue) {
	            this.queue.clear();
            }
		}
	}
	
	public void kill() {
		this.isKilled = true;
		this.interrupt();
	}
}
