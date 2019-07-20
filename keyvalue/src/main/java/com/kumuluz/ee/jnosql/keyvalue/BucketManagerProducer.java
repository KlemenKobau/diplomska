package com.kumuluz.ee.jnosql.keyvalue;

import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.key.BucketManager;
import org.jnosql.diana.api.key.BucketManagerFactory;
import org.jnosql.diana.api.key.KeyValueConfiguration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@ApplicationScoped
public class BucketManagerProducer {

	private static String bucket = "";
	private static String keyValueConfigClassName;
	private static Map<String, Object> settings;

	private KeyValueConfiguration configuration;

	private BucketManagerFactory managerFactory;

	static void setBucket(String bucket) {
		BucketManagerProducer.bucket = bucket;
	}

	static void setKeyValueConfigClassName(String keyValueConfigClassName) {
		BucketManagerProducer.keyValueConfigClassName = keyValueConfigClassName;
	}

	static void setSettings(Map<String, Object> settings) {
		BucketManagerProducer.settings = settings;
	}

	@PostConstruct
	public void init() {
		try {
			Class<?> keyValueConfigType = Class.forName(keyValueConfigClassName);
			Constructor<?> constructor = keyValueConfigType.getConstructor();
			configuration = (KeyValueConfiguration) constructor.newInstance();
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
	public BucketManager getManager() {
		return managerFactory.getBucketManager(bucket);
	}

}
