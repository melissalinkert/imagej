/* -*- mode: java; c-basic-offset: 8; indent-tabs-mode: t; tab-width: 8 -*- */

package ij.plugin;

import ij.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

/** This is a plugin that provides an easy user interface to finding
    commands you might know the name of without having to go through
    all the menus.  If you type a part of a command name, the box
    below will only show commands that match that substring (case
    insensitively). */

public class CommandFinder implements PlugIn, TextListener, ActionListener, WindowListener, KeyListener, ItemListener {

	class CommandAction {
		CommandAction(String classCommand, MenuItem menuItem, String menuLocation) {
			this.classCommand = classCommand;
			this.menuItem = menuItem;
			this.menuLocation = menuLocation;
		}
		String classCommand;
		MenuItem menuItem;
		String menuLocation;
		public String toString() {
			return "classCommand: " + classCommand + ", menuItem: "+menuItem+", menuLocation: "+menuLocation;
		}
	}

	Dialog d;
	TextField prompt;
	List completions;
	Button runButton;
	Button cancelButton;
	Checkbox fullInfoCheckbox;
	Hashtable commandsHash;
	String [] commands;
	Hashtable listLabelToCommand;

	protected String makeListLabel(String command, CommandAction ca, boolean fullInfo) {
		if (fullInfo) {
			String result = command;
			if( ca.menuLocation != null)
				result += " (in " + ca.menuLocation + ")";
			if( ca.classCommand != null )
				result += " [" + ca.classCommand + "]";
			return result;
		} else {
			return command;
		}
	}

