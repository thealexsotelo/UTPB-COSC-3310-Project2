import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class Pointer
{
	private final String _name;
	private final byte[] _data;

	public Pointer(String name, long val) {
		_name = name;
		_data = longToBytes(val);
	}

	public Pointer(String name, String val) {
		_name = name;
		_data = val.getBytes();
	}

	public String getName() {
		return _name;
	}

	public String getString() {
		return new String(_data);
	}

	public String subString(int index) {
		return getString().substring(index);
	}

	public char charAt(int index) {
		return (char) _data[index];
	}

	public long getLong() {
		return bytesToLong(_data);
	}

	public int getInt() {
		return (int) bytesToLong(_data);
	}

	public static byte[] longToBytes(long val) {
		return new byte[]{
				(byte) ((val >> 56) & 0xff),
				(byte) ((val >> 48) & 0xff),
				(byte) ((val >> 40) & 0xff),
				(byte) ((val >> 32) & 0xff),
				(byte) ((val >> 24) & 0xff),
				(byte) ((val >> 16) & 0xff),
				(byte) ((val >> 8) & 0xff),
				(byte) ((val >> 0) & 0xff),
				};
	}

	public static long bytesToLong(byte[] dat) {
		ByteBuffer buffer = ByteBuffer.wrap(dat);
		return buffer.getLong();
	}
}
