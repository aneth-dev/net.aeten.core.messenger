package org.pititom.core.event.provider;

import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.event.EventData;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.service.TransmitterService;

public class TransmitterProvider<Source, Event, Data extends EventData<Source, Event>> implements TransmitterService<Source, Event, Data>, Configurable {
	private RegisterableTransmitter<Source, Event, Data> delegatedTransmitter;
	
	private static final String IDENTIFIER_OPTION = "--identifier";
	private String identifier;
	
	/** @deprecated Reserved to configuration building */
	public TransmitterProvider() {
		this.identifier = null;
		this.delegatedTransmitter = null;
	}
	
	@Override
	public void transmit(Data data) {
		this.delegatedTransmitter.transmit(data);
	}

	@Override
	public void addEventHandler(Handler<Data> eventHandler,
			Event... eventList) {
		this.delegatedTransmitter.addEventHandler(eventHandler, eventList);
	}

	@Override
	public void removeEventHandler(Handler<Data> eventHandler,
			Event... eventList) {
		this.delegatedTransmitter.removeEventHandler(eventHandler, eventList);
	}

	@Override
	public void configure(String configuration) throws ConfigurationException {
		String[] arguments = configuration.split("\\s+");
		switch (arguments.length) {
			case 2:
				if (arguments[0].equals(IDENTIFIER_OPTION)) {
					this.identifier = arguments[1];
				} else {
					throw new ConfigurationException(configuration, "Invalid argument " + arguments[0] + " (" + IDENTIFIER_OPTION + " was expected)");
				}
				break;

			default:
				throw new ConfigurationException(configuration, "Configuration must be like: " + IDENTIFIER_OPTION + " anIdentifier");
		}
		this.delegatedTransmitter = TransmitterFactory.asynchronous(this.identifier);
	}
	
	@Override
	public String toString() {
		return this.identifier;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

}
