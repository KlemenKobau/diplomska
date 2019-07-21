package com.kumuluz.ee.jnosql.graph;

import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.EeComponentDependencies;
import com.kumuluz.ee.common.dependencies.EeComponentDependency;
import com.kumuluz.ee.common.dependencies.EeComponentType;
import com.kumuluz.ee.common.dependencies.EeExtensionDef;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@EeExtensionDef(name = "JNoSQL", group = "NoSQL")
@EeComponentDependencies(value = {
		@EeComponentDependency(EeComponentType.JSON_P),
		@EeComponentDependency(EeComponentType.JSON_B),
		@EeComponentDependency(EeComponentType.CDI),
		@EeComponentDependency(EeComponentType.SERVLET)})
public class JNoSQLGraphExtension implements Extension {

	private static final Logger log = Logger.getLogger(JNoSQLGraphExtension.class.getName());
	private static final String SETTINGS_PATH = "kumuluzee.jnosql.graph";

	private static void getGraphSettingsMap(List<String> settingNames, Map<String, String> settingsMap) {

		for (String settingName : settingNames) {
			Optional<String> valuePair = ConfigurationUtil.getInstance().get(SETTINGS_PATH + "." + settingName);
			valuePair.ifPresent(value -> settingsMap.put(settingName, value));
		}
	}

	@Override
	public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {
		log.info("Initializing JNoSql graph extension");

		ConfigurationUtil cfg = ConfigurationUtil.getInstance();

		Optional<List<String>> graphSettings = cfg.getMapKeys(SETTINGS_PATH);
		Map<String, String> settings = new HashMap<>();

		graphSettings.ifPresent(strings -> getGraphSettingsMap(strings, settings));

		GraphProducer.setSettings(settings);
	}

	@Override
	public void load() {
	}
}
