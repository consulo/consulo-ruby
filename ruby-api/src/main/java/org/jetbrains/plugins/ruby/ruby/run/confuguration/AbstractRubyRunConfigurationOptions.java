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

import com.dslplatform.json.CompiledJson;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class AbstractRubyRunConfigurationOptions extends RunConfigurationOptions {
    private @Nullable String myModuleName;
    private @Nullable String myRubyArgs;
    private @Nullable String myAlternativeSdkName;
    private boolean myUseAlternativeSdk;

    @Attribute("moduleName")
    public @Nullable String getModuleName() {
        return myModuleName;
    }

    public void setModuleName(@Nullable String moduleName) {
        myModuleName = StringUtil.nullize(moduleName, true);
    }

    @Attribute("rubyArgs")
    public @Nullable String getRubyArgs() {
        return myRubyArgs;
    }

    public void setRubyArgs(@Nullable String rubyArgs) {
        myRubyArgs = StringUtil.nullize(rubyArgs, true);
    }

    @Attribute("alternativeSdkName")
    public @Nullable String getAlternativeSdkName() {
        return myAlternativeSdkName;
    }

    public void setAlternativeSdkName(@Nullable String alternativeSdkName) {
        myAlternativeSdkName = StringUtil.nullize(alternativeSdkName, true);
    }

    @Attribute("useAlternativeSdk")
    public boolean isUseAlternativeSdk() {
        return myUseAlternativeSdk;
    }

    public void setUseAlternativeSdk(boolean useAlternativeSdk) {
        myUseAlternativeSdk = useAlternativeSdk;
    }
}
