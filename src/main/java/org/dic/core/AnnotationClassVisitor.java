package org.dic.core;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
//ClassVisitor
public class AnnotationClassVisitor extends ClassVisitor {

	private static final int ASM4 = Opcodes.ASM4;
	private Beans beans;
	private String className;
	private boolean configuration;

	public AnnotationClassVisitor(Beans beans) {
		super(ASM4);
		this.beans = beans;
	}

	//ClassVisitor method
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		className = name.replaceAll("/", ".");
	}

	//ClassVisitor method
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (desc.endsWith("annotation/Instance;")) {
			return new InstanceAnnotationVisitor(className, beans);
		} else if (desc.endsWith("annotation/Configuration;")) {
			setConfiguration(true);	
			return null;
		} else {
			return null;
		}
	}

	//ClassVisitor method
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		if(isConfiguration())
			return new BeanMethodVisitor(className,beans);
		else return null;
	}

	public boolean isConfiguration() {
		return configuration;
	}

	public void setConfiguration(boolean configuration) {
		this.configuration = configuration;
	}

}

//AnnotationVisitor
class InstanceAnnotationVisitor extends AnnotationVisitor {

	private static final int ASM4 = Opcodes.ASM4;
	private Beans beans;
	private String className;
	
	public InstanceAnnotationVisitor(String className, Beans beans) {
		super(ASM4);
		this.beans = beans;
		this.className = className;
	}
	
	//AnnotationVisitor method
	public void visit(String name, Object value) {
		beans.getInstanceClassNames().put((String) value, className);
	}

}

//MethodVisitor
class BeanMethodVisitor extends MethodVisitor{

	private static final int ASM4 = Opcodes.ASM4;
	private Beans beans;
	private String className;
		
	public BeanMethodVisitor(String className, Beans beans) {
		super(ASM4);
		this.beans = beans;
		this.className = className;		
	}
	
	//MethodVisitor method
	public AnnotationVisitor visitAnnotation(String desc, boolean visible){
		if(desc.endsWith("annotation/Bean;"))
			return new BeanAnnotationVisitor(className, beans);
		else return null;
	}
	
}

class BeanAnnotationVisitor extends AnnotationVisitor{
	
	private static final int ASM4 = Opcodes.ASM4;
	private Beans beans;
	private String className;
	
	public BeanAnnotationVisitor(String className, Beans beans) {
		super(ASM4);
		this.beans = beans;
		this.className = className;
	}
	
	public void visit(String name, Object value){
		beans.getConfigurationBeans().put((String) value, className);
	}
	
}
