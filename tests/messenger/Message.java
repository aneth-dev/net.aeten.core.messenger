import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Message extends AbstractMessage {
	private static final long serialVersionUID = -1252541775206801257L;
	public static final int MIN_VALUE = 2;
	public static final int MAX_VALUE = 5;
	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	protected void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		final int size = in.readInt();
		if (size != 8) { // size
			throw new IOException("Invalid size. Recieved=" + size + " expected=" + 8);
		}
		super.readObject(in);
		this.value = in.readInt();
	}

	@Override
	protected void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(8); // size
		super.writeObject(out);
		out.writeInt(this.value);
	}

	@Override
	public String toString() {
		return super.toString() + "; value=" + this.value;
	}
}
