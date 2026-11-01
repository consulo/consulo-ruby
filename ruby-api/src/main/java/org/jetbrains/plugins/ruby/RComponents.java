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

package org.jetbrains.plugins.ruby;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.rails.RailsComponents;
import org.jetbrains.plugins.ruby.ruby.RubyComponents;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 29.08.2006
 */

/**
 * All ruby components names are enumerated here. Use this names to get Component by name.
 */
public interface RComponents extends RubyComponents, RailsComponents
{
	@NonNls
	final String PLUGIN_ID = "Ruby";

	@NonNls
	final String RSETTINGS = "RSettings";
	@NonNls
	final String RMODULE_SETTINGS_STORAGE = "RModuleSettingsStorage";
	@NonNls
	final String RI_MODULE_COMPONENT = "RIModuleComponent";
	@NonNls
	final String RUBY_ERROR_REPORTER = "RubyErrorReporter";
	@NonNls
	final String RLAST_SYMBOL_STORAGE = "RLastSymbolStorage";
	@NonNls
	final String RPROJECT_ROOT_MANAGER = "RProjectRootManager";
	@NonNls
	final String RUBY_ICON_PROVIDER = "RubyIconProvider";

	@NonNls
	final String RSPEC_SUPPORT_LOADER = "RspecSupportLoader";
	@NonNls
	final String RSPEC_MODULE_SETTINGS_STORAGE = "RSpecModuleSettingsStorage";
	@NonNls
	final String RSPEC_RUN_CONFIGURATION_TYPE = "RSpecRunConfigurationType";


	@NonNls
	final String RUBY_TREE_STRUCTURE_PROVIDER = "RubyTreeStructureProvider";
	@NonNls
	final String RUBY_USAGE_GROUP_RULE_PROVIDER = "RubyUsageGroupRuleProvider";
	@NonNls
	final String RUBY_PSI_MANAGER = "RubyPsiManager";
}
