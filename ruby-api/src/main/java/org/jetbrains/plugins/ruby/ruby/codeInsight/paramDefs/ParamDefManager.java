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

package org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs;

import consulo.application.Application;
import java.util.HashMap;
import java.util.Map;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import jakarta.inject.Singleton;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import consulo.ide.ServiceManager;

/**
 * @author yole
 */
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
@Singleton
public class ParamDefManager
{
	public static ParamDefManager getInstance()
	{
		return ServiceManager.getService(ParamDefManager.class);
	}

	private final Map<String, ParamDef[]> myParamDefs = new HashMap<String, ParamDef[]>();
	private final Map<String, ParamDef[]> myParamDefsByMethodName = new HashMap<String, ParamDef[]>();

	private volatile boolean myProvidersLoaded;

	public void registerParamDef(String methodFQN, ParamDef... paramDefs)
	{
		myParamDefs.put(methodFQN, paramDefs);
		final int delimiter = methodFQN.lastIndexOf("::");
		myParamDefsByMethodName.putIfAbsent(delimiter >= 0 ? methodFQN.substring(delimiter + 2) : methodFQN, paramDefs);
	}

	/**
	 * Registers the definitions of another method for a method with the same parameters
	 */
	public void copyParamDefs(String methodFQN, String sourceMethodFQN)
	{
		final ParamDef[] paramDefs = myParamDefs.get(sourceMethodFQN);
		if(paramDefs != null)
		{
			registerParamDef(methodFQN, paramDefs);
		}
	}

	public ParamDef[] getParamDefs(Symbol method)
	{
		String path = SymbolUtil.getPresentablePath(method);
		return getParamDefs(path);
	}

	public ParamDef[] getParamDefs(final String methodFQN)
	{
		loadProviders();
		return myParamDefs.get(methodFQN);
	}

	/**
	 * @return the definitions of the first method registered under the name, for calls the symbols do not resolve
	 */
	public ParamDef[] getParamDefsByMethodName(final String methodName)
	{
		loadProviders();
		return myParamDefsByMethodName.get(methodName);
	}

	private void loadProviders()
	{
		if(myProvidersLoaded)
		{
			return;
		}
		synchronized(myParamDefs)
		{
			if(!myProvidersLoaded)
			{
				Application.get().getExtensionPoint(ParamDefProvider.class).forEach(provider -> provider.registerParamDefs(this));
				myProvidersLoaded = true;
			}
		}
	}
}
