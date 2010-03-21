package org.pititom.core.event;


public class HookEvent<Event extends Enum<?>> {
	public static enum Hook {
		PRE, POST
	}

	private final Event sourceEvent;
	private final Hook hook;

	private HookEvent(Event event, Hook hook) {
		super();
		this.sourceEvent = event;
		this.hook = hook;
	}

	public Event getSourceEvent() {
		return this.sourceEvent;
	}

	public Hook getHook() {
		return this.hook;
	}
	
	public static <Event extends Enum<?>> HookEvent<Event> get(Event event, Hook hook) {
		return new HookEvent<Event>(event, hook);
	}
	
  public static <Event extends Enum<?>> HookEvent<Event> get(Event event) {
     return new HookEvent<Event>(event, null);
   }

	@Override
	public String toString() {
		return ((this.hook == null) ? "" : this.hook + " ") + this.sourceEvent.toString();
	}
}
