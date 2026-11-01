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
package org.jetbrains.plugins.ruby.addins.rspec.run.configuration;

import com.dslplatform.json.CompiledJson;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.AbstractRTestsRunConfigurationOptions;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class RSpecRunConfigurationOptions extends AbstractRTestsRunConfigurationOptions {
    private @Nullable String mySpecArgs;
    private int myExampleLine;

    @Attribute("specArgs")
    public @Nullable String getSpecArgs() {
        return mySpecArgs;
    }

    public void setSpecArgs(@Nullable String specArgs) {
        mySpecArgs = StringUtil.nullize(specArgs, true);
    }

    @Attribute("exampleLine")
    public int getExampleLine() {
        return myExampleLine;
    }

    public void setExampleLine(int exampleLine) {
        myExampleLine = exampleLine;
    }
}
