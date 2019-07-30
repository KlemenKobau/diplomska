package com.kumuluz.ee.jnosql.common;

import org.jnosql.artemis.DatabaseType;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class DatabaseTemplate {

	private static final Logger log = Logger.getLogger(DatabaseTemplate.class.getName());

	private static final String graphTemplateClassName = "org.jnosql.artemis.graph.GraphTemplate";
	private static final String columnTemplateClassName = "org.jnosql.artemis.column.ColumnTemplate";
	private static final String documentTemplateClassName = "org.jnosql.artemis.document.DocumentTemplate";
	private static final String keyValueTemplateClassName = "org.jnosql.artemis.key.KeyValueTemplate";

	private Object databaseTemplate;
	private Class<?> databaseClass;
	private DatabaseType databaseType;

	@Inject
	public DatabaseTemplate(InjectionPoint ip, BeanManager bm) {

		DatabaseLogic databaseAnnotation = null;

		for (Annotation annotation : ip.getAnnotated().getAnnotations()) {
			if (annotation.annotationType().isAssignableFrom(DatabaseLogic.class)) {
				databaseAnnotation = ((DatabaseLogic) annotation);
				break;
			}
		}

		if (databaseAnnotation == null) {
			log.severe("Trying to inject JNoSQL database but " + DatabaseLogic.class.getName() + "annotation was not provided");
			throw new RuntimeException(DatabaseTemplate.class.getName() + " has to be annotated with " + DatabaseLogic.class.getName());
		}

		databaseType = databaseAnnotation.databaseType();

		try {
			switch (databaseType) {
				case GRAPH:
					databaseClass = Class.forName(graphTemplateClassName);
					break;
				case COLUMN:
					databaseClass = Class.forName(columnTemplateClassName);
					break;
				case DOCUMENT:
					databaseClass = Class.forName(documentTemplateClassName);
					break;
				case KEY_VALUE:
					databaseClass = Class.forName(keyValueTemplateClassName);
					break;
				default:
					log.severe("Unknown database type: " + databaseType.toString());
					throw new IllegalArgumentException("Unknown database type");
			}

		} catch (ClassNotFoundException e) {
			log.severe("Unable to create desired template class");
			e.printStackTrace();
		}

		databaseTemplate = CDI.current().select(databaseClass).get(); // TODO vraca napacen class, ker je ambiguous
		System.err.println(databaseTemplate);
	}

	@SuppressWarnings("unchecked")
	public <T> T insert(T entity) {
		String methodName;

		switch (databaseType) {
			case KEY_VALUE:
				methodName = "put";
				break;
			case DOCUMENT:
			case COLUMN:
			case GRAPH:
				methodName = "insert";
				break;
			default:
				throw new IllegalArgumentException("Unknown database type");
		}

		try {
			System.err.println(databaseTemplate.toString());
			Method insertMethod = databaseTemplate.getClass().getMethod(methodName);
			return ((T) insertMethod.invoke(databaseTemplate));
		} catch (NoSuchMethodException e) {
			log.log(Level.SEVERE, "This method doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			log.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			log.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error inserting entity");
	}


}
