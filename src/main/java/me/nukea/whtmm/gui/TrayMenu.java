package me.nukea.whtmm.gui;


import me.nukea.whtmm.audio.MicrophoneOption;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.function.Consumer;

public final class TrayMenu {
	private final Consumer<Boolean> toggleAction;
	private final Consumer<MicrophoneOption> microphoneAction;
	private final Runnable refreshAction;
	private final Runnable exitAction;
	
	private final TrayIcon trayIcon;
	private final JPopupMenu popupMenu = new JPopupMenu();
	
	private final JCheckBoxMenuItem enabledItem = new JCheckBoxMenuItem("Enable audio mixer");
	
	private final JMenu microphoneMenu = new JMenu("Microphone");
	
	private JWindow popupAnchor;
	private boolean disposed;
	
	private final AWTEventListener outsideClickListener = event -> {
		if (!(event instanceof MouseEvent mouseEvent)
		    || mouseEvent.getID() != MouseEvent.MOUSE_PRESSED) {
			return;
		}
		
		Component component = mouseEvent.getComponent();
		
		if (component == null) {
			return;
		}
		
		boolean insidePopup =
				SwingUtilities.isDescendingFrom(component, popupMenu);
		
		boolean insideAnchor =
				popupAnchor != null
				&& (component == popupAnchor
				    || SwingUtilities.isDescendingFrom(
						component,
						popupAnchor
				));
		
		if (!insidePopup && !insideAnchor) {
			SwingUtilities.invokeLater(() ->
					                           popupMenu.setVisible(false)
			);
		}
	};
	
	public TrayMenu(Consumer<Boolean> toggleAction, Consumer<MicrophoneOption> microphoneAction, Runnable refreshAction, Runnable exitAction) {
		this.toggleAction = toggleAction;
		this.microphoneAction = microphoneAction;
		this.refreshAction = refreshAction;
		this.exitAction = exitAction;
		
		if (!SystemTray.isSupported()) {
			throw new UnsupportedOperationException(
					"System tray is not supported"
			);
		}
		
		buildMenu();
		
		trayIcon = new TrayIcon(
				loadIcon(false),
				"What Happened To My Microphone"
		);
		
		trayIcon.setImageAutoSize(true);
		
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {
				int button = event.getButton();
				
				if (button == MouseEvent.BUTTON1
				    || button == MouseEvent.BUTTON3) {
					showPopupMenu();
				}
			}
		});
		
		try {
			SystemTray.getSystemTray().add(trayIcon);
		} catch (AWTException exception) {
			throw new IllegalStateException(
					"Could not add the tray icon",
					exception
			);
		}
	}
	
	private void buildMenu() {
		enabledItem.addActionListener(_ -> toggleAction.accept(enabledItem.isSelected()));
		
		JMenuItem refreshItem = new JMenuItem("Refresh microphones");
		
		refreshItem.addActionListener(_ -> refreshAction.run());
		
		JMenuItem exitItem = new JMenuItem("Exit");
		
		exitItem.addActionListener(_ -> exitAction.run());
		
		popupMenu.add(microphoneMenu);
		popupMenu.addSeparator();
		popupMenu.add(enabledItem);
		popupMenu.add(refreshItem);
		popupMenu.addSeparator();
		popupMenu.add(exitItem);
		
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
				Toolkit.getDefaultToolkit().addAWTEventListener(
						outsideClickListener,
						AWTEvent.MOUSE_EVENT_MASK
				);
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
				removeOutsideClickListener();
				disposePopupAnchor();
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent event) {
				removeOutsideClickListener();
				disposePopupAnchor();
			}
		});
	}
	
	private void showPopupMenu() {
		if (disposed || popupMenu.isVisible()) {
			return;
		}
		
		Point pointer = MouseInfo.getPointerInfo().getLocation();
		
		SwingUtilities.invokeLater(() -> {
			if (disposed || popupMenu.isVisible()) {
				return;
			}
			
			disposePopupAnchor();
			
			popupAnchor = new JWindow();
			popupAnchor.setAlwaysOnTop(true);
			popupAnchor.setSize(1, 1);
			popupAnchor.setLocation(pointer);
			popupAnchor.setVisible(true);
			
			popupMenu.show(
					popupAnchor.getContentPane(),
					0,
					0
			);
		});
	}
	
	public void setMicrophones(List<MicrophoneOption> microphones, MicrophoneOption selected) {
		microphoneMenu.removeAll();
		
		ButtonGroup group = new ButtonGroup();
		
		for (MicrophoneOption microphone : microphones) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(microphone.name(), microphone.equals(selected));
			
			group.add(item);
			microphoneMenu.add(item);
			
			item.addActionListener(_ -> microphoneAction.accept(microphone));
		}
		
		if (microphones.isEmpty()) {
			JMenuItem unavailable = new JMenuItem("No microphones found");
			
			unavailable.setEnabled(false);
			microphoneMenu.add(unavailable);
		}
	}
	
	public void setRunning(boolean running) {
		enabledItem.setSelected(running);
		trayIcon.setImage(loadIcon(running));
		trayIcon.setToolTip("What Happened To My Mic");
	}
	
	public void showError(String message) {
		trayIcon.displayMessage(
				"What Happened To My Mic?",
				message == null ? "An unknown error occurred." : message,
				TrayIcon.MessageType.ERROR
		);
	}
	
	public void dispose() {
		if (disposed) {
			return;
		}
		
		disposed = true;
		
		popupMenu.setVisible(false);
		removeOutsideClickListener();
		disposePopupAnchor();
		
		SystemTray.getSystemTray().remove(trayIcon);
	}
	
	private void removeOutsideClickListener() {
		Toolkit.getDefaultToolkit().removeAWTEventListener(outsideClickListener);
	}
	
	private void disposePopupAnchor() {
		if (popupAnchor != null) {
			popupAnchor.dispose();
			popupAnchor = null;
		}
	}
	
	private Image loadIcon(boolean running) {
		String resource = running ? "/icon/equalizer-on.png" : "/icon/equalizer-off.png";
		
		try (InputStream stream = TrayMenu.class.getResourceAsStream(resource)) {
			if (stream == null) throw new IllegalStateException("Missing resource: " + resource);
			
			Image image = ImageIO.read(stream);
			
			if (image == null) throw new IllegalStateException("Invalid image resource: " + resource);
			
			return image;
		} catch (IOException e) {
			throw new IllegalStateException("Could not load tray icon: " + resource, e);
		}
	}
}