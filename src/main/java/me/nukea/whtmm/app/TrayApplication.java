package me.nukea.whtmm.app;

import me.nukea.whtmm.audio.*;
import me.nukea.whtmm.gui.TrayMenu;

import javax.sound.sampled.Mixer;

public final class TrayApplication {
	private final MicrophoneManager microphoneManager;
	private final AudioEngine audioEngine;
	private final TrayMenu trayMenu;
	
	private boolean exiting;
	
	public TrayApplication() {
		microphoneManager = new MicrophoneManager();
		audioEngine = new AudioEngine();
		
		trayMenu = new TrayMenu(
				this::setProcessingEnabled,
				this::selectMicrophone,
				this::refreshMicrophones,
				this::exit
		);
		
		refreshMicrophones();
		trayMenu.setRunning(false);
	}
	
	private void setProcessingEnabled(boolean enabled) {
		if (exiting) {
			return;
		}
		
		try {
			if (enabled) {
				audioEngine.start(
						microphoneManager.getSelectedMicrophone()
				);
			} else {
				audioEngine.stop();
			}
		} catch (Exception exception) {
			audioEngine.stop();
			
			trayMenu.showError(
					"Could not change mixer state: "
					+ exception.getMessage()
			);
		}
		
		trayMenu.setRunning(audioEngine.isRunning());
	}
	
	private void selectMicrophone(MicrophoneOption microphone) {
		if (exiting) {
			return;
		}
		
		boolean wasRunning = audioEngine.isRunning();
		
		if (wasRunning) {
			audioEngine.stop();
		}
		
		microphoneManager.setSelectedMicrophone(microphone);
		
		if (wasRunning) {
			try {
				audioEngine.start(microphone);
			} catch (Exception exception) {
				audioEngine.stop();
				
				trayMenu.showError(
						"Could not switch microphone: "
						+ exception.getMessage()
				);
			}
		}
		
		trayMenu.setRunning(audioEngine.isRunning());
	}
	
	private void refreshMicrophones() {
		microphoneManager.refresh();
		
		trayMenu.setMicrophones(
				microphoneManager.getMicrophones(),
				microphoneManager.getSelectedMicrophone()
		);
	}
	
	private void exit() {
		if (exiting) {
			return;
		}
		
		exiting = true;
		
		audioEngine.stop();
		trayMenu.dispose();
		
		System.exit(0);
	}
}