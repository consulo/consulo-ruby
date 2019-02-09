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

package org.jetbrains.plugins.ruby.rails.presentation;

import javax.annotation.Nonnull;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable;
import consulo.awt.TargetAWT;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;
import org.jetbrains.plugins.ruby.rails.RailsIcons;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import org.jetbrains.plugins.ruby.ruby.presentation.RContainerPresentationUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 02.03.2007
 */
public class RControllerPresentationUtil
{
	public static Image getIcon()
	{
		return RailsIcons.RAILS_CONTROLLER_NODE;
	}

	/**
	 * Computes icon for RVirtualClass.
	 * Be careful, if flags contains information about visibility, method uses
	 * RIconsUtils.getIconWithModifiers()
	 *
	 * @param rClass RVirtualClass
	 * @param flags  com.intellij.openapi.util.Iconable flags
	 * @return Icon
	 */
	public static Image getIcon(@Nonnull final RVirtualClass rClass, final int flags)
	{
		if((flags & Iconable.ICON_FLAG_VISIBILITY) == Iconable.ICON_FLAG_VISIBILITY)
		{
			final AccessModifier modifier = rClass.getAccessModifier();
			return ImageEffects.appendRight(getIcon(), RContainerPresentationUtil.getIconForAccessModifier(modifier));
		}
		return getIcon();
	}

	public static ItemPresentation getPresentation(@Nonnull final RVirtualClass rClass)
	{
		final Image icon = getIcon(rClass, Iconable.ICON_FLAG_VISIBILITY);
		return new PresentationData(rClass.getName(), RContainerPresentationUtil.getLocation(rClass), TargetAWT.to(icon), null);
	}
}
