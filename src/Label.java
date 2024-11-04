public class Label
{
	private final String _name;
	private final int _lineNum;

	public Label(String name, int line) {
		_name = name;
		_lineNum = line;
	}

	public int getLine() {
		return _lineNum;
	}
}
