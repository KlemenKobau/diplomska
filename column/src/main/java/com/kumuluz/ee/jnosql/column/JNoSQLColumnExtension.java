package com.kumuluz.ee.jnosql.column;

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
public class JNoSQLColumnExtension implements Extension {
	private static final Logger log = Logger.getLogger(JNoSQLColumnExtension.class.getName());
	private static final String SETTINGS_PATH = "kumuluzee.jnosql.column";

	private static void getColumnSettingsMap(List<String> settingNames, Map<String, Object> settingsMap) {

		for (String settingName : settingNames) {
			Optional<String> valuePair = ConfigurationUtil.getInstance().get(SETTINGS_PATH + "." + settingName);
			valuePair.ifPresent(value -> settingsMap.put(settingName, value));
		}
	}

	@Override
	public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {
		log.info("Initializing JNoSQL column extension");

		ConfigurationUtil cfg = ConfigurationUtil.getInstance();

		Optional<List<String>> columnSettings = cfg.getMapKeys(SETTINGS_PATH);
		Map<String, Object> settings = new HashMap<>();

		columnSettings.ifPresent(strings -> getColumnSettingsMap(strings, settings));
		ColumnFamilyManagerProducer.setSettings(settings);

		cfg.get(SETTINGS_PATH + ".key-space").ifPresent(ColumnFamilyManagerProducer::setKeySpace);
		cfg.get(SETTINGS_PATH + ".config-class-name")
				.ifPresentOrElse(ColumnFamilyManagerProducer::setColumnConfigClassName
						, () -> log.severe("config class name must be provided"));
	}

	@Override
	public void load() {
	}
}
