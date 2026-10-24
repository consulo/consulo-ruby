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

import consulo.module.Module;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.ComboBox;
import consulo.ui.TextAttribute;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Module chooser of Ruby run configurations: the modules applicable to the configuration plus "none", keeping a stored
 * module name that is no longer applicable.
 */
public final class RubyModuleComboBox {
    private static final String NO_MODULE = "";

    private final Set<String> myModuleNames = new HashSet<>();
    private final MutableFlatDataModel<String> myModel;
    private final ComboBox<String> myComboBox;

    @RequiredUIAccess
    public RubyModuleComboBox() {
        myModel = FlatDataModel.of(List.of(NO_MODULE));
        myComboBox = ComboBox.create(myModel);
        myComboBox.setRenderer((presentation, item) -> {
            String name = item.getValue();
            if (StringUtil.isEmpty(name)) {
                presentation.append(RubyApiLocalize.runConfigurationMessagesNone());
            }
            else {
                presentation.withIcon(PlatformIconGroup.nodesModule());
                presentation.append(name, myModuleNames.contains(name) ? TextAttribute.REGULAR : TextAttribute.ERROR);
            }
        });
    }

    public ComboBox<String> getComponent() {
        return myComboBox;
    }

    @RequiredUIAccess
    public void reset(Module[] modules, @Nullable String selectedModuleName) {
        myModuleNames.clear();

        List<String> items = new ArrayList<>(modules.length + 2);
        items.add(NO_MODULE);
        for (Module module : modules) {
            myModuleNames.add(module.getName());
            items.add(module.getName());
        }

        String moduleName = StringUtil.notNullize(selectedModuleName);
        if (!items.contains(moduleName)) {
            items.add(moduleName);
        }

        myModel.replaceAll(items);
        myComboBox.setValue(moduleName, false);
    }

    public @Nullable String getModuleName() {
        return StringUtil.nullize(myComboBox.getValue());
    }

    public void addModuleListener(Consumer<String> listener) {
        myComboBox.addValueListener(event -> listener.accept(StringUtil.notNullize(event.getValue())));
    }
}
