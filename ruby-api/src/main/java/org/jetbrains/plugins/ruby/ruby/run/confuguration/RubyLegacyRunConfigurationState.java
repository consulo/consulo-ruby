/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.ruby.ruby.run.confuguration;

import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.util.lang.StringUtil;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Options of an old Ruby run configuration element, written as {@code <SETTINGS_ID NAME="..." VALUE="..."/>} children.
 */
public final class RubyLegacyRunConfigurationState {
    private static final String NAME = "NAME";
    private static final String VALUE = "VALUE";

    private final Map<String, String> myValues;

    private RubyLegacyRunConfigurationState(Map<String, String> values) {
        myValues = values;
    }

    public static RubyLegacyRunConfigurationState read(LegacyRunConfigurationConverter converter, String settingsId) {
        Map<String, String> values = new HashMap<>();
        for (Element option : converter.getElement().getChildren(settingsId)) {
            String name = option.getAttributeValue(NAME);
            if (name != null) {
                values.put(name, StringUtil.notNullize(option.getAttributeValue(VALUE)));
            }
        }
        return new RubyLegacyRunConfigurationState(values);
    }

    public @Nullable String get(String name) {
        return myValues.get(name);
    }

    public boolean getBoolean(String name) {
        return Boolean.parseBoolean(myValues.get(name));
    }
}
