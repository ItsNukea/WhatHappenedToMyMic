package me.nukea.whtmm.audio;

import javax.sound.sampled.*;
import java.util.*;

public final class MicrophoneManager {
	private static final DataLine.Info INPUT_LINE_INFO = new DataLine.Info(TargetDataLine.class, null);
	
	private final List<MicrophoneOption> microphones = new ArrayList<>();
	private MicrophoneOption selectedMicrophone = MicrophoneOption.defaultDevice();
	
	public MicrophoneManager() {
		refresh();
	}
	
	public void refresh() {
		MicrophoneOption previousSelection = selectedMicrophone;
		microphones.clear();
		
		// The default option is separate from the individual mixer entries.
		microphones.add(MicrophoneOption.defaultDevice());
		
		for (Mixer.Info info : AudioSystem.getMixerInfo()) {
			// This Windows alias represents the system default input.
			if (info.getName().equalsIgnoreCase("Primary Sound Capture Driver")) {
				continue;
			}
			
			try {
				Mixer mixer = AudioSystem.getMixer(info);
				
				if (mixer.isLineSupported(INPUT_LINE_INFO)) {
					microphones.add(new MicrophoneOption(info.getName(), info));
				}
			} catch (RuntimeException ignored) {
				// Some providers expose mixer entries that cannot be queried.
			}
		}
		
		selectedMicrophone = microphones.stream()
				                     .filter(option -> option.equals(previousSelection))
				                     .findFirst()
				                     .orElse(MicrophoneOption.defaultDevice());
	}
	
	public List<MicrophoneOption> getMicrophones() {
		return List.copyOf(microphones);
	}
	
	public MicrophoneOption getSelectedMicrophone() {
		return selectedMicrophone;
	}
	
	public void setSelectedMicrophone(MicrophoneOption microphone) {
		if (microphone == null || !microphones.contains(microphone)) {
			throw new IllegalArgumentException(
					"Unsupported microphone selection"
			);
		}
		
		selectedMicrophone = microphone;
	}
}