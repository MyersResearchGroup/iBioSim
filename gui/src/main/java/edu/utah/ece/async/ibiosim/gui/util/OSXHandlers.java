package edu.utah.ece.async.ibiosim.gui.util;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.lang3.JavaVersion;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;

import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.util.preferences.PreferencesDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;

public class OSXHandlers implements InvocationHandler {
	
	Gui gui;
			
	public OSXHandlers(Gui gui) {
		this.gui = gui;
	}

	public void addEventHandlers() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
	InvocationTargetException, InstantiationException {

		// using reflection to avoid Mac specific classes being required for compiling KSE on other platforms
		Class<?> applicationClass = Class.forName("com.apple.eawt.Application");
		Class<?> quitHandlerClass;
		Class<?> aboutHandlerClass;
		Class<?> openFilesHandlerClass;
		Class<?> preferencesHandlerClass;
		
		String version = System.getProperty("java.version");
		String[] versionElements = version.split("\\.|_|-b");
		
		if (Double.parseDouble(versionElements[0]) >= 9) {
			quitHandlerClass = Class.forName("java.awt.desktop.QuitHandler");
			aboutHandlerClass = Class.forName("java.awt.desktop.AboutHandler");
			openFilesHandlerClass = Class.forName("java.awt.desktop.OpenFilesHandler");
			preferencesHandlerClass = Class.forName("java.awt.desktop.PreferencesHandler");
		} else {
			quitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");
			aboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");
			openFilesHandlerClass = Class.forName("com.apple.eawt.OpenFilesHandler");
			preferencesHandlerClass = Class.forName("com.apple.eawt.PreferencesHandler");    
		}

		Object application = applicationClass.getConstructor((Class[]) null).newInstance((Object[]) null);
		Object proxy = Proxy.newProxyInstance(OSXHandlers.class.getClassLoader(), new Class<?>[]{
			quitHandlerClass, aboutHandlerClass, openFilesHandlerClass, preferencesHandlerClass}, this);

		applicationClass.getDeclaredMethod("setQuitHandler", quitHandlerClass).invoke(application, proxy);
		applicationClass.getDeclaredMethod("setAboutHandler", aboutHandlerClass).invoke(application, proxy);
		applicationClass.getDeclaredMethod("setOpenFileHandler", openFilesHandlerClass).invoke(application, proxy);
		applicationClass.getDeclaredMethod("setPreferencesHandler", preferencesHandlerClass).invoke(application,
				proxy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		/*if ("openFiles".equals(method.getName())) {
			if (args[0] != null) {
				Object files = args[0].getClass().getMethod("getFiles").invoke(args[0]);
				if (files instanceof List) {
					OpenAction openAction = new OpenAction(kseFrame);
					for (File file : (List<File>) files) {
						openAction.openKeyStore(file);
					}
				}
			}
		} else */if ("handleQuitRequestWith".equals(method.getName())) {
			gui.exit();
			// If we have returned from the above call the user has decied not to quit
			if (args[1] != null) {
				args[1].getClass().getDeclaredMethod("cancelQuit").invoke(args[1]);
			}
		} else if ("handleAbout".equals(method.getName())) {
			gui.about();
		} else if ("handlePreferences".equals(method.getName())) {
			PreferencesDialog.showPreferences(Gui.frame);
			//EditPreferences editPreferences = new EditPreferences(frame, async);
			//editPreferences.preferences();
			gui.getFileTree().setExpandibleIcons(!IBioSimPreferences.INSTANCE.isPlusMinusIconsEnabled());
			if (gui.getSBOLDocument() != null) {
				gui.getSBOLDocument().setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
			}
		}
		return null;
	}

}
