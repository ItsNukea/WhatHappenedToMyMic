package me.nukea.whtmm;

import me.nukea.whtmm.app.TrayApplication;
import javax.swing.*;

public class Main {
	static void main() {
		SwingUtilities.invokeLater(TrayApplication::new);
	}
}