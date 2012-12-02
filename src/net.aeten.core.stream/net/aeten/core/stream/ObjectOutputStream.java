package net.aeten.core.stream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * @author Thomas Pérennou
 * 
 */
public abstract class ObjectOutputStream extends
		java.io.ObjectOutputStream {

	private static final String WRITE_OBJECT_METHOD_NAME = "writeObject";
	private final DataOutputStream out;

	public ObjectOutputStream (DataOutputStream out)
			throws IOException {
		this.out = out;
	}

	public ObjectOutputStream (OutputStream out)
			throws IOException {
		this.out = new DataOutputStream (out);
	}

	@Override
	protected void writeObjectOverride (Object object) throws IOException {
		try {
			this.writeClass (object.getClass ());
			Method writeMethod = getWtriteMethod (object.getClass ());
			AccessibleObject.setAccessible (new Method[] {
				writeMethod
			}, true);
			writeMethod.invoke (object, this);
		} catch (Exception exception) {
			throw new IOException (exception);
		}
	}

	private static Method getWtriteMethod (Class <?> clazz) throws NoSuchMethodException {
		if (clazz == null) throw new NoSuchMethodException ();
		try {
			return clazz.getDeclaredMethod (WRITE_OBJECT_METHOD_NAME, java.io.ObjectOutputStream.class);
		} catch (NoSuchMethodException exception) {
			return getWtriteMethod (clazz.getSuperclass ());
		}
	}

	protected abstract void writeClass (Class <?> clazz) throws IOException;

	/**
	 * @throws IOException
	 * @see java.io.FilterOutputStream#close()
	 */
	@Override
	public void close () throws IOException {
		out.close ();
	}

	/**
	 * @throws IOException
	 * @see java.io.DataOutputStream#flush()
	 */
	@Override
	public void flush () throws IOException {
		out.flush ();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString () {
		return out.toString ();
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @throws IOException
	 * @see java.io.DataOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (	byte[] b,
								int off,
								int len) throws IOException {
		out.write (b, off, len);
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	@Override
	public void write (byte[] b) throws IOException {
		out.write (b);
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.DataOutputStream#write(int)
	 */
	@Override
	public void write (int b) throws IOException {
		out.write (b);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeBoolean(boolean)
	 */
	@Override
	public final void writeBoolean (boolean v) throws IOException {
		out.writeBoolean (v);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeByte(int)
	 */
	@Override
	public final void writeByte (int v) throws IOException {
		out.writeByte (v);
	}

	/**
	 * @param s
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeBytes(java.lang.String)
	 */
	@Override
	public final void writeBytes (String s) throws IOException {
		out.writeBytes (s);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeChar(int)
	 */
	@Override
	public final void writeChar (int v) throws IOException {
		out.writeChar (v);
	}

	/**
	 * @param s
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeChars(java.lang.String)
	 */
	@Override
	public final void writeChars (String s) throws IOException {
		out.writeChars (s);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeDouble(double)
	 */
	public final void writeDouble (double v) throws IOException {
		out.writeDouble (v);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeFloat(float)
	 */
	@Override
	public void writeFloat (float v) throws IOException {
		out.writeFloat (v);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeInt(int)
	 */
	@Override
	public void writeInt (int v) throws IOException {
		out.writeInt (v);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeLong(long)
	 */
	@Override
	public void writeLong (long v) throws IOException {
		out.writeLong (v);
	}

	/**
	 * @param v
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeShort(int)
	 */
	@Override
	public void writeShort (int v) throws IOException {
		out.writeShort (v);
	}

	/**
	 * @param str
	 * @throws IOException
	 * @see java.io.DataOutputStream#writeUTF(java.lang.String)
	 */
	@Override
	public final void writeUTF (String str) throws IOException {
		out.writeUTF (str);
	}
}
