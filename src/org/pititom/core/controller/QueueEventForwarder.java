package org.pititom.core.controller;

import java.util.concurrent.BlockingQueue;

import org.pititom.core.extersion.EventHandler;

public class QueueEventForwarder<Source, Event extends Enum<?>, Data> extends Thread {

    private final BlockingQueue<EventEntry<Source, Event, Data>> queue;
    private final EventHandler<Source, Event, Data> eventForwarder;
    private boolean isKilled;


    public QueueEventForwarder(String threadName, BlockingQueue<EventEntry<Source, Event, Data>> queue, EventHandler<Source, Event, Data> eventForwarder) {
        super(threadName);
        this.queue = queue;
        this.eventForwarder = eventForwarder;
    }

    public void run() {
        this.isKilled = false;
        this.queue.clear();
        try {
        	EventEntry<Source, Event, Data> eventEntry;
            while (!this.isKilled) {
            	eventEntry = this.queue.take();
                this.eventForwarder.handleEvent(eventEntry.getSource(), eventEntry.getEvent(), eventEntry.getData());
            }
        } catch (InterruptedException exception) {
        } finally {
            synchronized (queue) {
                this.queue.clear();
            }
        }
    }

    public void kill() {
        this.isKilled = true;
        this.interrupt();
    }
}
