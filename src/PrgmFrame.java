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
import javax.swing.SwingConstants;
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

	private final HashMap<String, Long> _registers = new HashMap<>();
	{
		_registers.put("RSI", 0L);
		_registers.put("RDI", 0L);
		_registers.put("RBP", 0L);
		_registers.put("RSP", 0L);
		_registers.put("RAX", 0L);
		_registers.put("RBX", 0L);
		_registers.put("RCX", 0L);
		_registers.put("RDX", 0L);
		_registers.put("R08", 0L);
		_registers.put("R09", 0L);
		_registers.put("R10", 0L);
		_registers.put("R11", 0L);
		_registers.put("R12", 0L);
		_registers.put("R13", 0L);
		_registers.put("R14", 0L);
		_registers.put("R15", 0L);
		_registers.put("PC", 0L);
	}
	private final HashMap<String, Boolean> _regHighlights = new HashMap<>();
	{
		_regHighlights.put("RSI", false);
		_regHighlights.put("RDI", false);
		_regHighlights.put("RBP", false);
		_regHighlights.put("RSP", false);
		_regHighlights.put("RAX", false);
		_regHighlights.put("RBX", false);
		_regHighlights.put("RCX", false);
		_regHighlights.put("RDX", false);
		_regHighlights.put("R08", false);
		_regHighlights.put("R09", false);
		_regHighlights.put("R10", false);
		_regHighlights.put("R11", false);
		_regHighlights.put("R12", false);
		_regHighlights.put("R13", false);
		_regHighlights.put("R14", false);
		_regHighlights.put("R15", false);
		_regHighlights.put("PC", false);
	}
	private boolean _isEqual = false;
	private boolean _isGreater = false;
	private boolean _cmpHighlight = false;

	private Stack<Long> _stack = new Stack<>();
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

	protected boolean _dirty = false;
	private String _lastSavedState = "";
	private int _lineNumber = 1;
	private boolean _running = false;
	private double _runDelay = 0.0;
	private final long _defaultDelay = 100;

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
	}

	public PrgmFrame(JASM parent, File prgmFile) {
		super();

		_parent = parent;

		_fileName = prgmFile.getName();

		String code = loadFile(prgmFile);

		if (code == null || code.trim().equals("")) {
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
			default:
				_sFormat = "x64";
				_iFormat = 0;
				break;
		}
	}

	//</editor-fold>

	//<editor-fold desc="Init">

	private void buildMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");

		JMenuItem delayMenuItem = new JMenuItem("Set Run Speed");
		//loadMenuItem.setMnemonic(); TODO
		delayMenuItem.addActionListener(event -> {
			JOptionPane.showInputDialog(_parent, String.format("Time to wait between each line of code during run: %.2f seconds", _runDelay), _runDelay);
		});
		fileMenu.add(delayMenuItem);

		JMenuItem saveMenuItem = new JMenuItem("Save");
		//loadMenuItem.setMnemonic(); TODO
		saveMenuItem.addActionListener(event -> saveFile());
		fileMenu.add(saveMenuItem);

		JMenuItem saveAsMenuItem = new JMenuItem("Save As");
		//loadMenuItem.setMnemonic(); TODO
		saveAsMenuItem.addActionListener(event -> Platform.runLater(() -> {
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
		closeMenuItem.addActionListener(event -> _parent.closeTab());
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
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.2;
		gbc.weighty = 1.0;

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
		runButton.addActionListener(event -> run());
		controls.add(runButton, cgbc);

		cgbc.gridx = 1;
		JButton stepButton = new JButton("Step");
		//stepButton.setMnemonic(); TODO
		stepButton.addActionListener(event -> step());
		controls.add(stepButton, cgbc);

		cgbc.gridx = 2;
		JButton pauseButton = new JButton("Pause");
		//pauseButton.setMnemonic(); TODO
		pauseButton.addActionListener(event -> pause());
		controls.add(pauseButton, cgbc);

		cgbc.gridx = 3;
		JButton resetButton = new JButton("Stop/Reset");
		//resetButton.setMnemonic(); TODO
		resetButton.addActionListener(event -> reset());
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

		gbc.gridx = 1;
		gbc.weightx = 0.05;
		_userProgramLines = new JTextArea();
		_userProgramLines.setLineWrap(false);
		_userProgramLines.setEditable(false);
		//_userProgramLines.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		JScrollPane lineScroll = new JScrollPane(_userProgramLines);
		lineScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		lineScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		add(lineScroll, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0.5;
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

		lineScroll.getVerticalScrollBar().addAdjustmentListener(adjustmentEvent -> prgmScroll.getVerticalScrollBar().setValue(lineScroll.getVerticalScrollBar().getValue()));
		prgmScroll.getVerticalScrollBar().addAdjustmentListener(adjustmentEvent -> lineScroll.getVerticalScrollBar().setValue(prgmScroll.getVerticalScrollBar().getValue()));

		updateLines();

		gbc.gridx = 3;
		gbc.weightx = 0.3;
		_prgmOutput = new JTextArea();
		_prgmOutput.setLineWrap(false);
		_prgmOutput.setEditable(false);
		_prgmOutput.setHighlighter(_prgmHl);
		add(scrollTextArea(_prgmOutput), gbc);

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
		if (_userProgram.getText().trim().equals(_lastSavedState.trim())) {
			_dirty = false;
		} else {
			_dirty = true;
		}
	}

	private void updateLines() {
		String[] lines = _userProgram.getText().split(Pattern.quote("\n"));
		String lineNums = "";
		int lineCount = 1;
		for (String line : lines) {
			lineNums += lineCount + "\n";
			lineCount += 1;
		}
		_userProgramLines.setText(lineNums);
	}

	private void setFont(String name, int size) {
		Font f = new Font(name, Font.PLAIN, size);
		_internalData.setFont(f);
		_userProgramLines.setFont(f);
		_userProgram.setFont(f);
		_prgmOutput.setFont(f);
	}

	private String checkRegister(String reg) {
		if (_pointers.containsKey(reg)) {
			return String.format("%s: ptr@%s%n", reg.toLowerCase(), _pointers.get(reg).getName());
		} else {
			return String.format("%s: 0x%016x%n", reg.toLowerCase(), _registers.get(reg));
		}
	}

	private void showRegisters() {
		StringBuilder dat = new StringBuilder();
		dat.append(String.format(" pc: ptr@line#%d%n", _registers.get("PC")));
		dat.append(checkRegister("RAX"));
		dat.append(checkRegister("RBX"));
		dat.append(checkRegister("RCX"));
		dat.append(checkRegister("RDX"));
		dat.append(checkRegister("RSP"));
		dat.append(checkRegister("RBP"));
		dat.append(checkRegister("RSI"));
		dat.append(checkRegister("RDI"));
		dat.append(checkRegister("R08"));
		dat.append(checkRegister("R09"));
		dat.append(checkRegister("R10"));
		dat.append(checkRegister("R11"));
		dat.append(checkRegister("R12"));
		dat.append(checkRegister("R13"));
		dat.append(checkRegister("R14"));
		dat.append(checkRegister("R15"));
		dat.append(String.format("%n%n"));
		for (String pk : _pointers.keySet()) {
			dat.append(String.format("%s: %d%n", pk, _pointers.get(pk)));
		}
		dat.append(String.format("%n%nStack:%n"));
		for (long l : _stack) {
			dat.append(String.format("%d%n", l));
		}
		_datHl.removeAllHighlights();
		_internalData.setText(dat.toString());
	}

	//</editor-fold>

	//<editor-fold desc="File IO">

	private String loadFile(File prgmFile) {
		try (Scanner scan = new Scanner(new FileInputStream(prgmFile))) {
			String code = "";
			while (scan.hasNext()) {
				code += scan.nextLine() + "\n";
			}
			return code;
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
			prgmFile.getParentFile().mkdirs();
			if (!prgmFile.exists())
			{
				prgmFile.createNewFile();
			}
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
			JOptionPane.showMessageDialog(_parent, String.format("Error: Could not create file %s!%n%s", prgmFile.getName(), ioEx.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try (PrintWriter pw = new PrintWriter(new FileOutputStream(prgmFile), true)) {
			pw.write(_userProgram.getText());
		} catch (FileNotFoundException fnfEx) {
			fnfEx.printStackTrace();
			JOptionPane.showMessageDialog(_parent, String.format("Error: Failed to open file %s for writing!%n%s", prgmFile.getName(), fnfEx.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return false;
	}

	private String getExtension(String fileName) {
		if (fileName.trim().equals("")) {
			return null;
		}
		if (fileName.contains(".")) {
			return fileName.substring(fileName.lastIndexOf('.'));
		}
		return null;
	}

	//</editor-fold>

	//<editor-fold desc="Run">

	private void start() {
		_userProgram.setEditable(false);

		String[] lines = _userProgram.getText().split(Pattern.quote("\n"));
		int lineCount = 1;
		for (String line : lines) {
			String[] op = line.trim().split(Pattern.quote("\\s+"));
			//if()
			lineCount += 1;
		}
	}

	private void reset() {
		for (String reg : _registers.keySet()) {
			_registers.put(reg, 0L);
		}
		for (String reg : _regHighlights.keySet()) {
			_regHighlights.put(reg, false);
		}
		_isEqual = false;
		_isGreater = false;
		_cmpHighlight = false;

		_stack.clear();
		_stackHighlight = 0;

		for (String ptr : _pointers.keySet()) {
			_ptrHighlights.put(ptr, false);
		}

		_lineNumber = 1;
		_running = false;

		_userProgram.setEditable(true);
	}

	private void run() {
		_running = true;
	}

	private void step() {
		_running = false;
		_lineNumber += 1;
	}

	private void pause() {
		_running = false;
	}

	private void updateLoop() {
		new Thread(() -> {
			try {
				if (_running)
				{
					nextLine();
					Thread.sleep((long) (_runDelay * 1000));
				}
				else
				{
					Thread.sleep(_defaultDelay);
				}
			} catch (InterruptedException intEx) {

			}
		}).start();
	}

	private void nextLine() {
		String[] lines = _userProgram.getText().split(Pattern.quote("\n"));
		int lineCount = 1;
		for (String line : lines) {
			String[] op = line.trim().split(Pattern.quote("\\s+"));
			switch(op[0]) {
				case "mov":
					break;
				case "add":
					break;
				default:
					break;
			}
			lineCount += 1;
		}
	}

	private long getReg(String reg) {
		return _registers.get(reg.toUpperCase());
	}

	private void setReg(String reg, long value) {
		_registers.put(reg.toUpperCase(), value);
	}

	private boolean isReg(String reg) {
		return _registers.containsKey(reg.toUpperCase());
	}

	private boolean isPtr(String ptr) {
		return _pointers.containsKey(ptr);
	}

	private boolean isLbl(String lbl) {
		return _labels.containsKey(lbl);
	}

	private void mov(String tgt, String value) {

	}

	private void add(String tgt, String value) {

	}

	private void sub(String tgt, String value) {

	}

	private void mul(String tgt) {

	}

	private void div(String tgt) {

	}

	private void inc(String tgt) {

	}

	private void dec(String tgt) {

	}

	private void push(String tgt) {

	}

	private void pop(String tgt) {

	}

	private void call(String tgt) {

	}

	private void ret() {
		long returnPtr = _stack.pop();

	}

	private void intr(int value) {

	}

	private void cmp(String tgt, String value) {

	}

	private void jmp(String tgt) {

	}

	private void jeq(String tgt) {

	}

	private void jg(String tgt) {

	}

	private void jge(String tgt) {

	}

	private void jne(String tgt) {

	}

	private void jl(String tgt) {

	}

	private void jle(String tgt) {

	}

	//</editor-fold>
}
