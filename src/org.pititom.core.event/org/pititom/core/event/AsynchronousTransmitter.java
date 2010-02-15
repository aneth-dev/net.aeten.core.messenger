package org.pititom.core.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousTransmitter<Source, Event, Data> implements Transmitter<Source, Event, Data>, Runnable {

	private final Transmitter<Source, Event, Data> transmitter;
	private final BlockingQueue<EventEntry<Source, Event, Data>> queue;
	private final Thread eventTread;
	private boolean isKilled;

	public AsynchronousTransmitter(String threadName, Transmitter<Source, Event, Data> transmitter) {
		this.transmitter = transmitter;
		this.eventTread = new Thread(this, threadName);
		this.queue = new LinkedBlockingQueue<EventEntry<Source, Event, Data>>();
		this.eventTread.start();
	}
	public AsynchronousTransmitter(String threadName, Handler<Source, Event, Data> eventHandler, Event... events) {
		this(threadName, new SynchronousTransmitter<Source, Event, Data>(eventHandler, events));
	}

	@Override
	public void run() {
		this.queue.clear();
		AsynchronousTransmitter.this.isKilled = false;
		try {
			EventEntry<Source, Event, Data> eventEntry;
			while (!AsynchronousTransmitter.this.isKilled) {
				eventEntry = AsynchronousTransmitter.this.queue.take();
				this.transmitter.transmit(eventEntry.getSource(), eventEntry.getEvent(), eventEntry.getData());
			}
		} catch (InterruptedException exception) {
		} finally {
			synchronized (queue) {
				AsynchronousTransmitter.this.queue.clear();
			}
		}
	}

	public void kill() {
		this.isKilled = true;
		this.eventTread.interrupt();
	}

	@Override
	public void transmit(Source source, Event event, Data data) {
		this.queue.add(new EventEntry<Source, Event, Data>(source, event, data));
	}

	static class EventEntry<Source, Event, Data> {

		private final Source source;
		private final Event event;
		private final Data data;

		public EventEntry(Source source, Event event, Data data) {
			this.source = source;
			this.event = event;
			this.data = data;
		}

		/**
		 * @return the event
		 */
		public Event getEvent() {
			return this.event;
		}

		/**
		 * @return the data
		 */
		public Data getData() {
			return this.data;
		}

		/**
		 * @return the source
		 */
		public Source getSource() {
			return source;
		}
	}
}
