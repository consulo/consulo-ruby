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

package org.jetbrains.plugins.ruby.rails;

import org.jetbrains.annotations.NonNls;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 20.09.2006
 */
public interface RailsComponents
{
	@NonNls
	final String BASE_RAILS_FACET_LISTENER = "BaseRailsFacetListener";

	@NonNls
	final String RAILS_SUPPORT_LOADER = "RailsSupportLoader";
	@NonNls
	final String RAILS_PROJECT_LOADER = "RailsProjectLoader";
	@NonNls
	final String RAILS_HIGHLIGHT_PASS_FACTORY = "RailsHighlighPassFactory";
	@NonNls
	final String RAILS_VIEW_FOLDERS_MANAGER = "RailsViewFoldersManager";
	@NonNls
	final String RAILS_TEMPLATES_LOADER = "RailsTemplatesLoader";
	@NonNls
	final String RHTML_AND_RUBY_BREADCRUMBS_INFO_PROVIDER = "RhtmlAndRubyBreadcrumbsInfoProvider";

	@SuppressWarnings({"UnusedDeclaration"}) //is used in @Storage definition
	@NonNls
	final String RAILS_MODULE_SETTINGS_STORAGE = "RailsModuleSettingsStorage";

	@NonNls
	final String RAILS_RUN_CONFIGURATION_TYPE = "RailsRunConfigurationType";
}
