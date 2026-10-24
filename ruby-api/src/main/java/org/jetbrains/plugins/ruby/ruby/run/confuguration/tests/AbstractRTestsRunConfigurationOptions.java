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
package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import com.dslplatform.json.CompiledJson;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfigurationOptions;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class AbstractRTestsRunConfigurationOptions extends AbstractRubyRunConfigurationOptions {
    private @Nullable String myTestsFolderPath;
    private @Nullable String myTestScriptPath;
    private @Nullable String myTestFileMask;
    private @Nullable String myTestType;

    @Attribute("testsFolderPath")
    public @Nullable String getTestsFolderPath() {
        return myTestsFolderPath;
    }

    public void setTestsFolderPath(@Nullable String testsFolderPath) {
        myTestsFolderPath = StringUtil.nullize(testsFolderPath, true);
    }

    @Attribute("testScriptPath")
    public @Nullable String getTestScriptPath() {
        return myTestScriptPath;
    }

    public void setTestScriptPath(@Nullable String testScriptPath) {
        myTestScriptPath = StringUtil.nullize(testScriptPath, true);
    }

    @Attribute("testFileMask")
    public @Nullable String getTestFileMask() {
        return myTestFileMask;
    }

    public void setTestFileMask(@Nullable String testFileMask) {
        myTestFileMask = StringUtil.nullize(testFileMask, true);
    }

    @Attribute("testType")
    public @Nullable String getTestType() {
        return myTestType;
    }

    public void setTestType(@Nullable String testType) {
        myTestType = StringUtil.nullize(testType, true);
    }
}
