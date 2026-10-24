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
package org.jetbrains.plugins.ruby.rails.run.configuration.server;

import com.dslplatform.json.CompiledJson;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfigurationOptions;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class RailsServerRunConfigurationOptions extends RubyRunConfigurationOptions {
    private boolean myChoosePortManually;
    private @Nullable String myPort;
    private @Nullable String myIpAddress;
    private @Nullable String myServerType;
    private @Nullable String myEnvironmentType;

    @Attribute("choosePortManually")
    public boolean isChoosePortManually() {
        return myChoosePortManually;
    }

    public void setChoosePortManually(boolean choosePortManually) {
        myChoosePortManually = choosePortManually;
    }

    @Attribute("port")
    public @Nullable String getPort() {
        return myPort;
    }

    public void setPort(@Nullable String port) {
        myPort = StringUtil.nullize(port, true);
    }

    @Attribute("ipAddress")
    public @Nullable String getIpAddress() {
        return myIpAddress;
    }

    public void setIpAddress(@Nullable String ipAddress) {
        myIpAddress = StringUtil.nullize(ipAddress, true);
    }

    @Attribute("serverType")
    public @Nullable String getServerType() {
        return myServerType;
    }

    public void setServerType(@Nullable String serverType) {
        myServerType = StringUtil.nullize(serverType, true);
    }

    @Attribute("environmentType")
    public @Nullable String getEnvironmentType() {
        return myEnvironmentType;
    }

    public void setEnvironmentType(@Nullable String environmentType) {
        myEnvironmentType = StringUtil.nullize(environmentType, true);
    }
}
