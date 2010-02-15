package org.pititom.core.event.signal;

import java.util.Collection;

import org.pititom.core.event.Default;
import org.pititom.core.event.Transmitter;
import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.event.signal.service.Slot;

public final class Signal<Data> {
	private static final RegisterableTransmitter<Object, Object, Object> ASYNC_TRANSMITTER = TransmitterFactory.asynchronous("Event loop");
	private static final RegisterableTransmitter<Object, Object, Object> SYNC_TRANSMITTER = TransmitterFactory.synchronous();

	private final Object source;
	private final Object event;
	private final Transmitter<Object, Object, Object> asyncTransmitter;

	public Signal(Object source, Object event) {
		this.source = source;
		this.event = event;
		this.asyncTransmitter = ASYNC_TRANSMITTER;
		
		synchronized (SlotRegister.class) {
			Collection<Slot<Object, Object, Object>> registredSlots = SlotRegister.SLOTS_MAP.get(this.event);
			if (registredSlots != null) {
				for (Slot<Object, Object, Object> slot : registredSlots) {
					ASYNC_TRANSMITTER.addEventHandler(slot, this.event);
					SYNC_TRANSMITTER.addEventHandler(slot, this.event);
				}
			}
		}
	}
	
	public Signal(Object event) {
		this(Default.ANONYMOUS_SOURCE, event);
	}
	
	@SuppressWarnings("unchecked")
	public Signal(Object source, Object event, Handler<?, ?, Data> handler, boolean ownThread) {
		this.source = source;
		this.event = event;
		if (ownThread) {
			this.asyncTransmitter = TransmitterFactory.asynchronous("Event" + event + " loop", (Handler<Object, Object, Object>)handler, this.event);
		} else {
			this.asyncTransmitter = ASYNC_TRANSMITTER;
			ASYNC_TRANSMITTER.addEventHandler((Handler<Object, Object, Object>) handler, this.event);
		}
		SYNC_TRANSMITTER.addEventHandler((Handler<Object, Object, Object>) handler, this.event);
	}

	public Signal(Object source, Object event, Handler<?, ?, Data> handler) {
		this(source, event, handler, false);
	}

	public Signal(Object source, Handler<?, Default, Data> handler, boolean ownThread) {
		this(source, Default.SINGLE_EVENT, handler, ownThread);
	}
	
	public Signal(Handler<Default, Default, Data> handler, boolean ownThread) {
		this(Default.ANONYMOUS_SOURCE, Default.SINGLE_EVENT, handler, ownThread);
	}

	public Signal(Object source, Handler<?, Default, Data> handler) {
		this(source, Default.SINGLE_EVENT, handler, false);
	}
	
	public Signal(Handler<Default, Default, Data> handler) {
		this(Default.ANONYMOUS_SOURCE, Default.SINGLE_EVENT, handler, false);
	}

	@Override
	public void finalize() throws Throwable {
		// TODO : unregister connection when it is the last one…
		super.finalize();
	}
	/**
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @return the event
	 */
	public Object getEvent() {
		return event;
	}

	/**
	 * Emit signal asynchronously in event loop with null data 
	 */
	public void emit() {
		this.emit(null);
	}

	/**
	 * Emit signal asynchronously in event loop
	 * 
	 * @param data
	 */
	public void emit(Data data) {
		asyncTransmitter.transmit(this.source, this.event, data);
	}
	
	/**
	 * Emit signal synchronously with null data
	 * 
	 * @param data
	 */
	public void emitSync() {
		this.emitSync(null);
	}

	/**
	 * Emit signal synchronously
	 * 
	 * @param data
	 */
	public void emitSync(Data data) {
		SYNC_TRANSMITTER.transmit(this.source, this.event, data);
	}

}
