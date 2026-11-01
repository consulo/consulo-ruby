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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl;

import consulo.content.bundle.Sdk;
import consulo.module.Module;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.FileSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CachedSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolCacheUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Oct 20, 2007
 */
public abstract class AbstractLayeredCachedSymbol extends AbstractCachedSymbol {
    // All the list of required files
    private Set<String> myAllExternalUrls;
    protected final boolean isInteropEnabled;

    public AbstractLayeredCachedSymbol(@Nonnull final Project project, @Nullable final Module module, @Nullable final Sdk sdk, final boolean interopEnabled) {
        super(project, module, sdk);
        isInteropEnabled = interopEnabled;
    }

    @Nullable
    protected abstract CachedSymbol getBaseSymbol();

    @Override
    protected final void fileChanged(@Nonnull String url) {
        if (myFileSymbol == null) {
            return;
        }
        if (myAllExternalUrls == null || myAllExternalUrls.contains(url)) {
            myFileSymbol = null;
        }
    }

    @Override
    protected void updateFileSymbol() {
        myFileSymbol = new FileSymbol(SymbolCacheUtil.getFileSymbol(getBaseSymbol()), myProject, isInteropEnabled);
        addAdditionalData();
        myAllExternalUrls = FileSymbolUtil.getUrls(myFileSymbol);
    }

    protected abstract void addAdditionalData();
}
