package org.dic.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dic.core.annotation.Bean;
import org.dic.core.annotation.Configuration;
import org.dic.core.annotation.Inject;
import org.dic.core.annotation.Instance;

/**
 * Creates Instances 
 * @author Sarath
 *
 */
public class InstanceFactory {

	private String packageToScan = "com";
	private ClassFinder classFinder;
	private List<Class<?>> configurationClasses = new LinkedList<Class<?>>();
	private Map<String, Class<?>> instanceClasses = new HashMap<String, Class<?>>();
	private Map<String, Method> configurationBeans = new HashMap<String, Method>();
	private Map<String, Object> instances = new HashMap<String, Object>();

	/**
	 * Instantiating this constructor causes the packageToScan to
	 *  be "com" by Default
	 */
	public InstanceFactory() {
		classFinder = new ClassFinder(getPackageToScan());
		init();
	}
	
	/**
	 * Use this constructor to set the package that are to be scanned by the 
	 * container
	 * @param packageToScan
	 */
	public InstanceFactory(String packageToScan) {
		setPackageToScan(packageToScan);
		classFinder = new ClassFinder(getPackageToScan());
		init();
	}

	/**
	 * Calls methods feedClasses() and feedConfigurationBeans()
	 */
	private void init() {		
		feedClasses();
		feedConfigurationBeans();		
	}

	/**
	 * Iterated through the classes fetched by ClassFinder finds
	 * the annotations on them then feeds the annotations and class to feedClass()
	 */
	private void feedClasses() {
		for (Class<?> c : classFinder.getClasses()) {
			Annotation[] annotations = c.getAnnotations();
			feedClass(annotations, c);
		}
	}

	/**
	 * Iterated through the annotations and stores class to appropriate locations
	 * @param annotations
	 * @param c
	 */
	private void feedClass(Annotation[] annotations, Class<?> c) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof Configuration) {				
				configurationClasses.add(c);
			} else if (annotation instanceof Instance) {
				Instance instance = (Instance) annotation;
				String instanceName = instance.name();
				instanceClasses.put(instanceName, c);
			}
		}
	}

	/**
	 * Iterates through Configuration Classes and feeds them one by one to 
	 * feedConfigurationBean()
	 */
	private void feedConfigurationBeans() {
		for (Class<?> configuration : configurationClasses) {			
			feedConfigurationBean(configuration);
		}
	}

	/**
	 * Fetches the methods annotated with Bean and stores them
	 * @param c
	 */
	private void feedConfigurationBean(Class<?> c) {
		Method[] methods = c.getDeclaredMethods();
		for (Method method : methods) {
			Annotation annotation = method.getAnnotation(Bean.class);
			if (annotation != null) {
				Bean bean = (Bean) annotation;
				String beanName = bean.name();
				configurationBeans.put(beanName, method);
			}
		}
	}

	/**
	 * Fetches the instance requested
	 * @param instanceName
	 * @return
	 */
	public Object getInstance(String instanceName) {
		Object instance = instances.get(instanceName);
		if(instance == null) {
			if (configurationBeans.containsKey(instanceName)) {
				return getInstanceFromConfiguration(instanceName);
			} else {
				return getInstanceFromAnnotatedClasses(instanceName);
			}
		} else {
			return instance;
		}
	}

	/**
	 * Get instance from the configuration class (from methods Annotated with Bean)
	 * @param instanceName
	 * @return
	 */
	private Object getInstanceFromConfiguration(String instanceName) {
		Object returnValue = null;
		Method method = configurationBeans.get(instanceName);
		try {
			Object object = method.getDeclaringClass().newInstance();
			returnValue = method.invoke(object, null);
			instances.put(instanceName, returnValue);
			return returnValue;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return returnValue;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return returnValue;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return returnValue;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return returnValue;
		}
	}

	/**
	 * Instance of class annotated with Instance annotation
	 * @param instanceName
	 * @return
	 */
	private Object getInstanceFromAnnotatedClasses(String instanceName) {		
		try {
			Object object = getInstance(instanceClasses.get(instanceName));
			instances.put(instanceName, object);
			return object;
		} catch (InstantiationException e) {			
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {			
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Analyzes the fields that are to be Injected by Container and generates
	 * Object accordingly
	 * @param c
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private Object getInstance(Class<?> c) throws InstantiationException,
			IllegalAccessException {
		Object object = c.newInstance();
		Field[] fields = c.getDeclaredFields();
		for (Field field : fields) {
			Annotation annotation = field.getAnnotation(Inject.class);
			if (annotation != null) {				
				Object value = instances.get(field.getName());
				if (value == null) {
					Object typesInstance = getInstance(field.getType());
					field.setAccessible(true);
					field.set(object, typesInstance);
				} else {
					field.setAccessible(true);
					field.set(object, value);
				}
			}
		}
		return object;
	}
	
	//Getters and Setters

	public String getPackageToScan() {
		return packageToScan;
	}

	public void setPackageToScan(String packageToScan) {
		this.packageToScan = packageToScan;
	}

}
