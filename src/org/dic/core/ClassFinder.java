package org.dic.core;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Finds and loads classes in given packages and sub packages
 * @author Sarath
 *
 */
public class ClassFinder {

	private List<String> classNames = new ArrayList<>();	
	private List<Class<?>> classes = new LinkedList<>();	

	public ClassFinder(String packageName) {
		init(packageName);
	}

	/**
	 * Initializes 
	 */
	private void init(String packageName) {		
		URL root = Thread.currentThread().getContextClassLoader()
				.getResource(packageName.replace(".", "/"));	
		feedClassNames(new File(root.getFile()), packageName);		
		feedClasses();
	}

	/**
	 * Feed the class names to the list
	 * @param file
	 * @param packageName
	 */
	private void feedClassNames(File file, String packageName) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				if(files[i].isDirectory())
					feedClassNames(files[i], packageName + "." + files[i].getName());
				else 
					feedClassNames(files[i], packageName);
			}
		} else {
			if (file.getName().endsWith(".class")) {								
				String className = packageName + "."
						+ file.getName().replaceAll(".class", "");							
				classNames.add(className);
			}
		}
	}

	/**
	 * Feeds the classes to the list
	 */
	public void feedClasses() {
		for(String className : classNames) {
			try {
				getClasses().add(Class.forName(className));
			} catch (ClassNotFoundException e) {				
				e.printStackTrace();
			}
		}
	}

	//Getters and Setters

	public List<Class<?>> getClasses() {
		return classes;
	}

	public void setClasses(List<Class<?>> classes) {
		this.classes = classes;
	}	
	
}
