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
package com.kumuluz.ee.jnosql.graph;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import java.util.Map;

/**
 * @author Klemen Kobau
 */
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
		Configuration configuration = new BaseConfiguration();
		for (Map.Entry<String, String> stringStringEntry : settings.entrySet()) {
			configuration.addProperty(stringStringEntry.getKey(), stringStringEntry.getValue());
		}
		this.graph = GraphFactory.open(configuration);
	}
}
