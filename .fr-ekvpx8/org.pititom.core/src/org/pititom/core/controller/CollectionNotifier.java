package org.pititom.core.controller;

import java.util.Collection;

import org.pititom.core.extersion.Notifier;

public class CollectionNotifier<Value> implements Notifier<Value> {
	private final Collection<Notifier<Value>> notifierCollection;

	CollectionNotifier(Collection<Notifier<Value>> notifierCollection) {
		this.notifierCollection = notifierCollection;
	}

	@Override
	public void notifyListener(Value value) {
		synchronized (this.notifierCollection) {
			for (Notifier<Value> notifier : this.notifierCollection) {
				notifier.notifyListener(value);
			}
		}
	}

}
