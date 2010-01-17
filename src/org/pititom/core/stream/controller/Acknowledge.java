package org.pititom.core.stream.controller;

import java.util.LinkedList;
import java.util.List;

import org.pititom.core.controller.CollectionNotifier;
import org.pititom.core.extersion.Notifier;

public abstract class Acknowledge {
	private AcknowledgeUnit objectUnitToBeAcknowledged = null;
	private final static List<AcknowledgeUnit> objectToBeAcknowledgedQueue = new LinkedList<AcknowledgeUnit>();
	private final static Notifier<Object> notifier = new CollectionNotifier<Object>(
	        null);

	public abstract boolean isAcknowledge(Object emittedObject,
	        Object recievedObject);

	public void checkAcknowledge(Object packet_p) {
		synchronized (objectToBeAcknowledgedQueue) {
			if (objectToBeAcknowledgedQueue.size() > 0) {
				for (AcknowledgeUnit unit : objectToBeAcknowledgedQueue) {
					if (this.isAcknowledge(unit.getObjectToAcknowledge(),
					        packet_p)) {
						synchronized (unit) {
							unit.setAcknowledged(true);
							unit.notifyAll();
							break;
						}
					}
				}
			}
		}
	}

	public void waitAcknowledge(Object objectToBeAcknowledged, long timeOut)
	        throws InterruptedException {
		synchronized (this) {
			objectUnitToBeAcknowledged = new AcknowledgeUnit(
			        objectToBeAcknowledged);
			synchronized (objectUnitToBeAcknowledged) {
				objectToBeAcknowledgedQueue.add(objectUnitToBeAcknowledged);
			}

			synchronized (objectUnitToBeAcknowledged) {
				objectUnitToBeAcknowledged.wait(timeOut);
			}
			if (!objectUnitToBeAcknowledged.isAcknowledged()) {
				Object packet = objectUnitToBeAcknowledged.getRecievedObject();
				notifier.notifyListener(packet);
			}
			synchronized (objectToBeAcknowledgedQueue) {
				objectToBeAcknowledgedQueue.remove(objectUnitToBeAcknowledged);
			}
			objectUnitToBeAcknowledged = null;
		}
	}

	private class AcknowledgeUnit {
		private final Object _dataToAcknowledge;
		private Object _endPointData;
		private boolean _isAcknowledged;

		public AcknowledgeUnit(Object dataToAcknowledge_p) {
			_dataToAcknowledge = dataToAcknowledge_p;
			_isAcknowledged = false;
			_endPointData = null;
		}

		public Object getRecievedObject() {
			return _endPointData;
		}

		public void setRecievedObject(Object endPointData_p) {
			_endPointData = endPointData_p;
		}

		public boolean isAcknowledged() {
			return _isAcknowledged;
		}

		public void setAcknowledged(boolean isAcknowledged_p) {
			_isAcknowledged = isAcknowledged_p;
			if (_isAcknowledged) {
				synchronized (this) {
					this.notifyAll();
				}
			}
		}

		public Object getObjectToAcknowledge() {
			return _dataToAcknowledge;
		}
	}

}