	protected void populateList(String matchingSubstring) {
		boolean fullInfo=fullInfoCheckbox.getState();
		String substring = matchingSubstring.toLowerCase();
		completions.removeAll();
		for(int i=0; i<commands.length; ++i) {
			String commandName = commands[i];
			if (commandName.length()==0)
				continue;
			String lowerCommandName = commandName.toLowerCase();
			if( lowerCommandName.indexOf(substring) >= 0 ) {
				CommandAction ca = (CommandAction)commandsHash.get(commandName);
				String listLabel = makeListLabel(commandName, ca, fullInfo);
				completions.add(listLabel);
			}
		}
	}

	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if (source==runButton) {
			String selected = completions.getSelectedItem();
			if(selected==null) {
				IJ.error("You must select a plugin to run");
				return;
			}
			runFromLabel(selected);
		} else if (source == cancelButton) {
			d.dispose();
		}
	}

	public void itemStateChanged(ItemEvent ie) {
		populateList(prompt.getText());
	}

	protected void runFromLabel(String listLabel) {
		String command = (String)listLabelToCommand.get(listLabel);
		CommandAction ca = (CommandAction)commandsHash.get(command);
		if (ca.classCommand != null ) {
			IJ.showStatus("Running command "+ca.classCommand);
			IJ.doCommand(command);
		} else if (ca.menuItem != null) {
			IJ.showStatus("Clicking menu item "+ca.menuLocation+" > "+command);
			ActionEvent ae = new ActionEvent(ca.menuItem, ActionEvent.ACTION_PERFORMED, command);
			ActionListener [] als = ca.menuItem.getActionListeners();
			for (int i=0; i<als.length; ++i)
				als[i].actionPerformed(ae);
		} else {
			IJ.error("BUG: nothing to run found for '"+listLabel+"'");
			return;
		}
		d.dispose();
	}

	public void keyPressed(KeyEvent ke) {
		int key = ke.getKeyCode();
		int items = completions.getItemCount();
		Object source = ke.getSource();
		if (source==prompt) {
			if (key==KeyEvent.VK_ENTER) {
				if (1==items) {
					String selected = completions.getItem(0);
					runFromLabel(selected);
				}
			} else if (key==KeyEvent.VK_UP) {
				completions.requestFocus();
				if(items>0)
					completions.select(completions.getItemCount()-1);
			} else if (key==KeyEvent.VK_DOWN)  {
				completions.requestFocus();
				if (items>0)
					completions.select(0);
			}
		} else if (source==completions) {
			if (key==KeyEvent.VK_ENTER) {
				String selected = completions.getSelectedItem();
				if (selected!=null)
					runFromLabel(selected);
			}
		}
	}

	public void keyReleased(KeyEvent ke) { }

	public void keyTyped(KeyEvent ke) { }

	public void textValueChanged(TextEvent te) {
		populateList(prompt.getText());
	}

	public void parseMenu(String path, Menu menu) {
		int n=menu.getItemCount();
		for (int i=0; i<n; ++i) {
			MenuItem m=menu.getItem(i);
			String label=m.getLabel();
			if (m instanceof Menu) {
				Menu subMenu=(Menu)m;
				parseMenu(path+" > "+label,subMenu);
			} else {
				String trimmedLabel = label.trim();
				if (trimmedLabel.length()==0 || trimmedLabel.equals("-"))
					continue;
				CommandAction ca=(CommandAction)commandsHash.get(label);
				if( ca == null )
					commandsHash.put(label, new CommandAction(null,m,path));
				else {
					ca.menuItem=m;
					ca.menuLocation=path;
				}
				CommandAction caAfter=(CommandAction)commandsHash.get(label);
			}
		}
	}

	public void findAllMenuItems() {
		MenuBar menuBar = Menus.getMenuBar();
		int topLevelMenus = menuBar.getMenuCount();
		for (int i=0; i<topLevelMenus; ++i) {
			Menu topLevelMenu=menuBar.getMenu(i);
			parseMenu(topLevelMenu.getLabel(), topLevelMenu);
		}
	}

	public void run(String ignored) {

		commandsHash = new Hashtable();

		Hashtable realCommandsHash = ij.Menus.getCommands();

		Set realCommandSet = realCommandsHash.keySet();

		for (Iterator i = realCommandSet.iterator();
		     i.hasNext();) {
			String command = (String)i.next();
			// Some of these are whitespace only or separators - ignore them:
			String trimmedCommand = command.trim();
			if (trimmedCommand.length()>0 && !trimmedCommand.equals("-")) {
				commandsHash.put(command,
						 new CommandAction((String)realCommandsHash.get(command),
								   null,
								   null));
			}
		}

		// There are some menu items that don't have commands
		// associated, such as those added by RefreshScripts,
		// so look through all the menus as well:

		findAllMenuItems();

		commands = (String[])commandsHash.keySet().toArray(new String[0]);
		Arrays.sort(commands);

		listLabelToCommand = new Hashtable();

		for (int i=0; i<commands.length; ++i) {
			CommandAction ca = (CommandAction)commandsHash.get(commands[i]);
			listLabelToCommand.put(makeListLabel(commands[i], ca, true), commands[i]);
			listLabelToCommand.put(makeListLabel(commands[i], ca, false), commands[i]);
		}

		ImageJ imageJ = IJ.getInstance();

		d = new Dialog(imageJ, "Command Finder");
		d.setLayout(new BorderLayout());
		d.addWindowListener(this);

		fullInfoCheckbox = new Checkbox(
			"Show full information for each command",
			false);
		fullInfoCheckbox.addItemListener(this);

		Panel northPanel = new Panel();

		northPanel.add(new Label("Type part of a command:"));

		prompt = new TextField("", 30);
		prompt.addTextListener(this);
		prompt.addKeyListener(this);

		northPanel.add(prompt);

		d.add(northPanel, BorderLayout.NORTH);

		completions = new List(20);
		completions.addKeyListener(this);
		populateList("");

		d.add(completions, BorderLayout.CENTER);

		runButton = new Button("Run");
		cancelButton = new Button("Cancel");

		runButton.addActionListener(this);
		cancelButton.addActionListener(this);

		Panel southPanel = new Panel();
		southPanel.setLayout(new BorderLayout());

		Panel optionsPanel = new Panel();
		optionsPanel.add(fullInfoCheckbox);

		Panel buttonsPanel = new Panel();
		buttonsPanel.add(runButton);
		buttonsPanel.add(cancelButton);

		southPanel.add(optionsPanel, BorderLayout.CENTER);
		southPanel.add(buttonsPanel, BorderLayout.SOUTH);

		d.add(southPanel, BorderLayout.SOUTH);

		int offsetX = 38;
		int offsetY = 84;

		Point pos=imageJ.getLocationOnScreen();

		// Move the dialog to slightly offset from the main ImageJ
		// window:
		d.setLocation((int)pos.getX()+38, (int)pos.getY()+84);

		d.pack();
		d.setVisible(true);

	}

	public void windowClosing(WindowEvent e) {
		d.dispose();
	}

	public void windowActivated(WindowEvent e) { }
	public void windowDeactivated(WindowEvent e) { }
	public void windowClosed(WindowEvent e) { }
	public void windowOpened(WindowEvent e) { }
	public void windowIconified(WindowEvent e) { }
	public void windowDeiconified(WindowEvent e) { }
}
