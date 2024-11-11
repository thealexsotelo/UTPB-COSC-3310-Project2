import javafx.application.Platform;
import javafx.stage.FileChooser;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Pattern;

public class PrgmFrame extends JFrame
{

	//<editor-fold desc="Registers">

	private final HashMap<String, Pointer> _registers = new HashMap<>();
	{
		//_registers.put("rsi", new Pointer("rsi", "dq", 0L));
		//_registers.put("rdi", new Pointer("rdi", "dq", 0L));
		//_registers.put("rbp", new Pointer("rbp", "dq", 0L));
		//_registers.put("rsp", new Pointer("rsp", "dq", 0L));
		_registers.put("rax", new Pointer("rax", "dq", 0L));
		_registers.put("rbx", new Pointer("rbx", "dq", 0L));
		_registers.put("rcx", new Pointer("rcx", "dq", 0L));
		_registers.put("rdx", new Pointer("rdx", "dq", 0L));
		_registers.put("r08", new Pointer("r08", "dq", 0L));
		_registers.put("r09", new Pointer("r09", "dq", 0L));
		_registers.put("r10", new Pointer("r10", "dq", 0L));
		_registers.put("r11", new Pointer("r11", "dq", 0L));
		_registers.put("r12", new Pointer("r12", "dq", 0L));
		_registers.put("r13", new Pointer("r13", "dq", 0L));
		_registers.put("r14", new Pointer("r14", "dq", 0L));
		_registers.put("r15", new Pointer("r15", "dq", 0L));
		//_registers.put("pc", new Pointer("pc", "dq", 0L));
	}
	private final HashMap<String, Boolean> _regHighlights = new HashMap<>();
	{
		//_regHighlights.put("rsi", false);
		//_regHighlights.put("rdi", false);
		//_regHighlights.put("rbp", false);
		//_regHighlights.put("rsp", false);
		_regHighlights.put("rax", false);
		_regHighlights.put("rbx", false);
		_regHighlights.put("rcx", false);
		_regHighlights.put("rdx", false);
		_regHighlights.put("r08", false);
		_regHighlights.put("r09", false);
		_regHighlights.put("r10", false);
		_regHighlights.put("r11", false);
		_regHighlights.put("r12", false);
		_regHighlights.put("r13", false);
		_regHighlights.put("r14", false);
		_regHighlights.put("r15", false);
		//_regHighlights.put("pc", false);
	}
	private boolean _zeroFlag = false;
	private boolean _signFlag = false;
	private boolean _overflowFlag = false;
	private boolean _carryFlag = false;
	private boolean _cmpHighlight = false;

	private final Stack<String> _stack = new Stack<>();
	private int _stackHighlight = 0;

	private final HashMap<String, Pointer> _pointers = new HashMap<>();
	private final HashMap<String, Boolean> _ptrHighlights = new HashMap<>();
	private final HashMap<String, Label> _labels = new HashMap<>();

	//</editor-fold>

	//<editor-fold desc="Member Vars">

	private final JASM _parent;

	private JTextArea _internalData;
	private Highlighter _datHl = new DefaultHighlighter();
	//private JTextArea _internalDataLines;
	private JTextArea _userProgram;
	private Highlighter _prgmHl = new DefaultHighlighter();
	private JTextArea _userProgramLines;
	private JTextArea _prgmOutput;
	private Highlighter _outHl = new DefaultHighlighter();
	//private JTextArea _prgmOutputLines;
	private int _mode = 0;

	protected boolean _dirty = false;
	protected boolean _compiled = false;
	private String _lastSavedState = "";
	private int _lineNumber = 1;
	private boolean _running = false;
	private double _runDelay = 0.0;
	private final long _defaultDelay = 100;
	private String[] _lines;

	protected String _fileName;
	private File _tgtFile;
	private final int _iFormat;
	private final String _sFormat;
	private final String _fileExt;

	//</editor-fold>

	//<editor-fold desc="Constructors">

	public PrgmFrame(JASM parent, String code, int format) {
		super();

		_parent = parent;

		if (_mode > 0) {
			_registers.remove("r08");
			_registers.remove("r09");
			_registers.remove("r10");
			_registers.remove("r11");
			_registers.remove("r12");
			_registers.remove("r13");
			_registers.remove("r14");
			_registers.remove("r15");
		}

		buildMenu();
		buildFrame(code);

		_iFormat = format;
		switch (format) {
			case JASM.NASM_FORMAT:
				_sFormat = "NASM";
				_fileExt = ".asm";
				break;
			case JASM.GCC_FORMAT:
				_sFormat = "GCC";
				_fileExt = ".s";
				break;
			default:
				_sFormat = "x64";
				_fileExt = ".asm";
				break;
		}

		updateLoop();
	}

	public PrgmFrame(JASM parent, File prgmFile) {
		super();

		_parent = parent;

		_fileName = prgmFile.getName();

		String code = loadFile(prgmFile);

		if (code == null || code.isBlank()) {
			JOptionPane.showMessageDialog(_parent, String.format("Warning: Loaded file %s appears to contain no valid code!", _fileName), "Warning", JOptionPane.WARNING_MESSAGE);
		}

		buildMenu();
		buildFrame(code);

		_fileExt = getExtension(_fileName);
		switch(_fileExt) {
			case ".asm":
				_sFormat = "NASM";
				_iFormat = JASM.NASM_FORMAT;
				break;
			case ".s":
				_sFormat = "GCC";
				_iFormat = JASM.GCC_FORMAT;
				break;
			case null:
			default:
				_sFormat = "x64";
				_iFormat = 0;
				break;
		}

		updateLoop();
	}

	//</editor-fold>

	//<editor-fold desc="Init">

	private void buildMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");

