package org.dic.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dic.core.annotation.Bean;
import org.dic.core.annotation.Inject;
import org.objectweb.asm.ClassReader;

/**
 * Instance Builder Class
 * @author Sarath
 *
 */
public class InstanceFactory{

	private Beans beans;
	private String packageToScan;

	public InstanceFactory(String packageToScan) {
		this.packageToScan = packageToScan;
		beans = new Beans();
		init();
	}

	/**
	 * Initializes the ClassFinder and feeds the classes to ClassReaders
	 */
	private void init() {
		ClassFinder finder = new ClassFinder(packageToScan);
		for(String className : finder.getClassNames()){
			try {
				ClassReader reader = new ClassReader(className);
				AnnotationClassVisitor classVisitor = new AnnotationClassVisitor(beans);
				reader.accept(classVisitor, 0);
			} catch (IOException e) {				
				e.printStackTrace();
				throw new RuntimeException("Initialization error");
			}
		}
	}	
		
	/**
	 * Returns an instance of the given name
	 * @param beanName
	 * @return Instance 
	 */
	public Object getInstance(String beanName) {
		if (beans.getInstances().containsKey(beanName)) {
			return beans.getInstances().get(beanName);
		} else if (beans.getConfigurationBeans().containsKey(beanName)) {
			makeConfigurationBeanInstance(beanName);
			return getInstance(beanName);
		} else if (beans.getInstanceClassNames().containsKey(beanName)) {
			makeAnnotatedClassInstance(beanName);
			return getInstance(beanName);
		} else {
			throw new RuntimeException("Bean not found exception");
		}
	}

	/**
	 * Creates an Instance of the given name from the Configuration classes
	 * (It finds the method annotated with Bean annotation with bean name = 
	 * given beanName and invokes the method which will return the instance)
	 * @param beanName
	 */
	private void makeConfigurationBeanInstance(String beanName) {
		try {
			Class<?> configuration = Class.forName(beans
					.getConfigurationBeans().get(beanName));
			Method[] methods = configuration.getDeclaredMethods();
			for (Method method : methods) {
				Bean bean = method.getAnnotation(Bean.class);
				if (bean != null) {
					if (bean.name().equals(beanName)) {
						Object object = configuration.newInstance();
						Object instance = method.invoke(object, null);
						beans.getInstances().put(beanName, instance);
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		}
	}

	/**
	 * Creates an Instance of the class with the Instance annotation. 
	 * @param beanName
	 */
	private void makeAnnotatedClassInstance(String beanName) {
		try {
			Class<?> annotatedClass = Class.forName(beans.getInstanceClassNames()
					.get(beanName));
			Object instance = getInstance(annotatedClass);
			beans.getInstances().put(beanName, instance);
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		} catch (IllegalAccessException e) {			
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		} catch (InstantiationException e) {			
			e.printStackTrace();
			throw new RuntimeException("Bean not found exception");
		}
	}

	/**
	 * Returns the Instance of the class annotated with Instance annotation
	 * Fields of the class are searched for Inject annotation and dependencies
	 * are injected to the fields annotated with the Inject annotation
	 * @param annotatedClass
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private Object getInstance(Class<?> annotatedClass) throws IllegalArgumentException, IllegalAccessException, InstantiationException {
		Object instance = annotatedClass.newInstance();
		Field[] fields = annotatedClass.getDeclaredFields();
		for(Field field : fields){
			Inject inject = field.getAnnotation(Inject.class);
			if(inject != null){
				Object object = getInstance(field.getName());				
				if(object != null){					
					field.setAccessible(true);
					field.set(instance, object);
				} else {
					Object fieldsTypesInstance = getInstance(field.getType());
					field.setAccessible(true);
					field.set(instance, fieldsTypesInstance);
				}
			}
		}
		return instance;
	}	

}
