package org.pititom.core.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class AsynchronousEventTransmitter<Source, Event extends Enum<?>, Data> implements EventTransmitter<Event, Data>, Runnable {

	private final EventTransmitter<Event, Data> eventTransmitter;
	private final BlockingQueue<EventEntry<Event, Data>> queue;
	private final Thread eventTread;
	private boolean isKilled;

	public AsynchronousEventTransmitter(String threadName, EventTransmitter<Event, Data> eventTransmitter) {
		this.eventTransmitter = eventTransmitter;
		this.eventTread = new Thread(this, threadName);
		this.queue = new LinkedBlockingQueue<EventEntry<Event, Data>>();
		this.eventTread.start();
	}

	@Override
	public void run() {
		this.queue.clear();
		AsynchronousEventTransmitter.this.isKilled = false;
		try {
			EventEntry<Event, Data> eventEntry;
			while (!AsynchronousEventTransmitter.this.isKilled) {
				eventEntry = AsynchronousEventTransmitter.this.queue.take();
				this.eventTransmitter.transmit(eventEntry.getEvent(), eventEntry.getData());
			}
		} catch (InterruptedException exception) {
		} finally {
			synchronized (queue) {
				AsynchronousEventTransmitter.this.queue.clear();
			}
		}
	}

	public void kill() {
		this.isKilled = true;
		this.eventTread.interrupt();
	}

	@Override
	public void transmit(Event event, Data data) {
		this.queue.add(new EventEntry<Event, Data>(event, data));
	}

	class EventEntry<Event extends Enum<?>, Data> {

		private final Event event;
		private final Data data;

		public EventEntry(Event event, Data data) {
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

		@Override
		public String toString() {
			return "event={" + this.event + "}; data={" + this.data + "}";
		}
	}
}
