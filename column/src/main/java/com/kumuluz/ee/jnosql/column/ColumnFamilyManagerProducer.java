package com.kumuluz.ee.jnosql.column;

import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.column.ColumnConfiguration;
import org.jnosql.diana.api.column.ColumnFamilyManager;
import org.jnosql.diana.api.column.ColumnFamilyManagerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@ApplicationScoped
public class ColumnFamilyManagerProducer {

	private static String keySpace = "";

	private static String columnConfigClassName;
	private static Map<String, Object> settings;

	private ColumnConfiguration configuration;

	private ColumnFamilyManagerFactory managerFactory;

	static void setKeySpace(String keySpace) {
		ColumnFamilyManagerProducer.keySpace = keySpace;
	}

	static void setColumnConfigClassName(String columnConfigClassName) {
		ColumnFamilyManagerProducer.columnConfigClassName = columnConfigClassName;
	}

	static void setSettings(Map<String, Object> settings) {
		ColumnFamilyManagerProducer.settings = settings;
	}

	@PostConstruct
	public void init() {
		try {
			Class<?> keyValueConfigType = Class.forName(columnConfigClassName);
			Constructor<?> constructor = keyValueConfigType.getConstructor();
			configuration = (ColumnConfiguration) constructor.newInstance();
			managerFactory = configuration.get(Settings.of(settings));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Key value configuration class has to be provided");
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Key value configuration class must have a constructor with no parameters");
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Constructor in inaccessible");
		} catch (InstantiationException e) {
			throw new RuntimeException("Key value config class should not be abstract");
		} catch (InvocationTargetException e) {
			throw new RuntimeException("The key value constructor threw an error");
		}
	}

	@Produces
	public ColumnFamilyManager getManager() {
		return managerFactory.get(keySpace);
	}
}
