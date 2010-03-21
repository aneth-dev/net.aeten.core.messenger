package org.pititom.core.event;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 * @author Thomas Pérennou
 */
class AsynchronousTransmitter<Source, Event, Data extends EventData<Source, Event>>
		implements Transmitter<Data>, Runnable {

	private final Transmitter<Data> transmitter;
	private final Queue<Data>[] queues;
	private final Object mutex;
	private final Thread eventTread;
	private boolean isKilled;

	public AsynchronousTransmitter(String threadName,
			Transmitter<Data> transmitter, int threadPriority) {
		this.transmitter = transmitter;
		this.eventTread = new Thread(this, threadName);
		this.eventTread.setPriority(threadPriority);
		this.mutex = new Object();
		this.queues = new LinkedBlockingQueue[Priority.values().length];
		for (Priority priority : Priority.values()) {
			this.queues[priority.ordinal()] = new LinkedBlockingQueue<Data>();
		}

		this.eventTread.start();
	}

	public AsynchronousTransmitter(String threadName,
			Transmitter<Data> transmitter) {
		this(threadName, transmitter, Thread.NORM_PRIORITY);
	}

	public AsynchronousTransmitter(String threadName, int threadPriority,
			Handler<Data> eventHandler, Event... events) {
		this(threadName, new SynchronousTransmitter<Event, Data>(eventHandler,
				events), threadPriority);
	}

	public AsynchronousTransmitter(String threadName,
			Handler<Data> eventHandler, Event... events) {
		this(threadName, new SynchronousTransmitter<Event, Data>(eventHandler,
				events), Thread.NORM_PRIORITY);
	}

	@Override
	public void run() {
		synchronized (this.mutex) {
			for (Priority priority : Priority.values()) {
				this.queues[priority.ordinal()].clear();
			}
		}
		this.isKilled = false;
		try {
			while (!this.isKilled) {
				synchronized (this.mutex) {
					while (!this.haveEventToTransmit()) {
						this.mutex.wait();
					}
				}
				for (Priority priority : Priority.values()) {
					this.transmit(this.queues[priority.ordinal()]);
				}
			}
		} catch (InterruptedException exception) {
		} finally {
			synchronized (this.mutex) {
				for (Priority priority : Priority.values()) {
					this.queues[priority.ordinal()].clear();
				}
			}
		}
	}

	private boolean haveEventToTransmit() {
		for (Priority priority : Priority.values()) {
			if (!AsynchronousTransmitter.this.queues[priority.ordinal()]
					.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public void kill() {
		this.isKilled = true;
		this.eventTread.interrupt();
	}

	private int transmit(Queue<Data> queue) {
		synchronized (queue) {
			int eventsToTransmitCount = queue.size();
			if (eventsToTransmitCount > 0) {
				for (int i = 0; i < eventsToTransmitCount; i++) {
					this.transmitter.transmit(queue.poll());
				}
			}
			return eventsToTransmitCount;
		}
	}

	@Override
	public void transmit(Data data) {
		Queue<Data> queue = this.queues[data.getPriority().ordinal()];
		synchronized (queue) {
			queue.add(data);
		}
		synchronized (this.mutex) {
			this.mutex.notify();
		}
	}

}
