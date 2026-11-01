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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache;

import consulo.annotation.component.ComponentScope;
import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.language.psi.PsiModificationTracker;
import consulo.module.content.layer.event.ModuleRootEvent;
import consulo.module.content.layer.event.ModuleRootListener;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.module.Module;
import consulo.project.Project;
import consulo.project.startup.StartupManager;
import consulo.util.lang.ref.SoftReference;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.plugins.ruby.RBundle;
import consulo.ruby.interop.RubyInterop;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.AbstractCachedSymbol;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 *
 * @author oleg
 * @date Jun 17, 2007
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class SymbolsCache implements Disposable {

    // Here we store info about modifiable symbols
    private final Map<CacheKey, SoftReference<CachedSymbol>> mySoftCache = new ConcurrentHashMap<>();

    // Here we store info about built-in symbols
    private final Map<CacheKey, CachedSymbol> myBuiltInCache = new ConcurrentHashMap<>();
    // the per file symbols are built from the PSI: any change makes them stale
    private volatile long myPsiModificationCount = -1;

    private Project myProject;

    @SuppressWarnings({"UnusedDeclaration"})
    @Nonnull
    public static SymbolsCache getInstance(@Nonnull final Project project) {
        return project.getInstance(SymbolsCache.class);
    }

    @Inject
    public SymbolsCache(@Nonnull final Project project) {
        myProject = project;
        // the SDK roots, the load path settings and the libraries are all in the roots
        project.getMessageBus().connect(this).subscribe(ModuleRootListener.class, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                clearCaches();
            }
        });
    }

    public void clearCaches() {
        myBuiltInCache.clear();

        clearCachesExceptBuiltIn();
    }

    public void clearCachesExceptBuiltIn() {
        mySoftCache.clear();
        LastSymbolStorage.getInstance(myProject).setSymbol(null);
    }

    public void recreateAllBuiltInCaches() {
        recreateBuiltInCaches(RModuleUtil.getAllModulesWithRubySupport(myProject));
    }

    public void recreateBuiltInCaches(final Module[] modules) {
        clearCaches();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {

                // Here we build required caches
                final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                if (indicator != null) {
                    indicator.setIndeterminate(true);
                }

                // Here we gather info about sdks and builin cache types required
                for (Module module : modules) {
                    final Sdk sdk = RModuleUtil.getModuleSdk(module);
                    if (sdk != null) {
                        final boolean isInteropEnabled = RubyInterop.isEnabled(module);
                        if (RailsFacetUtil.hasRailsSupport(module)) {
                            SymbolCacheUtil.getFileSymbol(getBuiltInCachedSymbol(FileSymbolType.RAILS_BUILT_IN, sdk, isInteropEnabled));
                        }
                        else if (RModuleUtil.hasRubySupport(module)) {
                            SymbolCacheUtil.getFileSymbol(getBuiltInCachedSymbol(FileSymbolType.BUILT_IN, sdk, isInteropEnabled));
                        }
                    }
                }

            }
        };
        // never a modal dialog: the startup activity is in the background already
        final Runnable task = () -> ReadAction.run(runnable::run);
        if (Application.get().isDispatchThread()) {
            Application.get().executeOnPooledThread(task);
        }
        else {
            task.run();
        }
    }

    @Override
    public void dispose() {
        mySoftCache.clear();
        myBuiltInCache.clear();
    }


    /**
     * Extract modifiable fileSymbol from modifiable cache
     *
     * @param type         Type
     * @param url          Url for file
     * @param module       file module
     * @param sdk          sdk for file
     * @param interopEnabled is interop enabled
     * @return up2dated FileSymbol
     */
    @Nullable
    public CachedSymbol getModifiableCachedSymbol(@Nonnull final FileSymbolType type, @Nullable final String url, @Nullable final Module module, @Nullable final Sdk sdk, final boolean interopEnabled) {
        final long modificationCount = PsiModificationTracker.getInstance(myProject).getModificationCount();
        if (modificationCount != myPsiModificationCount) {
            myPsiModificationCount = modificationCount;
            mySoftCache.clear();
        }

        final CacheKey key = new CacheKey(type, url, module, sdk, interopEnabled);
        SoftReference<CachedSymbol> reference = mySoftCache.get(key);
        CachedSymbol cachedSymbol = reference != null ? reference.get() : null;
        if (cachedSymbol != null) {
            return cachedSymbol;
        }
        cachedSymbol = CachedSymbolFactory.createCachedSymbol(type, url, myProject, module, sdk, interopEnabled);
        if (cachedSymbol == null) {
            removeKey(key);
            return null;
        }
        ((AbstractCachedSymbol) cachedSymbol).setKey(key);
        reference = new SoftReference<CachedSymbol>(cachedSymbol);
        mySoftCache.put(key, reference);
        return cachedSymbol;
    }

    /**
     * Extract modifiable fileSymbol from modifiable cache
     *
     * @param type         Type
     * @param module       file module
     * @param sdk          sdk for file
     * @param interopEnabled is interop enabled
     * @return up2dated FileSymbol
     */
    @Nullable
    public CachedSymbol getCachedSymbol(@Nonnull final FileSymbolType type, @Nullable final Module module, @Nullable final Sdk sdk, final boolean interopEnabled) {
        return getModifiableCachedSymbol(type, null, module, sdk, interopEnabled);
    }

    /**
     * Extract builtin fileSymbol from builtin caches
     *
     * @param type         Type of builtin fileSymbol
     * @param sdk          sdk for filesymbol
     * @param interopEnabled is interop enabled
     * @return up2dated FileSymbol
     */
    @Nullable
    public CachedSymbol getBuiltInCachedSymbol(@Nonnull final FileSymbolType type, @Nullable final Sdk sdk, final boolean interopEnabled) {
        final String cacheKeyUrl = SymbolCacheUtil.getStubUrlByType(type, sdk, interopEnabled);
        if (cacheKeyUrl == null) {
            return null;
        }

        final CacheKey key = new CacheKey(type, cacheKeyUrl, null, sdk, interopEnabled);
        CachedSymbol builtInCachedSymbol = myBuiltInCache.get(key);

        if (builtInCachedSymbol == null) {
            builtInCachedSymbol = CachedSymbolFactory.createCachedSymbol(type, cacheKeyUrl, myProject, null, sdk, interopEnabled);
            if (builtInCachedSymbol == null) {
                return null;
            }
            myBuiltInCache.put(key, builtInCachedSymbol);
        }
        return builtInCachedSymbol;
    }

    public void removeKey(@Nonnull final CacheKey key) {
        mySoftCache.remove(key);
    }
}