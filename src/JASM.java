import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;

public class JASM extends JFrame
{
	//<editor-fold desc="Member Vars">

	public static final int NASM_FORMAT = 1;
	public static final int GCC_FORMAT = 2;

	private final String _hwNasmPrgm = """
			section .data
			hw_msg db 'Hello world!', 0x0a, 0x00
			hw_len equ $ - hw_msg
			
			section .text
			global _start
			
			_start:
			mov rdx, hw_len
			mov rcx, hw_msg
			mov rbx, 1
			mov rax, 4
			int 0x80
			
			mov rax, 1
			int 0x80
			
			""";

	private final String _hwGccPrgm = """
			"""; // TODO

	private int _newCount = 1;

	private final JTabbedPane _tabs = new JTabbedPane();

	private final HashMap<Container, PrgmFrame> _prgms = new HashMap<>();

	//</editor-fold>

	//<editor-fold desc="Constructors">

	public JASM() {
		super("Java x64 Assembly Interpreter");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setExtendedState(MAXIMIZED_BOTH);
		//setUndecorated(true);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				exit();
			}
		});

		_tabs.addChangeListener(_ -> {
			_tabs.revalidate();
			_tabs.repaint();
		});
		add(_tabs);

		buildMenu();

		setVisible(true);
		requestFocus();
	}

	//</editor-fold>

	//<editor-fold desc="Init">

	private void buildMenu() {
		JMenuBar menuBar = new JMenuBar();

		JMenu mainMenu = new JMenu("JASM");

		JMenu newPrgmMenu = new JMenu("New Program");

		JMenu newNasmMenu = new JMenu("NASM-Format File");

		JMenuItem newBlankNasmMenuItem = new JMenuItem("Blank Template");
		//loadMenuItem.setMnemonic(); TODO
		newBlankNasmMenuItem.addActionListener(_ -> {
			PrgmFrame prgm = new PrgmFrame(this, "", NASM_FORMAT);
			addTab(prgm);
		});
		newNasmMenu.add(newBlankNasmMenuItem);

		JMenuItem newHwNasmMenuItem = new JMenuItem("Hello World Template");
		//loadMenuItem.setMnemonic(); TODO
		newHwNasmMenuItem.addActionListener(_ -> {
			PrgmFrame prgm = new PrgmFrame(this, _hwNasmPrgm, NASM_FORMAT);
			addTab(prgm);
		});
		newNasmMenu.add(newHwNasmMenuItem);

		newPrgmMenu.add(newNasmMenu);

		JMenu newGccMenu = new JMenu("GCC-Format File");

		JMenuItem newBlankGccMenuItem = new JMenuItem("Blank Template");
		//loadMenuItem.setMnemonic(); TODO
		newBlankGccMenuItem.addActionListener(_ -> {
			PrgmFrame prgm = new PrgmFrame(this, "", GCC_FORMAT);
			addTab(prgm);
		});
		newNasmMenu.add(newBlankGccMenuItem);

		JMenuItem newHwGccMenuItem = new JMenuItem("Hello World Template");
		//loadMenuItem.setMnemonic(); TODO
		newHwGccMenuItem.addActionListener(_ -> {
			PrgmFrame prgm = new PrgmFrame(this, _hwGccPrgm, GCC_FORMAT);
			addTab(prgm);
		});
		newNasmMenu.add(newHwGccMenuItem);

		newGccMenu.setEnabled(false); // TODO
		newPrgmMenu.add(newGccMenu);

		mainMenu.add(newPrgmMenu);

		JMenuItem loadMenuItem = new JMenuItem("Load Program");
		//loadMenuItem.setMnemonic(); TODO
		loadMenuItem.addActionListener(_ -> Platform.runLater(() -> {
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Load x64 Program From File");
			chooser.getExtensionFilters().addAll(
					new FileChooser.ExtensionFilter("NASM-Format Files", "*.asm"),
					new FileChooser.ExtensionFilter("GCC_Format Files", "*.s")
			);
			List<File> files = chooser.showOpenMultipleDialog(null);

			for (File prgmFile : files) {
				PrgmFrame prgm = new PrgmFrame(this, prgmFile);
				addTab(prgm);
			}
		}));
		mainMenu.add(loadMenuItem);

		mainMenu.addSeparator();

		JMenuItem closeMenuItem = new JMenuItem("Close JASM");
		//closeMenuItem.setMnemonic(); TODO
		closeMenuItem.addActionListener(_ -> exit());
		mainMenu.add(closeMenuItem);

		menuBar.add(mainMenu);

		/*JMenu asmMenu = new JMenu();

		JMenuItem addPtrMenuItem = new JMenuItem("Add Pointer Data");
		//addPtrMenuItem.setMnemonic(); TODO
		addPtrMenuItem.addActionListener(event -> {
			// TODO
		});
		asmMenu.add(addPtrMenuItem);

		JMenuItem addFncMenuItem = new JMenuItem("Add Function");
		//addFncMenuItem.setMnemonic(); TODO
		addFncMenuItem.addActionListener(event -> {
			// TODO
		});
		asmMenu.add(addFncMenuItem);

		menuBar.add(asmMenu);*/

		setJMenuBar(menuBar);
	}

	public void exit() {
		for (Component cmp : _tabs.getComponents()) {
			PrgmFrame prgm = _prgms.get(cmp);

			if (!closeTab(prgm)) {
				return;
			}

		}
		System.exit(0);
	}

	//</editor-fold>

	//<editor-fold desc="Tabs">

	private boolean addTab(PrgmFrame prgm) {
		try {
			String name = prgm._fileName != null ? prgm._fileName : String.format("new_%d", _newCount++);
			prgm._fileName = name;
			_tabs.addTab(name, prgm.getRootPane());
			_prgms.put(prgm.getRootPane(), prgm);

			revalidate();
			repaint();

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, String.format("Error: Failed to add tab to viewer!%n%s", ex.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public boolean closeTab() {
		Component cmp = _tabs.getSelectedComponent();
		if (cmp == null) {
			return false;
		}
		return closeTab(_prgms.get(cmp));
	}


	public boolean closeTab(PrgmFrame prgm) {
		if (_tabs.getTabCount() == 0) {
			return false;
		}

		if (prgm._dirty) {
			int choice = JOptionPane.showConfirmDialog(this, String.format("File %s has unsaved changes.  Save before closing?", prgm._fileName), "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			switch (choice) {
				case JOptionPane.YES_OPTION:
					prgm.saveFile();
					break;
				case JOptionPane.CANCEL_OPTION:
					return false;
				default:
					break;
			}
		}

		_tabs.remove(prgm.getRootPane());
		_prgms.remove(prgm.getRootPane());

		revalidate();
		repaint();

		return true;
	}

	//</editor-fold>

	public static void main(String[] args) {
		new JASM();
		new JFXPanel();
	}

}
