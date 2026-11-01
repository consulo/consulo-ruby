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

package org.jetbrains.plugins.ruby.ruby.presentation;

import jakarta.annotation.Nonnull;

import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nullable;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RubyCallType;
import consulo.ui.ex.tree.PresentationData;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 13, 2007
 */
public class ForeignImportPresentationUtil
{
	@Nullable
	public static ItemPresentation getIncludeJavaPresentation(@Nonnull final RCallBase rCall)
	{
		if(rCall.getCallType() != RubyCallType.INCLUDE_CLASS_CALL)
		{
			return null;
		}
		final Image icon = getIncludeIcon();
		return new PresentationData(rCall.getText(), TextUtil.wrapInParens(getLocation(rCall)), icon, null);
	}

	public static Image getIncludeIcon()
	{
		final Image icon = RubyInterop.compute(RubyInteropProvider::getIcon);
		return icon != null ? icon : PlatformIconGroup.nodesPackage();
	}

	public static String getLocation(@Nonnull final RCall call)
	{
		return RContainerPresentationUtil.getLocation(call);
	}
}
