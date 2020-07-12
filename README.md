# KumuluzEE JNoSQL

> KumuluzEE JNoSQL integrates the eclipse JNoSQL framework, which
> strives to unify the integration of NoSQL databases

KumuluzEE JNoSQL uses the JNoSQL framework to unify the use of NoSQL
databases.
It divides NoSQL databases into Column, Document, Graph and key-value stores.
The different types are integrated similarly, only differing in used dependencies and their distinct
query languages.

## Usage

The following example will show how to use KumuluzEE JNoSQL with
Cassandra, a column database and will build upon KumuluzEE 
JAX-RS.

You can enable KumuluzEE JNoSQL by adding the dependency, that
corresponds to the type of database you wish to use.
When using Cassandra, we use the Column dependency.

```mvn
<dependency>
    <groupId>com.kumuluz.ee.jnosql</groupId>
    <artifactId>kumuluzee-jnosql-column</artifactId>
    <version>${kumuluzee-jnosql-column.version}</version>
</dependency>
```

We also have to include the Diana driver for the database.
```mvn
<dependency>
    <groupId>org.jnosql.diana</groupId>
    <artifactId>cassandra-driver</artifactId>
    <version>${diana.version}</version>
</dependency>
```

### Repositories

A repository can be created by extending the in built repository type.

```java
public interface PeopleRepository extends Repository<Person, Long> {
}
```

This will provide basic CRUD operations over entities.
Additional operations can be defined, by defining new functions
and using prefixes for example *findByName(String name)*, or by
using the query language, specific for the database type.
More about this can be read about in the official JNoSQL documentation.

### Queries

Each database type supports its own query language.
```java
@ApplicationScoped
public class ColumnBean {

	@Inject
	private ColumnTemplate column;

	public void useStatement(Long id) {
		PreparedStatement statement = column.prepare("remove @id");
		statement.bind("id", id);
		statement.getSingleResult();
	}
}
```

### Config
We can configure the different database parameters using the
*config.yaml* file.

The following is the configuration for Cassandra.

```yaml
kumuluzee:
  ...
  jnosql:
    column:
      config-class-name: org.jnosql.diana.cassandra.column.CassandraConfiguration

      cassandra-host-1: localhost
      cassandra-threads-number: 4
      key-space: 'developers'
      cassandra-query-1: "CREATE KEYSPACE IF NOT EXISTS developers WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 3};"
      cassandra-query-2: "CREATE COLUMNFAMILY IF NOT EXISTS developers.Person (id bigint PRIMARY KEY, name text, phones list<text>);"

```

Config class name has to be the class name of the configuration class in the Diana dependency.
Each database has its own config class and they can be found simply by searching for classes that contain the word *Config*.
The next lines are database specific and unfortunately, Eclipse JNoSQL doesn't provide satisfactory documentation for them.
 