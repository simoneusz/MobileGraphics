package com.mygdx.hellobox;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mygdx.hellobox.HelloBox;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {

	public static void main (String[] arg) {
		int width = 720;
		int height = 480;

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setWindowedMode(width, height);
		config.useVsync(true);
		config.setForegroundFPS(60);
		new Lwjgl3Application(new HelloBox(), config);
	}
}
