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

import consulo.localize.LocalizeValue;
import consulo.ruby.api.localize.RubyApiLocalize;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration.TestType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.AbstractRTestsTargetFragment;

import java.util.List;

public class RSpecTargetFragment extends AbstractRTestsTargetFragment<RSpecRunConfiguration> {
    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationFragmentSpec();
    }

    @Override
    protected List<TestType> getTestTypes() {
        return List.of(TestType.ALL_IN_FOLDER, TestType.TEST_SCRIPT);
    }

    @Override
    protected LocalizeValue getTestTypeName(TestType testType) {
        return testType == TestType.ALL_IN_FOLDER
            ? RubyApiLocalize.rspecRunConfigurationTestsDialogRbAllinfolder()
            : RubyApiLocalize.rspecRunConfigurationTestsDialogRbFile();
    }

    @Override
    protected LocalizeValue getTestTypeLabel() {
        return RubyApiLocalize.rspecRunConfigurationTestsFormTesttypeTitle();
    }

    @Override
    protected LocalizeValue getFolderLabel() {
        return LocalizeValue.join(RubyApiLocalize.rspecRunConfigurationTestsDialogComponentsFolder(), LocalizeValue.colon());
    }

    @Override
    protected LocalizeValue getFileMaskLabel() {
        return RubyApiLocalize.rspecRunConfigurationTestsDialogComponentsSearchMask();
    }

    @Override
    protected LocalizeValue getScriptLabel() {
        return RubyApiLocalize.rspecRunConfigurationMessagesScriptPath();
    }

    @Override
    protected String getDefaultFileMask() {
        return RSpecRunConfiguration.DEFAULT_TESTS_SEARCH_MASK;
    }
}
