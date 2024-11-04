import java.nio.ByteBuffer;
import java.util.Arrays;

public class Pointer
{
	private final String _name;
	private final String _type;
	private final int _size;
	private byte[] _data;
	private boolean _isLong;

	public Pointer(String name, String type, long val) {
		_name = name;
		_type = type;
		switch (type) {
			case "resb", "resw", "resd", "resq", "rest" -> {
				_size = (int)val;
				_data = new byte[8 * _size];
			}
			default -> {
				_size = -1;
				_data = longToBytes(val);
				_isLong = true;
			}
		}
	}

	public Pointer(String name, String type, double val) {
		_name = name;
		_type = type;
		_size = -1;
		_data = floatToBytes(val);
		_isLong = false;
	}

	public Pointer(String name, String type, String val) {
		_name = name;
		_type = type;
		_size = -1;
		_data = val.getBytes();
		_isLong = false;
	}

	public String toString() {
		switch (_type) {
			case "equ" -> {
				return String.format("0x%016x", getLong());
			}
			case "db", "dw", "dd", "dq", "dt" -> {
				if (_isLong) {
					return String.format("0x%016x", getLong());
				} else {
					return getString();
				}
			}
			case "resb", "resw", "resd", "resq", "rest" -> {
				StringBuilder out = new StringBuilder();
				for (int i = 0; i < _size; i++) {
					byte[] chunk = Arrays.copyOfRange(_data, i*8, i*8+8);
					out.append(String.format("0x%016x%n", bytesToLong(chunk)));
				}
				return out.toString();
			}
			default -> {return "";}
		}
	}

	public boolean isLong() {
		return _isLong;
	}

	public boolean isString() {
		return _type.equals("db") && !_isLong;
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

	public void setLong(long val) {
		_data = longToBytes(val);
	}

	public byte[] getData() {
		return Arrays.copyOf(_data, _data.length);
	}

	public void setData(byte[] dat) {
		_data = dat;
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
				(byte) ((val) & 0xff),
				};
	}

	public static byte[] floatToBytes(double val) {
		return new byte[8];
	}

	public static long bytesToLong(byte[] dat) {
		ByteBuffer buffer = ByteBuffer.wrap(dat);
		return buffer.getLong();
	}
}
