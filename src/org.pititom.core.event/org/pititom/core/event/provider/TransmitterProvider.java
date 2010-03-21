package org.pititom.core.event.provider;

import org.kohsuke.args4j.Option;
import org.pititom.core.Configurable;
import org.pititom.core.ConfigurationException;
import org.pititom.core.args4j.CommandLineParserHelper;
import org.pititom.core.event.EventData;
import org.pititom.core.event.Handler;
import org.pititom.core.event.RegisterableTransmitter;
import org.pititom.core.event.TransmitterFactory;
import org.pititom.core.event.service.TransmitterService;

public class TransmitterProvider<Source, Event, Data extends EventData<Source, Event>> implements TransmitterService<Source, Event, Data>, Configurable {
	private RegisterableTransmitter<Source, Event, Data> delegatedTransmitter;
	
	@Option(name = "-id", aliases = "--identifier", required = true)
	private String identifier;
	@Option(name = "-d", aliases = "--description", required = false)
	private String description;
	
	/** @deprecated Reserved to configuration building */
	public TransmitterProvider() {
		this.identifier = null;
		this.description = "";
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
		CommandLineParserHelper.configure(this, configuration);
		this.delegatedTransmitter = TransmitterFactory.asynchronous(this.identifier);
	}
	
	@Override
	public String toString() {
		return "".equals(this.description) ? this.identifier : this.description;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
