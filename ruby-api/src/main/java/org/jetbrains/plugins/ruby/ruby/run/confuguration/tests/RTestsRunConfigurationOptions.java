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
import org.jspecify.annotations.Nullable;

@CompiledJson
public class RTestsRunConfigurationOptions extends AbstractRTestsRunConfigurationOptions {
    private @Nullable String myTestClassName;
    private @Nullable String myTestMethodName;
    private boolean myInheritanceCheckDisabled;

    @Attribute("testClassName")
    public @Nullable String getTestClassName() {
        return myTestClassName;
    }

    public void setTestClassName(@Nullable String testClassName) {
        myTestClassName = StringUtil.nullize(testClassName, true);
    }

    @Attribute("testMethodName")
    public @Nullable String getTestMethodName() {
        return myTestMethodName;
    }

    public void setTestMethodName(@Nullable String testMethodName) {
        myTestMethodName = StringUtil.nullize(testMethodName, true);
    }

    @Attribute("inheritanceCheckDisabled")
    public boolean isInheritanceCheckDisabled() {
        return myInheritanceCheckDisabled;
    }

    public void setInheritanceCheckDisabled(boolean inheritanceCheckDisabled) {
        myInheritanceCheckDisabled = inheritanceCheckDisabled;
    }
}
