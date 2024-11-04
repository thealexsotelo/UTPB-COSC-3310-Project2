public class Line
{
	private String opcode;
	private String param1 = null;
	private String param2 = null;

	public Line(String op) {
		opcode = op;
	}

	public Line(String op, String p1) {
		opcode = op;
		param1 = p1;
	}

	public Line(String op, String p1, String p2) {
		opcode = op;
		param1 = p1;
		param2 = p2;
	}

	public boolean exec() {
		return false;
	}

}
