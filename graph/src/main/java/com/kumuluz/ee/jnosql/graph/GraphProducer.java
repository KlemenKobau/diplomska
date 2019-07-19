package com.kumuluz.ee.jnosql.graph;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import java.util.Map;

@ApplicationScoped
public class GraphProducer {

	private static Map<String, String> settings;

	private Graph graph;

	static void setSettings(Map<String, String> settings) {
		GraphProducer.settings = settings;
	}

	@Produces
	@ApplicationScoped
	public Graph getGraph() {
		return graph;
	}

	public void close(@Disposes Graph graph) throws Exception {
		graph.close();
	}

	@PostConstruct
	private void init() {
		this.graph = GraphFactory.open(settings);
	}
}
