package org.dic.core;

import java.util.HashMap;
import java.util.Map;

public class Beans {
	
	private Map<String, String> configurationBeans = new HashMap<String, String>();
	private Map<String, String> InstanceClassNames = new HashMap<String, String>();
	private Map<String, Object> instances;
	
	public Map<String, String> getConfigurationBeans() {
		return configurationBeans;
	}
	
	public Map<String, String> getInstanceClassNames() {
		return InstanceClassNames;
	}

	public Map<String, Object> getInstances() {
		if(instances == null) instances = new HashMap<String, Object>();
		return instances;
	}			
	
}