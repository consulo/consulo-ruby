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
package org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes;

import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.formatter.models.wrap.RWrapLastChild;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RFormatStructureElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RClassObject;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RFieldConstantContainer;

/**
 * @author oleg
 * @since 2006-07-18
 */
public interface RObjectClass extends RFieldConstantContainer, RFormatStructureElement, RWrapLastChild {
    @Nullable
    public RClassObject getObject();

    @Nonnull
    public LocalizeValue getPresentableName();
}
