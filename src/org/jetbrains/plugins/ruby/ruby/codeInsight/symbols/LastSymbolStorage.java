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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RComponents;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Aug 18, 2007
 */
public class LastSymbolStorage implements ProjectComponent {
    @Override
	public void projectOpened() {
    }

    @Override
	public void projectClosed() {
    }

    @Override
	@NonNls
    @NotNull
    public String getComponentName() {
        return RComponents.RLAST_SYMBOL_STORAGE;
    }

    @Override
	public void initComponent() {
    }

    @Override
	public void disposeComponent() {
    }

    private FileSymbol lastEvaluatedSymbol;

    @Nullable
    public synchronized FileSymbol getSymbol() {
        return lastEvaluatedSymbol;
    }

    public synchronized void setSymbol(@Nullable final FileSymbol symbol) {
        lastEvaluatedSymbol = symbol;
    }

    @NotNull
    public static LastSymbolStorage getInstance(@NotNull final Project project){
        return project.getComponent(LastSymbolStorage.class);
    }
}
