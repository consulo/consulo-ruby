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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualClass;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.formatter.models.wrap.RWrapLastChild;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RFormatStructureElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RClassName;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RSuperClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RFieldConstantContainer;
import com.intellij.psi.PsiNamedElement;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 18.07.2006
 */
public interface RClass extends RVirtualClass, RFieldConstantContainer, RFormatStructureElement, RWrapLastChild, PsiNamedElement, RContainer
{
	@Nullable
	public RClassName getClassName();

	@Nullable
	public RSuperClass getPsiSuperClass();

	@Nonnull
	public List<RClass> getSuperClass(@Nonnull FileSymbol fileSymbol);
}
