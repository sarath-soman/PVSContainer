PVSContainer
============

This a Dependancy Injection framework. Dependencies are mentioned in Configuration classes(Classes Annotated with @Configuration). Injection is done to fields annotated with @Inject annotation directly. Also if instances of some classes
are to be created by framework then the class should be annotated with @Instance

Example

Class Hello:

    package com.pvs.test.greetings;

     public class Hello {
	
	public void sayHello(){
		System.out.println("Hello");
	}
	
    }

Class GoodBye:

    package com.pvs.test.greetings;

    import org.dic.core.annotation.Instance;

    @Instance(name = "goodBye")
    public class GoodBye {
	
	public void sayGoodBye(){
		System.out.println("Good Bye");
	}
	
    }


Class BeanConfiguration:

    package com.pvs.test.conf;

    import org.dic.core.annotation.Bean;
    import org.dic.core.annotation.Configuration;

    import com.pvs.test.greetings.Hello;

    @Configuration
    public class BeanConfiguration {

	@Bean(name = "hello")
	public Hello getHello(){
		return new Hello();
	}
	
    } 


Class Test:

    package com.pvs.test;

    import org.dic.core.InstanceFactory;
     
    import com.pvs.test.greetings.GoodBye;
    import com.pvs.test.greetings.Hello;

    public class Test {

	public static void main(String[] args) {
		String packageToScan = "com.pvs.test";
		InstanceFactory factory = new InstanceFactory(packageToScan);
		Hello hello = (Hello) factory.getInstance("hello");
		GoodBye goodBye = (GoodBye) factory.getInstance("goodBye");
		hello.sayHello();
		goodBye.sayGoodBye();
	}

    }

