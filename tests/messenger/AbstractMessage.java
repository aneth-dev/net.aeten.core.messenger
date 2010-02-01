import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {
	private static final long serialVersionUID = -1149769829841085667L;
	
	private Acknowledge acknowledge;

	public Acknowledge getAcknowledge() {
		return acknowledge;
	}

	public void setAcknowledge(Acknowledge acknowledge) {
		this.acknowledge = acknowledge;
	}

	protected void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		final int ordinal = in.readInt();
		this.acknowledge = ((ordinal >= 0) && (ordinal < Acknowledge.values().length)) ? Acknowledge.values()[ordinal] : null;
	}

	protected void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt((this.acknowledge == null) ? -1 : this.acknowledge.ordinal());
	}

	@Override
	public String toString() {
		return "acknowledge=" + this.acknowledge;
	}
}
