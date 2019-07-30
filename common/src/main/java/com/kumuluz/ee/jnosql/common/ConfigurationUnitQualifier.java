package com.kumuluz.ee.jnosql.common;

import org.jnosql.artemis.ConfigurationUnit;
import org.jnosql.artemis.DatabaseType;

import javax.enterprise.util.AnnotationLiteral;

public class ConfigurationUnitQualifier extends AnnotationLiteral<ConfigurationUnit> implements ConfigurationUnit {

	private String name;
	private String fileName;
	private String database;
	private DatabaseType repository;
	private String qualifier;

	public ConfigurationUnitQualifier(String name, String fileName, String database, DatabaseType repository, String qualifier) {
		this.name = name;
		this.fileName = fileName;
		this.database = database;
		this.repository = repository;
		this.qualifier = qualifier;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String fileName() {
		return fileName;
	}

	@Override
	public String database() {
		return database;
	}

	@Override
	public DatabaseType repository() {
		return repository;
	}

	@Override
	public String qualifier() {
		return qualifier;
	}
}
