package com.kumuluz.ee.jnosql.document;

import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.document.DocumentCollectionManager;
import org.jnosql.diana.api.document.DocumentCollectionManagerFactory;
import org.jnosql.diana.api.document.DocumentConfiguration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

@ApplicationScoped
public class DocumentCollectionManagerProducer {

	private static String collection = "";
	private static String documentConfigClassName;
	private static Map<String, Object> settings;

	private DocumentConfiguration configuration;

	private DocumentCollectionManagerFactory managerFactory;

	static void setCollection(String collection) {
		DocumentCollectionManagerProducer.collection = collection;
	}

	static void setDocumentConfigClassName(String documentConfigClassName) {
		DocumentCollectionManagerProducer.documentConfigClassName = documentConfigClassName;
	}

	static void setSettings(Map<String, Object> settings) {
		DocumentCollectionManagerProducer.settings = settings;
	}

	@PostConstruct
	private void init() {
		try {
			Class<?> keyValueConfigType = Class.forName(documentConfigClassName);
			Constructor<?> constructor = keyValueConfigType.getConstructor();
			configuration = (DocumentConfiguration) constructor.newInstance();
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
	public DocumentCollectionManager getManager() {
		return managerFactory.get(collection);
	}
}