		JMenuItem delayMenuItem = new JMenuItem("Set Run Speed");
		//loadMenuItem.setMnemonic(); TODO
		delayMenuItem.addActionListener(_ -> {
			String delayStr = JOptionPane.showInputDialog(_parent, String.format("Time to wait between each line of code during run: %.2f seconds", _runDelay), _runDelay);
			if (delayStr == null) {
				return;
			}
			try {
				_runDelay = Double.parseDouble(delayStr);
			} catch (NumberFormatException _) {

			}
		});
		fileMenu.add(delayMenuItem);

		JMenuItem saveMenuItem = new JMenuItem("Save");
		//loadMenuItem.setMnemonic(); TODO
		saveMenuItem.addActionListener(_ -> saveFile());
		fileMenu.add(saveMenuItem);

		JMenuItem saveAsMenuItem = new JMenuItem("Save As");
		//loadMenuItem.setMnemonic(); TODO
		saveAsMenuItem.addActionListener(_ -> Platform.runLater(() -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Save File As...");
			chooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("NASM-Format Files", "*.asm"),
					new FileChooser.ExtensionFilter("GCC_Format Files", "*.s")
			);
			File prgmFile = chooser.showSaveDialog(null);

			saveFile(prgmFile);
		}));
		fileMenu.add(saveAsMenuItem);

		fileMenu.addSeparator();

		JMenuItem closeMenuItem = new JMenuItem("Close File");
		//closeMenuItem.setMnemonic(); TODO
		closeMenuItem.addActionListener(_ -> _parent.closeTab());
		fileMenu.add(closeMenuItem);

		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}

	private void buildFrame(String code) {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 9;
		gbc.gridheight = 1;
		gbc.weightx = 0.3;
		gbc.weighty = 1.0;

		//<editor-fold desc="Control Panel">

		JPanel controls = new JPanel();
		controls.setLayout(new GridBagLayout());

		GridBagConstraints cgbc = new GridBagConstraints();
		cgbc.anchor = GridBagConstraints.CENTER;
		cgbc.fill = GridBagConstraints.BOTH;
		cgbc.gridx = 0;
		cgbc.gridy = 0;
		cgbc.gridwidth = 1;
		cgbc.gridheight = 1;
		cgbc.weightx = 1.0;
		cgbc.weighty = 0.02;

		JButton runButton = new JButton("Run");
		//runButton.setMnemonic(); TODO
		runButton.addActionListener(_ -> run());
		controls.add(runButton, cgbc);

		cgbc.gridx = 1;
		JButton stepButton = new JButton("Step");
		//stepButton.setMnemonic(); TODO
		stepButton.addActionListener(_ -> step());
		controls.add(stepButton, cgbc);

		cgbc.gridx = 2;
		JButton pauseButton = new JButton("Pause");
		//pauseButton.setMnemonic(); TODO
		pauseButton.addActionListener(_ -> pause());
		controls.add(pauseButton, cgbc);

		cgbc.gridx = 3;
		JButton resetButton = new JButton("Stop/Reset");
		//resetButton.setMnemonic(); TODO
		resetButton.addActionListener(_ -> reset());
		controls.add(resetButton, cgbc);

		cgbc.gridx = 0;
		cgbc.gridy = 1;
		cgbc.gridwidth = 4;
		cgbc.gridheight = 1;
		cgbc.weighty = 1.0;
		_internalData = new JTextArea();
		_internalData.setLineWrap(false);
		_internalData.setEditable(false);
		_internalData.setHighlighter(_datHl);
		controls.add(scrollTextArea(_internalData), cgbc);

		add(controls, gbc);

		//</editor-fold>

		// <editor-fold desc="Code Panel">

		gbc.gridx = 9;
		gbc.gridwidth = 1;
		gbc.weightx = 0.01;
		_userProgramLines = new JTextArea();
		_userProgramLines.setLineWrap(false);
		_userProgramLines.setEditable(false);
		//_userProgramLines.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		JScrollPane lineScroll = new JScrollPane(_userProgramLines);
		lineScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		lineScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		add(lineScroll, gbc);

		gbc.gridx = 10;
		gbc.gridwidth = 10;
		gbc.weightx = 1.0;
		_userProgram = new JTextArea();
		_userProgram.setLineWrap(false);
		_userProgram.setEditable(true);
		_userProgram.setText(code);
		_userProgram.setHighlighter(_prgmHl);
		JScrollPane prgmScroll = scrollTextArea(_userProgram);
		add(prgmScroll, gbc);

		_userProgram.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				checkDirty();
				updateLines();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				checkDirty();
				updateLines();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
				checkDirty();
				updateLines();
			}
		});

		lineScroll.getVerticalScrollBar().addAdjustmentListener(_ -> prgmScroll.getVerticalScrollBar().setValue(lineScroll.getVerticalScrollBar().getValue()));
		prgmScroll.getVerticalScrollBar().addAdjustmentListener(_ -> lineScroll.getVerticalScrollBar().setValue(prgmScroll.getVerticalScrollBar().getValue()));

		// </editor-fold>

		updateLines();

		// <editor-fold desc="Output Panel">

		gbc.gridx = 20;
		gbc.weightx = 1.0;
		_prgmOutput = new JTextArea();
		_prgmOutput.setLineWrap(false);
		_prgmOutput.setEditable(false);
		_prgmOutput.setHighlighter(_prgmHl);
		add(scrollTextArea(_prgmOutput), gbc);

		// </editor-fold>

		setFont("Courier New", 24);
		showRegisters();
	}

	private JScrollPane scrollTextArea(JTextArea ta) {
		JScrollPane scroll = new JScrollPane(ta);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scroll;
	}

	private void checkDirty() {
		_dirty = !_userProgram.getText().trim().equals(_lastSavedState.trim());
	}

	private void updateLines() {
		String[] lines = _userProgram.getText().split(Pattern.quote("\n"));
		StringBuilder lineNums = new StringBuilder();
		for (int lineNum = 0; lineNum < lines.length; lineNum++) {
			lineNums.append(lineNum).append("\n");
		}
		_userProgramLines.setText(lineNums.toString());
	}

	private void setFont(String name, int size) {
		Font f = new Font(name, Font.PLAIN, size);
		_internalData.setFont(f);
		_userProgramLines.setFont(f);
		_userProgram.setFont(f);
		_prgmOutput.setFont(f);
	}

	private String checkRegister(String reg) {
		if (_registers.get(reg).isString()) {
			return String.format("%s: ptr@%s%n", reg.toLowerCase(), _registers.get(reg).getName());
		} else {
			return String.format("%s: 0x%016x%n", reg.toLowerCase(), _registers.get(reg).getLong());
		}
	}

	private void showRegisters() {
		StringBuilder dat = new StringBuilder();
		dat.append(String.format("Registers%n"));
		dat.append(String.format(" pc: ptr@line#%d%n", _lineNumber));
		dat.append(checkRegister("rax"));
		dat.append(checkRegister("rbx"));
		dat.append(checkRegister("rcx"));
		dat.append(checkRegister("rdx"));
		//dat.append(checkRegister("rsp"));
		//dat.append(checkRegister("rbp"));
		//dat.append(checkRegister("rsi"));
		//dat.append(checkRegister("rdi"));
		if (_mode == 0)
		{
			dat.append(checkRegister("r08"));
			dat.append(checkRegister("r09"));
			dat.append(checkRegister("r10"));
			dat.append(checkRegister("r11"));
			dat.append(checkRegister("r12"));
			dat.append(checkRegister("r13"));
			dat.append(checkRegister("r14"));
			dat.append(checkRegister("r15"));
		}
		dat.append(String.format("%nFlags%n"));
		dat.append(String.format("Zero: %d%n", _zeroFlag ? 1 : 0));
		dat.append(String.format("Sign: %s%n", _signFlag ? 1 : 0));
		//dat.append(String.format("Overflow: %s%n", _overflowFlag ? 1 : 0));
		//dat.append(String.format("Carry: %s%n", _carryFlag ? 1 : 0));
		dat.append(String.format("%nPointers%n"));
		for (String pk : _pointers.keySet()) {
			if (_registers.containsKey(pk)) {
				continue;
			}
			dat.append(String.format("%s: %s%n", pk, _pointers.get(pk).toString()));
		}
		dat.append(String.format("%nStack:%n"));
		for (String s : _stack) {
			dat.append(String.format("%s%n", s));
		}
		_datHl.removeAllHighlights();
		_internalData.setText(dat.toString());
	}

	//</editor-fold>

	//<editor-fold desc="File IO">

	private String loadFile(File prgmFile) {
		try (Scanner scan = new Scanner(new FileInputStream(prgmFile))) {
			StringBuilder code = new StringBuilder();
			while (scan.hasNext()) {
				code.append(scan.nextLine()).append("\n");
			}
			return code.toString();
		} catch (FileNotFoundException fnfEx) {
			fnfEx.printStackTrace();
			JOptionPane.showMessageDialog(_parent, String.format("Error: Failed to open file %s for reading!%n%s", prgmFile.getName(), fnfEx.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public boolean saveFile() {
		return saveFile(_tgtFile);
	}

	public boolean saveFile(File prgmFile) {
		try
		{
			if (!prgmFile.getParentFile().mkdirs()) {
				JOptionPane.showMessageDialog(_parent, String.format("Error: Could not create file %s!%n", prgmFile.getName()), "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (!prgmFile.exists())
			{
				if (!prgmFile.createNewFile()) {
					JOptionPane.showMessageDialog(_parent, String.format("Error: Could not create file %s!%n", prgmFile.getName()), "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
			JOptionPane.showMessageDialog(_parent, String.format("Error: Could not create file %s!%n%s", prgmFile.getName(), ioEx.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try (PrintWriter pw = new PrintWriter(new FileOutputStream(prgmFile), true)) {
			pw.write(_userProgram.getText());
			_lastSavedState = _userProgram.getText().trim();
		} catch (FileNotFoundException fnfEx) {
			fnfEx.printStackTrace();
			JOptionPane.showMessageDialog(_parent, String.format("Error: Failed to open file %s for writing!%n%s", prgmFile.getName(), fnfEx.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return false;
	}

	private String getExtension(String fileName) {
		if (fileName.isBlank()) {
			return null;
		}
		if (fileName.contains(".")) {
			return fileName.substring(fileName.lastIndexOf('.'));
		}
		return null;
	}

	//</editor-fold>

	//<editor-fold desc="Run">

	private boolean start() {
		_userProgram.setEditable(false);

		if (!_compiled) {
			_prgmOutput.setText("");
			_prgmOutput.append(String.format("~/>nasm -f elf64 %s%s%n", _fileName, _fileExt));
			if (!compile()) {
				_prgmOutput.append(String.format("Compilation failed!%n"));
				return true;
			}
			_compiled = true;
			_prgmOutput.append(String.format("~/>ld -s -o %s %s.o%n", _fileName, _fileName));
			_prgmOutput.append(String.format("~/>./%s%n", _fileName));
		}
		return false;
	}

	private void reset() {
		for (String reg : _registers.keySet()) {
			_registers.put(reg, new Pointer(reg, "dq", 0L));
		}
		for (String reg : _regHighlights.keySet()) {
			_regHighlights.put(reg, false);
		}
		_zeroFlag = false;
		_signFlag = false;
		_cmpHighlight = false;

		_stack.clear();
		_stackHighlight = 0;

		for (String ptr : _pointers.keySet()) {
			_ptrHighlights.put(ptr, false);
		}

		_lineNumber = -1;
		_running = false;
		_compiled = false;

		_userProgram.setEditable(true);
	}

	private void run() {
		if (start()) {
			return;
		}
		_running = true;
	}

	private void step() {
		_running = false;
		if (start()) {
			return;
		}
		nextLine();
	}

	private void pause() {
		_running = false;
	}

	private void updateLoop() {
		new Thread(() -> {
			try {
				while (true)
				{
					if (_running)
					{
						nextLine();
						Thread.sleep((long) (_runDelay * 1000));
					}
					else
					{
						Thread.sleep(_defaultDelay);
					}
				}
			} catch (InterruptedException _) {

			}
		}).start();
	}

	//</editor-fold>

	//<editor-fold desc="Ops">

	private void nextLine() {
		String line = _lines[_lineNumber];
		_lineNumber += 1;
		if (line == null || line.isBlank()) {
			nextLine();
			return;
		}
		String[] tokens = line.split(Pattern.quote(" "));
		if (tokens[0].equals("label")) {
			nextLine();
			return;
		}
		switch (tokens[0]) {
			case "mov" -> mov(tokens[1], tokens[2]);
			case "add" -> add(tokens[1], tokens[2]);
			case "sub" -> sub(tokens[1], tokens[2]);
			case "and" -> and(tokens[1], tokens[2]);
			case "or" -> or(tokens[1], tokens[2]);
			case "xor" -> xor(tokens[1], tokens[2]);
			case "cmp" -> cmp(tokens[1], tokens[2]);
			case "mul" -> mul(tokens[1]);
			case "div" -> div(tokens[1]);
			case "inc" -> inc(tokens[1]);
			case "dec" -> dec(tokens[1]);
			case "not" -> not(tokens[1]);
			case "push" -> push(tokens[1]);
			case "pop" -> pop(tokens[1]);
			case "call" -> call(tokens[1]);
			case "ret" -> ret();
			case "int" -> intr(0);
			case "jmp" -> jmp(tokens[1]);
			case "jeq", "je" -> jeq(tokens[1]);
			case "jne" -> jne(tokens[1]);
			case "jg" -> jg(tokens[1]);
			case "jge" -> jge(tokens[1]);
			case "jl" -> jl(tokens[1]);
			case "jle" -> jle(tokens[1]);
			default -> segFault(String.format("Opcode %s not recognized", tokens[0]));
		}
		showRegisters();
	}

	private boolean deref(String param) {
		return param.startsWith("[") && param.endsWith("]");
	}

	private String getPtr(String param) {
		return param.substring(1, param.lastIndexOf(']'));
	}

	private Pointer getValue(String param) {
		if (deref(param)) {
			String ptr = getPtr(param);
			if (_pointers.containsKey(ptr)) {
				if (_pointers.get(ptr).isLong()) {
					return new Pointer(ptr, "dq", _pointers.get(ptr).getLong());
				}
				if (_pointers.get(ptr).isString()) {
					return new Pointer("literal", "db", _pointers.get(ptr).charAt(0));
				}
				segFault("Unrecognized pointer type");
				return null;
			}
			segFault(String.format("Attempted to dereference unknown pointer", ptr));
			return null;
		}
		if (_pointers.containsKey(param)) {
			return _pointers.get(param);
		}
		if (_registers.containsKey(param)) {
			return _registers.get(param);
		}
		Integer intVal = parseInt(param);
		if (intVal == null) {
			Double fltVal = parseDouble(param);
			if (fltVal == null) {
				segFault("Pointer value not parseable as either integer or float");
				return null;
			}
			return new Pointer("literal", "dq", fltVal);
		}
		return new Pointer("literal", "dq", intVal);
	}

	private void setValue(String param, Pointer val) {
		if (deref(param)) {
			String ptr = getPtr(param);
			if (_pointers.containsKey(ptr)) {
				if (_pointers.get(ptr).isLong()) {
					//if (val.getName().equals("literal") && val.isLong()) {
					if (val.isLong()) {
						_pointers.get(ptr).setLong(val.getLong());
						return;// true;
					}
					segFault("Attempted to set a non-integer value to an integer pointer");
					return;// false;
				}
				segFault("Attempted to set a long value to a non-integer pointer");
				return;// false;
			}
			segFault(String.format("Attempted to assign value to unknown pointer %s", ptr));
			return;// false;
		}
		if (_pointers.containsKey(param)) {
			_pointers.get(param).setData(val.getData());
			return;// true;
		}
		if (_registers.containsKey(param)) {
			_registers.put(param, val);
			return;// true;
		}
		segFault(String.format("Identifier %s not recognized as a valid register or pointer", param));
		return;// false;
	}

	private void segFault(String errorMsg) {
		_prgmOutput.append(String.format("Segmentation fault%n"));
		_prgmOutput.append(String.format("%s%n", errorMsg));
		_running = false;
		_compiled = false;
		_lineNumber = -1;
	}

	private void mov(String tgt, String value) {
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		setValue(tgt, ptr);
	}

	private void add(String tgt, String value) {
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		Pointer base = getValue(tgt);
		crash = base == null;
		if (crash) {
			return;
		}

		if (base.isString()) {
			setValue(tgt, new Pointer(String.format("%s+%d", base.getName(), ptr.getLong()), "db", base.subString((int)ptr.getLong())));
		}
		if (base.isLong()) {
			long l = base.getLong();
			l += ptr.getLong();
			_zeroFlag = l == 0L;
			_signFlag = l < 0L;
			setValue(tgt, new Pointer("literal", "dq", l));
		}
	}

	private void sub(String tgt, String value) {
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		Pointer base = getValue(tgt);
		crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l -= ptr.getLong();
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
		setValue(tgt, new Pointer("literal", "dq", l));
	}

	private void mul(String value) {
		if (!value.equals("rbx")) {
			segFault("The MUL opcode only accepts RBX as a valid parameter"); // mul only works if it's called on rbx for some reason?
			return;
		}
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		Pointer base = getValue("rax");
		crash = base == null;
		if (crash) {
			return;
		}
		Pointer c = getValue("rcx");
		crash = c == null;
		if (crash) {
			return;
		}
		if (c.getLong() != 0L) {
			segFault("The value of RCX must be exactly 0x00 before calling MUL");
			return;
		}
		Pointer d = getValue("rdx");
		crash = d == null;
		if (crash) {
			return;
		}
		if (d.getLong() != 0L) {
			segFault("The value of RDX must be exactly 0x00 before calling MUL");
			return;
		}

		long l = base.getLong();
		l *= ptr.getLong();
		setValue("rax", new Pointer("literal", "dq", l));
		setValue("rbx", new Pointer("literal", "dq", 0L));
		setValue("rcx", new Pointer("literal", "dq", 0L));
		setValue("rdx", new Pointer("literal", "dq", 0L));
	}

	private void div(String value) {
		if (!value.equals("rbx")) {
			segFault("The DIV opcode only accepts RBX as a valid parameter"); // div only works if it's called on rbx for some reason?
			return;
		}
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		if (ptr.getLong() == 0L) {
			segFault("The value of RBX must be exactly 0x00 before calling DIV"); // divide by 0 = segfault
			return;
		}
		Pointer base = getValue("rax");
		crash = base == null;
		if (crash) {
			return;
		}
		Pointer c = getValue("rcx");
		crash = c == null;
		if (crash) {
			return;
		}
		if (c.getLong() != 0L) {
			segFault("The value of RCX must be exactly 0x00 before calling DIV");
			return;
		}
		Pointer d = getValue("rdx");
		crash = d == null;
		if (crash) {
			return;
		}
		if (d.getLong() != 0L) {
			segFault("The value of RDX must be exactly 0x00 before calling DIV");
			return;
		}

		long l = base.getLong();
		l *= ptr.getLong();
		setValue("rax", new Pointer("literal", "dq", l));
		setValue("rbx", new Pointer("literal", "dq", 0L));
		setValue("rcx", new Pointer("literal", "dq", 0L));
		l = base.getLong();
		l = l % ptr.getLong();
		setValue("rdx", new Pointer("literal", "dq", l));
	}

	private void inc(String tgt) {
		Pointer base = getValue(tgt);
		boolean crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l += 1L;
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
		setValue(tgt, new Pointer("literal", "dq", l));
	}

	private void dec(String tgt) {
		Pointer base = getValue(tgt);
		boolean crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l -= 1L;
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
		setValue(tgt, new Pointer("literal", "dq", l));
	}

	private void and(String tgt, String value) {
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		Pointer base = getValue(tgt);
		crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l = l & ptr.getLong();
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
		setValue(tgt, new Pointer("literal", "dq", l));
	}

	private void or(String tgt, String value) {
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		Pointer base = getValue(tgt);
		crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l = l | ptr.getLong();
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
		setValue(tgt, new Pointer("literal", "dq", l));
	}

	private void xor(String tgt, String value) {
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		Pointer base = getValue(tgt);
		crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l = l ^ ptr.getLong();
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
		setValue(tgt, new Pointer("literal", "dq", l));
	}

	private void not(String tgt) {
		Pointer base = getValue(tgt);
		boolean crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l = ~l;
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
		setValue(tgt, new Pointer("literal", "dq", l));
	}

	private void push(String tgt) {
		if (tgt.equals("pc")) {
			_stack.push(String.format("ptr@line%d", _lineNumber));
			return;
		}
		Pointer ptr = getValue(tgt);
		if (ptr == null) {
			return;
		}
		if (ptr.isLong()) {
			if (ptr.getType().equals("line")) {
				_stack.push(String.format("ptr@line%d", ptr.getLong()));
				return;
			}
			_stack.push(String.format("0x%016x", ptr.getLong()));
			return;
		}
		if (ptr.isString()) {
			_stack.push(String.format("ptr@%s", ptr.getName()));
		}
	}

	private void pop(String tgt) {
		if (tgt.equals("pc")) {
			ret();
		}
		String stackVal = _stack.pop();
		if (stackVal.startsWith("0x")) {
			Integer l = parseInt(stackVal);
			if (l == null) {
				segFault(String.format("Failed to parse apparent integer value %s as valid integer", stackVal));
				return;
			}
			setValue(tgt, new Pointer(tgt, "dq", l));
			return;
		}
		if (stackVal.startsWith("ptr@line")) {
			String ptr = stackVal.substring(8);
			Integer l = parseInt(ptr);
			if (l == null) {
				segFault(String.format("Failed to parse apparent integer value %s as valid integer (should be a valid opcode/line)", ptr));
				return;
			}
			setValue(tgt, new Pointer(tgt, "line", l));
			return;
		}
		if (stackVal.startsWith("ptr@")) {
			String ptr = stackVal.substring(4);
			if (ptr == null || !_pointers.containsKey(ptr)) {
				segFault(String.format("Apparent pointer address %s does not reference a known pointer", ptr));
				return;
			}
			setValue(tgt, new Pointer(ptr, "db", _pointers.get(ptr).getString()));
			return;
		}
		segFault(String.format("Unknown pointer type %s", stackVal));
	}

	private void call(String tgt) {
		push("pc");
		jmp(tgt);
	}

	private void ret() {
		String returnPtr = _stack.pop();
		if (returnPtr.startsWith("ptr@line")) {
			Integer line = parseInt(returnPtr.substring("ptr@line".length()));
			if (line == null) {
				segFault(String.format("Failed to parse apparent integer value %s as valid integer (should be a valid opcode/line)", returnPtr));
				return;
			}
			_lineNumber = line;
			return;
		}
		segFault(String.format("Attempted to return to non-line pointer %s", returnPtr));
	}

	private void intr(int value) {
		Pointer a = getValue("rax");
		boolean crash = a == null;
		if (crash) {
			return;
		}
		if (a.getLong() == 1L) {
			_running = false;
			_lineNumber = -1;
			_prgmOutput.append(String.format("Program terminated%n"));
			_prgmOutput.append("~/>");
			return;
		}
		if (a.getLong() == 4L)
		{
			Pointer b = getValue("rbx");
			crash = b == null;
			if (crash)
			{
				return;
			}
			Pointer c = getValue("rcx");
			crash = c == null;
			if (crash)
			{
				return;
			}
			String cName = c.getName();
			if (cName.contains("+")) {
				cName = cName.substring(0, cName.indexOf("+"));
			}
			if (!_pointers.containsKey(cName)) {
				segFault(String.format("Unknown pointer %s - RCX must contain a pointer/address of a character string", c.getName()));
				return;
			}
			Pointer d = getValue("rdx");
			crash = d == null;
			if (crash)
			{
				return;
			}
			if (d.getLong() < 1) {
				segFault("RDX must contain an integer literal number of characters to print");
				return;
			}
			if (b.getLong() == 1L) {
				int strLen = (int)d.getLong();
				_prgmOutput.append(_registers.get("rcx").getString().substring(0, strLen).replace(String.format("%s", (char)0x00), ""));
			}
			setValue("rax", new Pointer("??", "db", "??"));
			setValue("rbx", new Pointer("??", "db", "??"));
			setValue("rcx", new Pointer("??", "db", "??"));
			setValue("rdx", new Pointer("??", "db", "??"));
			return;
		}
		segFault(String.format("Unknown interrupt code %d - RAX must contain either 0x01 (system exit) or 0x04 (print)", a.getLong()));
	}

	private void cmp(String tgt, String value) {
		Pointer ptr = getValue(value);
		boolean crash = ptr == null;
		if (crash) {
			return;
		}
		Pointer base = getValue(tgt);
		crash = base == null;
		if (crash) {
			return;
		}
		long l = base.getLong();
		l -= ptr.getLong();
		_zeroFlag = l == 0L;
		_signFlag = l < 0L;
	}

	private void jmp(String tgt) {
		if (_labels.containsKey(tgt)) {
			_lineNumber = _labels.get(tgt).getLine();
			return;
		}
		segFault(String.format("Could not identify valid label %s", tgt));
	}

	private void jeq(String tgt) {
		if (_labels.containsKey(tgt)) {
			if (_zeroFlag) {
				_lineNumber = _labels.get(tgt).getLine();
			}
			return;
		}
		segFault(String.format("Could not identify valid label %s", tgt));
	}

	private void jg(String tgt) {
		if (_labels.containsKey(tgt)) {
			if (!_zeroFlag && !_signFlag) {
				_lineNumber = _labels.get(tgt).getLine();
			}
			return;
		}
		segFault(String.format("Could not identify valid label %s", tgt));
	}

	private void jge(String tgt) {
		if (_labels.containsKey(tgt)) {
			if (_zeroFlag || !_signFlag) {
				_lineNumber = _labels.get(tgt).getLine();
			}
			return;
		}
		segFault(String.format("Could not identify valid label %s", tgt));
	}

	private void jne(String tgt) {
		if (_labels.containsKey(tgt)) {
			if (!_zeroFlag) {
				_lineNumber = _labels.get(tgt).getLine();
			}
			return;
		}
		segFault(String.format("Could not identify valid label %s", tgt));
	}

	private void jl(String tgt) {
		if (_labels.containsKey(tgt)) {
			if (!_zeroFlag && _signFlag) {
				_lineNumber = _labels.get(tgt).getLine();
			}
			return;
		}
		segFault(String.format("Could not identify valid label %s", tgt));
	}

	private void jle(String tgt) {
		if (_labels.containsKey(tgt)) {
			if (_zeroFlag || _signFlag) {
				_lineNumber = _labels.get(tgt).getLine();
			}
			return;
		}
		segFault(String.format("Could not identify valid label %s", tgt));
	}

	//</editor-fold>

	//<editor-fold desc="Compile">

	private boolean compile() {
		String code = _userProgram.getText().toLowerCase();
		String[] lines = code.split(Pattern.quote("\n"));

		boolean dataSection = false;
		boolean textSection = false;
		String label = null;
		boolean isCode = false;
		boolean compileSuccess = true;
		boolean globalStart = false;
		boolean labelStart = false;

		for (int lineNum = 0; lineNum < lines.length; lineNum++) {
			String line = lines[lineNum].trim();
			if (line.isBlank()) {
				continue;
			}
			if (label != null) {
				_labels.put(label, new Label(label, lineNum));
				if (label.equals("_start")) {
					_lineNumber = lineNum;
				}
				label = null;
			}
			String cleanOp = parseLine(line, lineNum);
			String[] tokens = cleanOp.split(Pattern.quote(" "));
			switch (tokens[0]) {
				case "section" -> {
					dataSection = tokens[1].equals("data");
					textSection = tokens[1].equals("text");
					if (dataSection) {
						isCode = false;
					}
				}
				case "global" -> {
					globalStart = true;
				}
				case "label" -> {
					if (dataSection) {
						_prgmOutput.append(String.format("Error line %d: Code labels not allowed outside of .text section%n", lineNum));
						compileSuccess = false;
					} else {
						label = tokens[1];
						isCode = true;
						if (label.equals("_start")) {
							labelStart = true;
						}
					}
				}
				case "ptr" -> {
					if (textSection) {
						_prgmOutput.append(String.format("Error line %d: Pointer definition not allowed outside of .data section%n", lineNum));
						compileSuccess = false;
					} else {
						if (tokens[2].equals("equ")) {
							break;
						}
						if (tokens[2].equals("db")) {
							StringBuilder ptrVal = new StringBuilder();
							for (int i = 3; i < tokens.length; i++) {
								ptrVal.append(tokens[i]).append(" ");
							}
							_pointers.put(tokens[1], new Pointer(tokens[1], tokens[2], ptrVal.toString()));
						}
						if (tokens[2].equals("dq")) {
							try
							{
								int ptrVal = Integer.parseInt(tokens[3]);
								_pointers.put(tokens[1], new Pointer(tokens[1], tokens[2], ptrVal));
							}
							catch (NumberFormatException nfEx)
							{

							}
						}
					}
				}
				case "Error" -> {
					_prgmOutput.append(String.format("%s%n", cleanOp));
					compileSuccess = false;
				}
				default -> {
					if (!isCode) {
						_prgmOutput.append(String.format("Error line %d: Unreachable code%n", lineNum));
					}
				}
			}
		}

		if (!globalStart || !labelStart) {
			_prgmOutput.append(String.format("Error: Programs must include both a \"global _start\" and \"_start:\" label%n"));
			compileSuccess = false;
		}

		_lines = new String[lines.length];

		for (int lineNum = 0; lineNum < lines.length; lineNum++) {
			String line = lines[lineNum].trim();
			if (line.isBlank()) {
				_lines[lineNum] = null;
				continue;
			}
			String cleanOp = parseLine(line, lineNum);
			_lines[lineNum] = cleanOp;
			String[] tokens = cleanOp.split(Pattern.quote(" "));
			switch (tokens[0]) {
				case "Error" -> {
					_prgmOutput.append(String.format("%s%n", cleanOp));
					compileSuccess = false;
				}
				case "ptr" -> {
					if (tokens[2].equals("equ")) {
						String param1 = tokens[3];
						if (!_pointers.containsKey(param1)) {
							_prgmOutput.append(String.format("Error line %d: Unknown pointer identifier %s in equation%n", lineNum, param1));
							compileSuccess = false;
							break;
						}
						if (!_pointers.get(param1).isString()) {
							_prgmOutput.append(String.format("Error line %d: Pointer equations only supported for valid string literals%n", lineNum));
							compileSuccess = false;
							break;
						}
						int ptrLen = _pointers.get(param1).getString().length();
						_pointers.put(tokens[1], new Pointer(tokens[1], "equ", ptrLen));
					}
				}
				case "mov", "add", "sub", "and", "or", "xor", "cmp" -> {
					String param1 = tokens[1];
					String param2 = tokens[2];
					boolean deref = false;
					if (!_registers.containsKey(param1)) {
						if (param1.startsWith("[") && param1.endsWith("]")) {
							param1 = param1.substring(1, param1.lastIndexOf(']'));
							deref = true;
						}
						if (!_pointers.containsKey(param1)) {
							_prgmOutput.append(String.format("Error line %d: First parameter for operation %s must be a register or pointer%n", lineNum, tokens[0]));
							compileSuccess = false;
							break;
						}
					}
					if (!_registers.containsKey(param2)) {
						if (param2.startsWith("[") && param2.endsWith("]")) {
							if (deref) {
								_prgmOutput.append(String.format("Error line %d: Cannot dereference more than one pointer per line%n", lineNum));
								compileSuccess = false;
								break;
							}
							param2 = param2.substring(1, param2.lastIndexOf(']'));
						}
						if (!_pointers.containsKey(param2)) {
							try {
								Integer.parseInt(param2);
							} catch (NumberFormatException infEx) {
								try {
									Double.parseDouble(param2);
								} catch (NumberFormatException fnfEx) {
									_prgmOutput.append(String.format("Error line %d: Only integer and float literals are supported for operation %s%n", lineNum, tokens[0]));
									compileSuccess = false;
								}
							}
						}
					}
				}
				case "mul", "div", "inc", "dec", "not", "push", "pop" -> {
					String param1 = tokens[1];
					if (!_registers.containsKey(param1)) {
						if (param1.startsWith("[") && param1.endsWith("]")) {
							param1 = param1.substring(1, param1.lastIndexOf(']'));
						}
						if (!_pointers.containsKey(param1)) {
							_prgmOutput.append(String.format("Error line %d: First parameter for operation %s must be a register or pointer%n", lineNum, tokens[0]));
							compileSuccess = false;
						}
					}
				}
				case "int" -> {
					String param1 = tokens[1];
					Integer interrupt = parseInt(param1);
					if (interrupt == null) {
						_prgmOutput.append(String.format("Error line %d: Non-integer interrupt value %s%n", lineNum, param1));
						compileSuccess = false;
						break;
					}
					if (interrupt != 0x80) {
						_prgmOutput.append(String.format("Error line %d: Unsupported interrupt value %s%n", lineNum, param1));
						compileSuccess = false;
					}
				}
				case "call", "jmp", "jeq", "je", "jne", "jge", "jg", "jle", "jl" -> {
					String param1 = tokens[1];
					if (!_labels.containsKey(param1)) {
						_prgmOutput.append(String.format("Error line %d: Code label %s not found%n", lineNum, param1));
						compileSuccess = false;
					}
				}
			}
		}

		showRegisters();

		return compileSuccess;
	}

	private Integer parseInt(String param) {
		try {
			int intValue;
			if (param.startsWith("0x")) {
				intValue = Integer.parseInt(param.substring(2), 16);
			} else if (param.startsWith("0b")) {
				intValue = Integer.parseInt(param.substring(2), 2);
			} else if (param.startsWith("0d")) {
				intValue = Integer.parseInt(param.substring(2));
			} else {
				intValue = Integer.parseInt(param);
			}
			return intValue;
		} catch (NumberFormatException _) {
			return null;
		}
	}

	private Double parseDouble(String param) {
		try {
			return Double.parseDouble(param);
		} catch (NumberFormatException _) {
			return null;
		}
	}

	private Character parseChar(String param) {
		Integer intVal = parseInt(param);
		if (intVal == null) {
			return null;
		}
		return (char)intVal.intValue();
	}

	private String parseLine(String line, int lineNum) {
		String[] tokens = line.split("\\s+");
		try
		{
			switch (tokens[0])
			{
				case "section" -> {
					switch (tokens[1]) {
						case ".data" -> {return "section data";}
						case ".text" -> {return "section text";}
						default -> {return String.format("Error line %d: Unsupported section identifier %s", lineNum, tokens[1]);}
					}
				}
				case "global" -> {
					if (tokens[1].equals("_start")) {
						return "global _start";
					} else {
						return String.format("Error line %d: Unsupported global identifier %s", lineNum, tokens[1]);
					}
				}
				case "mov", "add", "sub", "and", "or", "xor", "cmp" -> {return twoParamOp(tokens[0], tokens, lineNum);}
				case "mul", "div", "inc", "dec", "not", "push", "pop", "call", "int", "jmp", "jeq", "je", "jne", "jge", "jg", "jle", "jl" -> {return oneParamOp(tokens[0], tokens, lineNum);}
				case "ret" -> {return noParamOp(tokens[0], tokens, lineNum);}
				default -> {
					if (tokens[0].endsWith(":")) {
						return String.format("label %s", tokens[0].substring(0, tokens[0].lastIndexOf(':')));
					}
					return switch (tokens[1])
					{
						case "db", "dw", "dd", "dq", "dt", "resb", "resw", "resd", "resq", "rest" -> pointer(tokens[0], tokens[1], line, lineNum);
						case "equ" -> literal(tokens[0], tokens, lineNum);
						default -> String.format("Error line %d: Unsupported operation %s", lineNum, tokens[0]);
					};
				}
			}
		} catch (Exception ex) {
			return String.format("Error line %d: No operation found on non-empty line", lineNum);
		}
	}

	private String literal(String name, String[] tokens, int lineNum) {
		if (!tokens[2].equals("$") || !tokens[3].equals("-") || tokens.length > 5) {
			return String.format("Error line %d: Only supported equation is \"equ $ - ptr\"", lineNum);
		}
		return String.format("ptr %s equ %s", name, tokens[4]);
	}

	private String pointer(String name, String type, String line, int lineNum) {
		StringBuilder ptr = new StringBuilder(String.format("ptr %s %s ", name, type));
		line = line.trim().substring(name.length());
		line = line.trim().substring(type.length());
		if (type.startsWith("res")) {
			Integer bufferSize = parseInt(line);
			if (bufferSize == null) {
				return String.format("Error line %d: Buffers must be defined as an integer number of bytes", lineNum);
			}
			ptr.append(line);
			return ptr.toString();
		}

		String[] tokens = line.split(Pattern.quote(","));
		StringBuilder ptrVal = new StringBuilder();
		boolean isString = false;
		for (String token : tokens)
		{
			String trimmed = token.trim();
			if (trimmed.startsWith("\"") || trimmed.startsWith("'")) {
				isString = true;
				if (!type.equals("db"))
				{
					return String.format("Error line %d: Type mismatch (strings must be type db)", lineNum);
				}
				continue;
			}
			if (type.equals("db")) {
				char c = parseChar(trimmed);
				switch (c) {
					case 0x0a, 0x0d, 0x00 -> isString = true;
				}
			}
		}

		if (!isString && !type.equals("dq")) {
			return String.format("Error line %d: Integer and float pointers currently only support type dq", lineNum);
		}

		if (!isString && tokens.length > 1) {
			return String.format("Error line %d: Integer and float arrays are not currently supported", lineNum);
		}

		for (String token : tokens) {
			String trimmed = token.trim();
			if (trimmed.startsWith("\"")) {
				trimmed = trimmed.substring(1, trimmed.lastIndexOf('"'));
				ptrVal.append(trimmed);
				continue;
			}
			if (trimmed.startsWith("'")) {
				trimmed = trimmed.substring(1, trimmed.lastIndexOf('\''));
				ptrVal.append(trimmed);
				continue;
			}
			Integer intVal = parseInt(trimmed);
			if (intVal == null) {
				if (isString) {
					return String.format("Error line %d: Unknown char/string value %s", lineNum, token);
				}
				Double floatVal = parseDouble(trimmed);
				if (floatVal == null) {
					return String.format("Error line %d: Unable to parse %s as valid data", lineNum, token);
				}
				ptrVal.append(floatVal);//.append(",");
				continue;
			}
			if (isString) {
				if (intVal == 0x00) {
					continue;
				}
				ptrVal.append((char)intVal.intValue());
				continue;
			}
			ptrVal.append(intVal);//.append(",");
		}

		ptr.append(ptrVal);
		return ptr.toString();
	}

	private String twoParamOp(String op, String[] tokens, int lineNum) {
		if (tokens.length < 2) {
			return String.format("Error line %d: Parameter count mismatch for op %s", lineNum, op);
		} else if (tokens.length == 2) {
			String[] params = tokens[1].split(Pattern.quote(","));
			if (params.length < 2) {
				return String.format("Error line %d: Parameter count mismatch for op %s", lineNum, op);
			}
			if (params.length == 2)
			{
				return String.format("%s %s %s", op, params[0], params[1]);
			} else {
				return String.format("Error line %d: Parameter count mismatch for op %s", lineNum, op);
			}
		} else if (tokens.length == 3) {
			if (tokens[1].endsWith(",")) {
				String param1 = tokens[1].substring(0, tokens[1].lastIndexOf(','));
				return String.format("%s %s %s", op, param1, tokens[2]);
			} else if (tokens[2].startsWith(",")) {
				String param2 = tokens[2].substring(1);
				return String.format("%s %s %s", op, tokens[1], param2);
			} else {
				return String.format("Error line %d: Expected comma separator between parameters for op %s", lineNum, op);
			}
		} else if (tokens.length == 4) {
			if (tokens[2].equals(",")) {
				return String.format("%s %s %s", op, tokens[1], tokens[3]);
			} else {
				return String.format("Error line %d: Parameter count mismatch for op %s", lineNum, op);
			}
		} else {
			return String.format("Error line %d: Parameter count mismatch for op %s", lineNum, op);
		}
	}

	private String oneParamOp(String op, String[] tokens, int lineNum) {
		if (tokens.length == 2) {
			if (tokens[1].contains(",")) {
				return String.format("Error line %d: Found unexpected comma separator in op %s", lineNum, op);
			} else {
				return String.format("%s %s", op, tokens[1]);
			}
		} else {
			return String.format("Error line %d: Parameter count mismatch for op %s", lineNum, op);
		}
	}

	private String noParamOp(String op, String[] tokens, int lineNum) {
		if (tokens.length == 1) {
			return String.format("%s", op);
		} else {
			return String.format("Error line %d: Parameter count mismatch for op %s", lineNum, op);
		}
	}

	//</editor-fold>
}
