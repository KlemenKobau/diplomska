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

/**
 * @author Klemen Kobau
 */
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
