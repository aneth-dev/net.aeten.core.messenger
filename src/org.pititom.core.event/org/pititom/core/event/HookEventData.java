package org.pititom.core.event;

public class HookEventData<Source, Event extends Enum<?>> extends
		EventData<Source, HookEvent<Event>> {
	private boolean doIt;

	public HookEventData(Source source, HookEvent<Event> event,
			Priority priority) {
		this(source, event, priority, true);
	}

	public HookEventData(Source source, HookEvent<Event> event,
			Priority priority, boolean doIt) {
		super(source, event, priority);
		this.doIt = doIt;
	}

	public HookEventData(Source source, HookEvent<Event> event) {
		this(source, event, Priority.MEDIUM);
	}

	public boolean doIt() {
		return this.doIt;
	}

	public void setDoIt(boolean doIt) {
		this.doIt = doIt;
	}

	public void setEvent(HookEvent<Event> event) {
		this.event = event;
	}
}
