/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.ruby;

import org.jetbrains.annotations.NonNls;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 20.09.2006
 */
public interface RubyComponents
{
	@NonNls
	final String RUBY_MODULE_SETTINGS = "RubyModuleSettings";
	@NonNls
	final String RUBY_SUPPORT_LOADER = "RubySupportLoader";
	@NonNls
	final String RUBY_MODULE_TYPE = "RubyModuleType";
	@NonNls
	final String RUBY_SDK_TYPE = "RubySdkType";
	@NonNls
	final String RUBY_RUN_CONFIGURATION_TYPE = "RubyRunConfigurationType";
	@NonNls
	final String RUBY_SCRIPT_TEMPLATE_LOADER = "RubyScriptTemplateLoader";
	@NonNls
	final String RUBY_MODULE_LOCATION_EDITOR_PROVIDER = "RubyModuleLocationEditorProvider";
	@NonNls
	final String RUBY_SYMBOL_CACHE = "RubySymbolCache";
	@NonNls
	final String RUBY_PROJECT_VIRTUAL_FILE_MANAGER = "RubyProjectVirtualFileManager";
	@NonNls
	final String RUBY_ASPECT = "RubyAspect";
	@NonNls
	final String RUBY_INTENTION_ACTIONS_MANAGER = "RubyIntentionActrionsManager";

	@NonNls
	final String RUBY_DOC_MANAGER = "RubyDocManager";
	@NonNls
	final String RUBY_DOC_SETTINGS = "RubyDocSettings";

	@NonNls
	final String RUBY_HIGHLIGHT_RANGE_FACTORY = "RubyHighlightRangeFactory";
	@NonNls
	final String RUBY_SLOW_HIGHLIGHT_LINE_FACTORY = "RubySlowHighlightLineFactory";
	@NonNls
	final String RUBY_FAST_HIGHLIGHT_LINE_FACTORY = "RubyFastHighlightLineFactory";
	@NonNls
	final String RUBY_MODULE_ROOT_MANAGER = "RubyModuleRootManager";
	@NonNls
	final String RUBY_REFERENCE_PROVIDER_LOADER = "RubyReferenceProviderLoader";
	//    @NonNls final String RUBY_MODULE_SETTINGS_STORAGE =     "RUBY_MODULE_SETTINGS_STORAGE";

}
