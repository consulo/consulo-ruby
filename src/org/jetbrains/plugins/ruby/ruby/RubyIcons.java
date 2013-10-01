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

import javax.swing.Icon;

import com.intellij.openapi.util.IconLoader;

/**
 * Created by IntelliJ IDEA.
 * User: oleg, Roman.Chernyatchik
 * Date: 20.09.2006
 */
public interface RubyIcons
{

	final Icon RUBY_ICON = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/ruby.png");
	final Icon RUBY_LARGE_ICON = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/rubylarge.png");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Nodes
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Icon RUBY_CLASS_NODE = IconLoader.findIcon("/nodes/class.png");
	final Icon RUBY_METHOD_NODE = IconLoader.findIcon("/nodes/method.png");
	final Icon RUBY_REQUIRE_NODE = IconLoader.findIcon("/nodes/aspect.png");
	final Icon RUBY_PARAMETER_NODE = IconLoader.findIcon("/nodes/parameter.png");
	final Icon RUBY_VARIABLE_NODE = IconLoader.findIcon("/nodes/variable.png");
	final Icon RUBY_MODULE_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/module.png");
	final Icon RUBY_CONSTANT_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/constant.png");
	final Icon RUBY_NOT_DEFINED_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/not_defined.png");

	final Icon RUBY_OBJECT_CLASS_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/include.png");
	final Icon RUBY_GLOBAL_VAR_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/variable$.png");
	final Icon RUBY_FIELD_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/variable@.png");

	final Icon RUBY_INCLUDE_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/include.png");
	final Icon RUBY_ALIAS_NODE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/nodes/alias.png");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Node attributes
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Icon RUBY_ATTR_NODE = IconLoader.findIcon("/nodes/annotationtype.png");
	final Icon RUBY_ATTR_PUBLIC = IconLoader.findIcon("/nodes/c_public.png");
	final Icon RUBY_ATTR_PRIVATE = IconLoader.findIcon("/nodes/c_private.png");
	final Icon RUBY_ATTR_PROTECTED = IconLoader.findIcon("/nodes/c_protected.png");
	final Icon RUBY_ATTR_STATIC = IconLoader.findIcon("/nodes/staticMark.png");

	final Icon RUBY_ATTR_READER = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/reader.png");
	final Icon RUBY_ATTR_WRITER = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/writer.png");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Module
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Icon RUBY_MODULE_BIG = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/rubymedium.png");

	final Icon RUBY_MODULE_OPENED = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/ruby_module_opened.png");
	final Icon RUBY_MODULE_CLOSED = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/ruby_module_closed.png");

	final Icon RUBY_ADD_MODULE = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/add_ruby_modulewizard.png");

	final Icon RUBY_MODULE_SETTINGS_LOADPATH = IconLoader.findIcon("/modules/classpath.png");
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Sdk
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Icon RUBY_SDK = RUBY_MODULE_CLOSED;
	final Icon RUBY_SDK_EXPANDED = RUBY_MODULE_OPENED;

	final Icon RUBY_SDK_ADD_ICON = RUBY_ICON;


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Run configurations
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Icon RUBY_RUN_CONFIGURATION_FOLDER = RUBY_ICON;

	final Icon RUBY_RUN_CONFIGURATION_SCRIPT = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/configuration/ruby_script.png");
	final Icon RTEST_RUN_CONFIGURATION = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/configuration/ruby_test_unit.png");
	final Icon RAILS_SERVER_RUN_CONFIGURATION = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/configuration/run_conf_server.png");


	final Icon RUBY_RUNNER_SHOW_CMDLINE = IconLoader.findIcon("/actions/showViewer.png");

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Misc
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Icon RUBY_COLOR_PAGE = RUBY_ICON;

	final Icon RUBY_GUTTER_OVERRIDING = IconLoader.findIcon("/gutter/overridingMethod.png");
	final Icon RUBY_GUTTER_IMPLEMENTING = IconLoader.findIcon("/gutter/implementingMethod.png");

	final Icon RI_ICON = IconLoader.findIcon("/org/jetbrains/plugins/ruby/ruby/ri.png");
}

