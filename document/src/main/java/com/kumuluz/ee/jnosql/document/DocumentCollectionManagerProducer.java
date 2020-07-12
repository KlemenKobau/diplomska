/*
 *  Copyright (c) 2014-2017 Kumuluz and/or its affiliates
 *  and other contributors as indicated by the @author tags and
 *  the contributor list.
 *
 *  Licensed under the MIT License (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://opensource.org/licenses/MIT
 *
 *  The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND, express or
 *  implied, including but not limited to the warranties of merchantability,
 *  fitness for a particular purpose and noninfringement. in no event shall the
 *  authors or copyright holders be liable for any claim, damages or other
 *  liability, whether in an action of contract, tort or otherwise, arising from,
 *  out of or in connection with the software or the use or other dealings in the
 *  software. See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

/**
 * @author Klemen Kobau
 */
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
