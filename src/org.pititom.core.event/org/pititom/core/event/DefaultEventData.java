package org.pititom.core.event;

public class DefaultEventData<Data> extends EventData<Default, Default> {
	private final Data data;
	public DefaultEventData(Data data) {
	    super(Default.ANONYMOUS_SOURCE, Default.SINGLE_EVENT);
	    this.data = data;
    }
	public Data getData() {
	    return data;
    }
}
