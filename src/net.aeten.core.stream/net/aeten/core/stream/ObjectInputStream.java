package net.aeten.core.stream;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * @author Thomas Pérennou
 * 
 */
public abstract class ObjectInputStream extends
		java.io.ObjectInputStream {

	private static final String READ_OBJECT_METHOD_NAME = "readObject";
	private final DataInputStream in;

	public ObjectInputStream (InputStream in)
			throws IOException {
		this.in = new DataInputStream (in);
	}

	public ObjectInputStream (DataInputStream in)
			throws IOException {
		this.in = in;
	}

	/**
	 * @see java.io.ObjectInputStream#readObjectOverride()
	 */
	@Override
	protected Object readObjectOverride ()	throws IOException,
														ClassNotFoundException {
		try {
			Class <?> clazz = this.readClass ();
			if (clazz == null) {
				throw new ClassNotFoundException ();
			}
			final Object objSerialized = clazz.newInstance ();
			Method readMethod = getReadMethod (objSerialized.getClass ());
			AccessibleObject.setAccessible (new Method[] {
				readMethod
			}, true);
			readMethod.invoke (objSerialized, this);

			return objSerialized;
		} catch (ClassNotFoundException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new ClassNotFoundException (exception.getMessage (), exception);
		}
	}

	private static Method getReadMethod (Class <?> clazz) throws NoSuchMethodException {
		if (clazz == null) throw new NoSuchMethodException ();
		try {
			return clazz.getDeclaredMethod (READ_OBJECT_METHOD_NAME, java.io.ObjectInputStream.class);
		} catch (NoSuchMethodException exception) {
			return getReadMethod (clazz.getSuperclass ());
		}
	}

	protected Class <?> readClass () throws IOException {
		return null;
	};

	/**
	 * @see java.io.ObjectInputStream#close()
	 */
	@Override
	public void close () throws IOException {
		in.close ();
	}

	/**
	 * Reads in a boolean.
	 * 
	 * @return the boolean read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public boolean readBoolean () throws IOException {
		return in.readBoolean ();
	}

	/**
	 * Reads an 8 bit byte.
	 * 
	 * @return the 8 bit byte read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public byte readByte () throws IOException {
		return in.readByte ();
	}

	/**
	 * Reads an unsigned 8 bit byte.
	 * 
	 * @return the 8 bit byte read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public int readUnsignedByte () throws IOException {
		return in.readUnsignedByte ();
	}

	/**
	 * Reads a 16 bit char.
	 * 
	 * @return the 16 bit char read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public char readChar () throws IOException {
		return in.readChar ();
	}

	/**
	 * Reads a 16 bit short.
	 * 
	 * @return the 16 bit short read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public short readShort () throws IOException {
		return in.readShort ();
	}

	/**
	 * Reads an unsigned 16 bit short.
	 * 
	 * @return the 16 bit short read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public int readUnsignedShort () throws IOException {
		return in.readUnsignedShort ();
	}

	/**
	 * Reads a 32 bit int.
	 * 
	 * @return the 32 bit integer read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public int readInt () throws IOException {
		return in.readInt ();
	}

	/**
	 * Reads a 64 bit long.
	 * 
	 * @return the read 64 bit long.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public long readLong () throws IOException {
		return in.readLong ();
	}

	/**
	 * Reads a 32 bit float.
	 * 
	 * @return the 32 bit float read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public float readFloat () throws IOException {
		return in.readFloat ();
	}

	/**
	 * Reads a 64 bit double.
	 * 
	 * @return the 64 bit double read.
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public double readDouble () throws IOException {
		return in.readDouble ();
	}

	/**
	 * Reads bytes, blocking until all bytes are read.
	 * 
	 * @param buf
	 *            the buffer into which the data is read
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public void readFully (byte[] buf) throws IOException {
		in.readFully (buf, 0, buf.length);
	}

	/**
	 * Reads bytes, blocking until all bytes are read.
	 * 
	 * @param buf
	 *            the buffer into which the data is read
	 * @param off
	 *            the start offset of the data
	 * @param len
	 *            the maximum number of bytes to read
	 * @throws EOFException
	 *             If end of file is reached.
	 * @throws IOException
	 *             If other I/O error has occurred.
	 */
	@Override
	public void readFully (	byte[] buf,
									int off,
									int len) throws IOException {
		int endoff = off + len;
		if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
			throw new IndexOutOfBoundsException ();
		}
		in.readFully (buf, off, len);
	}

	/**
	 * @see java.io.ObjectInputStream#readUTF()
	 */
	@Override
	public String readUTF () throws IOException {
		String s = in.readUTF ();
		return s;
	}

	/**
	 * @see java.io.ObjectInputStream#read()
	 */
	@Override
	public int read () throws IOException {
		return in.read ();
	}

	/**
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read (byte[] b) throws IOException {
		return in.read (b);
	}

	/**
	 * @see java.io.ObjectInputStream#read(byte[], int, int)
	 */
	@Override
	public int read (	byte[] buf,
							int off,
							int len) throws IOException {
		return in.read (buf, off, len);
	}

	/**
	 * @see java.io.ObjectInputStream#reset()
	 */
	@Override
	public synchronized void reset () throws IOException {
		this.in.reset ();
	}

	/**
	 * @see java.io.ObjectInputStream#mark(int)
	 */
	@Override
	public synchronized void mark (int readlimit) {
		this.in.mark (readlimit);
	}

	/**
	 * @see java.io.ObjectInputStream#markSupported()
	 */
	@Override
	public boolean markSupported () {
		return this.in.markSupported ();
	}
}
