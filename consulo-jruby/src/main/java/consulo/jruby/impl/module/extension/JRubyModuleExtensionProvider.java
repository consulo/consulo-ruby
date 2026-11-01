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

package consulo.jruby.impl.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.jruby.icon.JRubyIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class JRubyModuleExtensionProvider implements ModuleExtensionProvider<JRubyModuleExtension>
{
	@Override
	public String getId()
	{
		return "jruby";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "java";
	}

	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("JRuby");
	}

	@Override
	public Image getIcon()
	{
		return JRubyIconGroup.jruby();
	}

	@Override
	public ModuleExtension<JRubyModuleExtension> createImmutableExtension(ModuleRootLayer moduleRootLayer)
	{
		return new JRubyModuleExtension(getId(), moduleRootLayer);
	}

	@Override
	public MutableModuleExtension<JRubyModuleExtension> createMutableExtension(ModuleRootLayer moduleRootLayer)
	{
		return new JRubyMutableModuleExtension(getId(), moduleRootLayer);
	}
}
