package org.pititom.core.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Thomas Pérennou
 */
class AsynchronousForwarder<Source, Event extends Enum<?>, Data> implements Forwarder<Source, Event, Data>, Runnable {

	private final Forwarder<Source, Event, Data> forwarder;
	private final BlockingQueue<EventEntry<Event, Data>> queue;
	private final Thread eventTread;
	private boolean isKilled;

	public AsynchronousForwarder(String threadName, Forwarder<Source, Event, Data> forwarder) {
		this.forwarder = forwarder;
		this.eventTread = new Thread(this, threadName);
		this.queue = new LinkedBlockingQueue<EventEntry<Event, Data>>();
		this.eventTread.start();
	}
	public AsynchronousForwarder(String threadName, Handler<Source, Event, Data> eventHandler, Event... events) {
		this(threadName, new SynchronousForwarder<Source, Event, Data>(eventHandler, events));
	}

	@Override
	public void run() {
		this.queue.clear();
		AsynchronousForwarder.this.isKilled = false;
		try {
			EventEntry<Event, Data> eventEntry;
			while (!AsynchronousForwarder.this.isKilled) {
				eventEntry = AsynchronousForwarder.this.queue.take();
				this.forwarder.forward(eventEntry.getSource(), eventEntry.getEvent(), eventEntry.getData());
			}
		} catch (InterruptedException exception) {
		} finally {
			synchronized (queue) {
				AsynchronousForwarder.this.queue.clear();
			}
		}
	}

	public void kill() {
		this.isKilled = true;
		this.eventTread.interrupt();
	}

	@Override
	public void forward(Source source, Event event, Data data) {
		this.queue.add(new EventEntry<Event, Data>(source, event, data));
	}

	class EventEntry<Event extends Enum<?>, Data> {

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
