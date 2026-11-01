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

import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 * User: oleg, Roman.Chernyatchik
 * Date: 20.09.2006
 */
public interface RubyIcons
{
	final Image RUBY_ICON = RubyApiIconGroup.rubyRuby();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Nodes
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Image RUBY_CLASS_NODE = PlatformIconGroup.nodesClass();
	final Image RUBY_METHOD_NODE = PlatformIconGroup.nodesMethod();
	final Image RUBY_REQUIRE_NODE = PlatformIconGroup.nodesPlugin();
	final Image RUBY_PARAMETER_NODE = PlatformIconGroup.nodesParameter();
	final Image RUBY_VARIABLE_NODE = PlatformIconGroup.nodesVariable();
	final Image RUBY_MODULE_NODE = RubyApiIconGroup.rubyNodesModule();
	final Image RUBY_CONSTANT_NODE = PlatformIconGroup.nodesConstant();
	final Image RUBY_NOT_DEFINED_NODE = RubyApiIconGroup.rubyNodesNotdefined();

	final Image RUBY_OBJECT_CLASS_NODE = PlatformIconGroup.nodesClass();
	final Image RUBY_GLOBAL_VAR_NODE = RubyApiIconGroup.rubyNodesVariable();
	final Image RUBY_FIELD_NODE = PlatformIconGroup.nodesField();

	final Image RUBY_INCLUDE_NODE = RubyApiIconGroup.rubyNodesModule();
	final Image RUBY_ALIAS_NODE = PlatformIconGroup.nodesMethod();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Node attributes
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Image RUBY_ATTR_NODE = PlatformIconGroup.nodesAnnotationtype();
	final Image RUBY_ATTR_PUBLIC = PlatformIconGroup.nodesC_public();
	final Image RUBY_ATTR_PRIVATE = PlatformIconGroup.nodesC_private();
	final Image RUBY_ATTR_PROTECTED = PlatformIconGroup.nodesC_protected();
	final Image RUBY_ATTR_STATIC = PlatformIconGroup.nodesStaticmark();

	final Image RUBY_ATTR_READER = RubyApiIconGroup.rubyReader();
	final Image RUBY_ATTR_WRITER = RubyApiIconGroup.rubyWriter();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Run configurations
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Image RUBY_RUN_CONFIGURATION_FOLDER = RUBY_ICON;

	final Image RUBY_RUN_CONFIGURATION_SCRIPT = RubyApiIconGroup.rubyRubyscript();
	final Image RTEST_RUN_CONFIGURATION = RubyApiIconGroup.testunitTestunitrunconfiguration();
	final Image RAILS_SERVER_RUN_CONFIGURATION = RubyApiIconGroup.railsRails();


	final Image RUBY_RUNNER_SHOW_CMDLINE = PlatformIconGroup.actionsPreview();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Misc
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	final Image RUBY_GUTTER_OVERRIDING = PlatformIconGroup.gutterOverridingmethod();
	final Image RUBY_GUTTER_IMPLEMENTING = PlatformIconGroup.gutterImplementingmethod();
}

