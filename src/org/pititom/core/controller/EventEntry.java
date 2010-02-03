package org.pititom.core.controller;

/**
 *
 * @author Thomas Pérennou
 */
public class EventEntry<Source, Event extends Enum<?>, Data> {

	private final Source source;
	private final Event event;
	private final Data data;

	public EventEntry(Source source, Event event, Data data) {
		this.source = source;
		this.event = event;
		this.data = data;
	}

	/**
	 * @return the source
	 */
	public Source getSource() {
		return this.source;
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
		return "source={" + this.source + "}; event={" + this.event + "}; data={" + this.data + "}";
	}
}
