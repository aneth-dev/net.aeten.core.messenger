package org.pititom.core.controller;

import java.util.ArrayList;
import java.util.Collection;

import org.pititom.core.extersion.Notifier;

public class CollectionNotifier<Value> implements Notifier<Value> {
	private final Collection<Notifier<Value>> notifierCollection;

	public CollectionNotifier(Collection<Notifier<Value>> notifierCollection) {
		this.notifierCollection = notifierCollection;
	}

	public CollectionNotifier(Notifier<Value>... notifierList) {
		this.notifierCollection = new ArrayList<Notifier<Value>>(notifierList.length);
		for (Notifier<Value> notifier: notifierList) {
			this.notifierCollection.add(notifier);
		}
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
