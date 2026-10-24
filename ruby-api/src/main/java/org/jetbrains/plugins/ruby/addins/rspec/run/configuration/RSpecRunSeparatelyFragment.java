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

import consulo.execution.configuration.fragment.TagRunConfigurationFragment;
import consulo.localize.LocalizeValue;
import consulo.ruby.api.localize.RubyApiLocalize;

/**
 * Runs every spec of a folder in its own process.
 */
public class RSpecRunSeparatelyFragment implements TagRunConfigurationFragment<RSpecRunConfiguration> {
    @Override
    public String getId() {
        return "rspecRunSeparately";
    }

    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationFragmentRunSpecsSeparately();
    }

    @Override
    public boolean isSet(RSpecRunConfiguration target) {
        return target.shouldRunSpecSeparately();
    }

    @Override
    public void set(RSpecRunConfiguration target, boolean value) {
        target.setShouldRunSpecSeparately(value);
    }
}
