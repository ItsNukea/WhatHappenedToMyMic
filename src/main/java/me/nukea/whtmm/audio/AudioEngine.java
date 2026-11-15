package me.nukea.whtmm.audio;

import java.util.Objects;

public final class AudioEngine {
	private boolean running;
	
	public void start(MicrophoneOption microphone) {
		Objects.requireNonNull(microphone, "No microphone selected");
		
		if (running) {
			return;
		}
		
		// TODO: Open the selected TargetDataLine (or the system default input).
		// TODO: Start capture and connect it to the audio-processing pipeline.
		
		running = true;
	}
	
	public void stop() {
		if (!running) {
			return;
		}
		
		// TODO: Stop the capture/processing pipeline and close its audio lines.
		
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
}