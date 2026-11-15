package me.nukea.whtmm.app;

public class AppState {
	private static final AppState INSTANCE = new AppState();
	
	private volatile boolean processingEnabled;
	
	private AppState() {}
	
	public static AppState getInstance() {
		return INSTANCE;
	}
	
	public boolean isProcessingEnabled() {
		return processingEnabled;
	}
	
	public void setProcessingEnabled(boolean enabled) {
		processingEnabled = enabled;
	}
}