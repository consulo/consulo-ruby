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

import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RObjectClass;

import jakarta.annotation.Nonnull;

import consulo.ui.ex.tree.PresentationData;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;

/**
 * For ".self" etc.
 *
 * @author oleg
 * @since 2007-03-13
 */
public class RObjectClassPresentationUtil {
    public static Image getIcon() {
        return RubyIcons.RUBY_OBJECT_CLASS_NODE;
    }

    @Nonnull
    public static ItemPresentation getPresentation(@Nonnull RObjectClass objectClass) {
        Image icon = getIcon();
        return new PresentationData(objectClass.getPresentableName(), TextUtil.wrapInParens(getLocation(objectClass)), icon, null);
    }

    private static String getLocation(RObjectClass objectClass) {
        return RContainerPresentationUtil.getLocation(objectClass);
    }
}
