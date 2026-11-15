package me.nukea.whtmm.audio;

import javax.sound.sampled.Mixer;
import java.util.Objects;

public record MicrophoneOption(String name, Mixer.Info mixerInfo) {
	private static final MicrophoneOption DEFAULT = new MicrophoneOption("Default", null);
	
	public static MicrophoneOption defaultDevice() {
		return DEFAULT;
	}
	
	public boolean isDefault() {
		return mixerInfo == null;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if(other instanceof MicrophoneOption option) return Objects.equals(mixerInfo, option.mixerInfo);
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(mixerInfo);
	}
	
	@Override
	public String toString() {
		return name;
	}
}