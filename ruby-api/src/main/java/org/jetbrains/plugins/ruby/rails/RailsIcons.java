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

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ui.image.Image;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg, Roman Chernyatchik
 * @date: 20.09.2006
 */
public interface RailsIcons
{
	final Image RAILS_SMALL = RubyApiIconGroup.railsProjectviewRailsmodulefolder();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Module
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Deprecated
	final Image RAILS_MODULE_NODE = RAILS_SMALL;
	@Deprecated
	final Image RAILS_MODULE_CLOSED = RubyApiIconGroup.railsProjectviewRailsmodulefolder();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Rails project view
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Image RAILS_PROJECT_VIEW = RAILS_MODULE_NODE;
	final Image RAILS_APPlICON_NODES = RubyApiIconGroup.rubyModels();
	//    final Icon RAILS_COMPONENTS_NODES =     IconLoader.findIcon("/modules/globalResources.png");
	final Image RAILS_CONTROLERS_NODES = RubyApiIconGroup.railsProjectviewControllersfolder();
	final Image RAILS_HELPER_NODE = RubyIcons.RUBY_MODULE_NODE;
	final Image RAILS_HELPERS_NODES = RubyApiIconGroup.railsProjectviewHelpersfolder();
	//    final Icon RAILS_LIBS_NODES =           IconLoader.findIcon("/objectBrowser/showLibraryContents.png");
	final Image RAILS_MODEL_NODES = RubyApiIconGroup.rubyModels();
	final Image RAILS_MODEL_NODE = RubyApiIconGroup.railsModelclass();
	final Image RAILS_FOLDER_OPENED = PlatformIconGroup.nodesFolder();
	final Image RAILS_FOLDER_CLOSED = PlatformIconGroup.nodesFolder();
	final Image RAILS_PARTIALS_OPEN = PlatformIconGroup.nodesWebfolder();
	final Image RAILS_PARTIALS_CLOSED = PlatformIconGroup.nodesWebfolder();
	final Image RAILS_MIGRATIONS_CLOSED = RubyApiIconGroup.railsProjectviewMigrationsfolder();
	final Image RAILS_SCHEMA_FILE = PlatformIconGroup.nodesDatatables();
	//    final Icon RAILS_MODELS_NODES =      IconLoader.findIcon(DATA_PATH+"modelesNode.png");
	//    final Icon RAILS_TESTS_NODES =      IconLoader.findIcon(DATA_PATH +"testsFolder.png");
	//    final Icon RAILS_TESTS_NODES =          IconLoader.findIcon("/modules/testSourceClosed.png");
	//    final Icon RAILS_TESTS_NODES_OPENED = IconLoader.findIcon("/modules/testSourceOpened.png");
	//    final Icon RAILS_TESTS_NODES_CLOSED = IconLoader.findIcon("/modules/testSourceClosed.png");
	//    final Icon RAILS_VIEW_NODES =           IconLoader.findIcon("/nodes/webFolderClosed.png");

	final Image RAILS_ACTION_NODE = RubyApiIconGroup.railsActionmethod();
	final Image RAILS_CONTROLLER_NODE = RubyApiIconGroup.railsController();
	//    final Icon RAILS_PACKAGE_MODULE_NODE =  IconLoader.findIcon("/nodes/packageClosed.png");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Rake
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Image RAKE_TASK_ICON = RubyApiIconGroup.rakeRaketask();
	final Image RAKE_GROUP_ICON = RAILS_FOLDER_CLOSED;
	final Image RAKE_TASKS_ROOT_ICON = RubyApiIconGroup.rakeRakerunconfiguration();

	final Image RAKE_PARAMS_TRACE = PlatformIconGroup.toolwindowsToolwindowdebugger();
	//    final Icon RAKE_PARAMS_LIBDIR =         IconLoader.findIcon("/modules/libraries.png");
	final Image RAKE_PARAMS_PREREQS = null;
	//    final Icon RAKE_PARAMS_USAGE =          IconLoader.findIcon("/actions/find.png");
	final Image RAKE_PARAMS_DRY_RUN = PlatformIconGroup.actionsPreview();
	final Image RAKE_PARAMS_NOSEARCH = PlatformIconGroup.actionsSearch();
	//    final Icon RAKE_PARAMS_MODULE =         IconLoader.findIcon(DATA_ROOT + "rails/rake/module.png");
	//    final Icon RAKE_PARAMS_AUTOIMPORT =     IconLoader.findIcon("/nodes/aspect.png");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Misc
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	final Image RAILS_RUN_CONFIGURATION_FOLDER = RAILS_SMALL;

	final Image RAILS_ACTION_TO_VIEW_MARKER = RubyApiIconGroup.erbErbfile();
	final Image RAILS_VIEW_TO_ACTION_MARKER = RubyApiIconGroup.railsActionmethod();
	final Image RAILS_VIEW_TO_CONTROLLER_MARKER = RAILS_VIEW_TO_ACTION_MARKER;

	final Image RJS_ICON = RubyApiIconGroup.erbRjsfile();
	final Image RHTML_ICON = RubyApiIconGroup.erbErbfile();
	final Image RXTML_ICON = RubyApiIconGroup.erbRxmlfile();

	final Image GENERATOR_ICON = RubyApiIconGroup.railsAddany();
	final Image GENERATORS_ROOT_ICON = PlatformIconGroup.nodesFolder();

	final Image WARINIG_ICON = PlatformIconGroup.generalWarning();
}
