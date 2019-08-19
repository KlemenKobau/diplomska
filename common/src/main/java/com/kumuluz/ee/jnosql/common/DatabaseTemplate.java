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
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Dependent
public class DatabaseTemplate {

	private static final Logger LOG = Logger.getLogger(DatabaseTemplate.class.getName());

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
			LOG.severe("Trying to inject JNoSQL database but " + DatabaseLogic.class.getName() + "annotation was not provided");
			throw new RuntimeException(DatabaseTemplate.class.getName() + " has to be annotated with " + DatabaseLogic.class.getName());
		}

		databaseType = databaseAnnotation.value();

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
					LOG.severe("Unknown database type: " + databaseType.toString());
					throw new IllegalArgumentException("Unknown database type");
			}

		} catch (ClassNotFoundException e) {
			LOG.severe("Unable to create desired template class");
			e.printStackTrace();
		}

		databaseTemplate = CDI.current().select(databaseClass).get(); // TODO vraca napacen class, ker je ambiguous
	}

	/**
	 * Saves the entity, method is available for GRAPH, COLUMN and DOCUMENT databases
	 * @param entity entity to be saved, requires a field to be annotated with {@link org.jnosql.artemis.Id} when using
	 *               a GRAPH database
	 * @param <T> entity class
	 * @return returns the saved entity
	 */
	@SuppressWarnings("unchecked")
	public <T> T insert(T entity) {
		String methodName;

		switch (databaseType) {
			case DOCUMENT:
			case COLUMN:
			case GRAPH:
				methodName = "insert";
				break;
			default:
				throw new IllegalArgumentException("Operation not supported for database of type: " + databaseType.toString());
		}

		try {
			Method insertMethod = databaseTemplate.getClass().getMethod(methodName, Object.class);
			return ((T) insertMethod.invoke(databaseTemplate, entity));
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "Method with name " + methodName + " doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error inserting entity");
	}

	/**
	 * Inserts an entity with time to live, after time is up the entity is deleted. Works with database types COLUMN and
	 * DOCUMENT.
	 * @param entity entity to be inserted
	 * @param ttl duration of the entity
	 * @param <T> entity class
	 * @return returns the inserted entity
	 */
	@SuppressWarnings("unchecked")
	public <T> T insert(T entity, Duration ttl) {
		String methodName;

		switch (databaseType) {
			case DOCUMENT:
			case COLUMN:
				methodName = "insert";
				break;
			default:
				throw new IllegalArgumentException("Operation not supported for database of type: " + databaseType.toString());
		}

		try {
			Method insertMethod = databaseTemplate.getClass().getMethod(methodName, Object.class, Duration.class);
			return ((T) insertMethod.invoke(databaseTemplate, entity, ttl));
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "Method with name " + methodName + " doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error inserting entity");
	}

	/**
	 * Inserts entities, by default it just does multiple inserts, however some providers might override that. Works with
	 * COLUMN, GRAPH and DOCUMENT databases.
	 * @param entities entities to be inserted.
	 * @param <T> entity class
	 * @return returns an iterable of saved entities.
	 */
	@SuppressWarnings("unchecked")
	public <T> Iterable<T> insert(Iterable<T> entities) {
		String methodName;

		switch (databaseType) {
			case GRAPH:
			case DOCUMENT:
			case COLUMN:
				methodName = "insert";
				break;
			default:
				throw new IllegalArgumentException("Operation not supported for database of type: " + databaseType.toString());
		}

		if (databaseType.equals(DatabaseType.GRAPH)) {
			Collection<T> res = new LinkedList<>();
			for (T entity : entities) {
				T out = insert(entity);
				res.add(out);
			}
			return res;
		}

		try {
			Method insertMethod = databaseTemplate.getClass().getMethod(methodName, Iterable.class);
			return ((Iterable<T>) insertMethod.invoke(databaseTemplate, entities));
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "Method with name " + methodName + " doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error inserting entity");
	}

	/**
	 *  Inserts entities with time to live, by default it just does multiple inserts with time to live, however some providers might override that. Works with
	 * 	COLUMN and DOCUMENT databases.
	 * @param entities entities to be inserted
	 * @param ttl duration of the entities
	 * @param <T> class of the entities
	 * @return returns an iterable of inserted entities.
	 */
	@SuppressWarnings("unchecked")
	public <T> Iterable<T> insert(Iterable<T> entities, Duration ttl) {
		String methodName;

		switch (databaseType) {
			case DOCUMENT:
			case COLUMN:
				methodName = "insert";
				break;
			default:
				throw new IllegalArgumentException("Operation not supported for database of type: " + databaseType.toString());
		}

		try {
			Method insertMethod = databaseTemplate.getClass().getMethod(methodName, Iterable.class, Duration.class);
			return ((Iterable<T>) insertMethod.invoke(databaseTemplate, entities, ttl));
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "Method with name " + methodName + " doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error inserting entity");
	}

	/**
	 * Inserts an entity into the database, in case the entity has id provided and an entity with matching id is present in the database,
	 * the the old entity will be deleted and the new one will be saved, available for KEY_VALUE databases
	 * @param entity the entity to be saved
	 * @param <T> class of the entity
	 * @return returns the put entity
	 */
	@SuppressWarnings("unchecked")
	public <T> T put(T entity) {
		String methodName;

		if (databaseType.equals(DatabaseType.KEY_VALUE)) {
			methodName = "put";
		} else {
			throw new IllegalArgumentException("Operation not supported for database of type: " + databaseType.toString());
		}

		try {
			Method putMethod = databaseTemplate.getClass().getMethod(methodName, Object.class);
			return ((T) putMethod.invoke(databaseTemplate, entity));
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "Method with name " + methodName + " doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error putting entity");
	}

	/**
	 * Update an entity, the new entity will update the old entity based on the provided id, works with DOCUMENT,
	 * COLUMN and GRAPH databases.
	 * @param entity the entity used to update
	 * @param <T> entity class
	 * @return returns the updated entity
	 */
	@SuppressWarnings("unchecked")
	public <T> T update(T entity) {
		String methodName;

		switch (databaseType) {
			case DOCUMENT:
			case COLUMN:
			case GRAPH:
				methodName = "update";
				break;
			default:
				throw new IllegalArgumentException("Operation not supported for database of type: " + databaseType.toString());
		}
		try {
			Method updateMethod = databaseTemplate.getClass().getMethod(methodName, Object.class);
			return ((T) updateMethod.invoke(databaseTemplate, entity));
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "Method with name " + methodName + " doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error updating entity");
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> update(Iterable<T> entities) {
		String methodName;

		switch (databaseType) {
			case DOCUMENT:
			case COLUMN:
			case GRAPH:
				methodName = "update";
				break;
			default:
				throw new IllegalArgumentException("Operation not supported for database of type: " + databaseType.toString());
		}

		if (databaseType.equals(DatabaseType.GRAPH)) {
			Collection<T> res = new LinkedList<>();

			for (T entity : entities) {
				T out = update(entity);
				res.add(out);
			}
			return res;
		}

		try {
			Method updateMethod = databaseTemplate.getClass().getMethod(methodName, Iterable.class);
			return ((Iterable<T>) updateMethod.invoke(databaseTemplate, entities));
		} catch (NoSuchMethodException e) {
			LOG.log(Level.SEVERE, "Method with name " + methodName + " doesn't exist for database type: " + databaseType.toString());
		} catch (IllegalAccessException e) {
			LOG.log(Level.SEVERE, "Illegal access", e);
		} catch (InvocationTargetException e) {
			LOG.log(Level.SEVERE, "Constructor or invoked method threw an exception", e);
		}
		throw new RuntimeException("Error updating entity");
	}

}
