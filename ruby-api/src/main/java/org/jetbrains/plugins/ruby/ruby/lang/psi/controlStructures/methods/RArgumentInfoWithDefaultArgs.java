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

package org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 25.04.2007
 */
public class RArgumentInfoWithDefaultArgs extends ArgumentInfo
{
	private String myDefValue;

	public RArgumentInfoWithDefaultArgs(@Nonnull final String name, @Nullable final String defValue, final Type type)
	{
		super(name, type);
		myDefValue = defValue;
	}

	@Override
	public String getPresentableName()
	{
		return getName() + "=" + myDefValue;
	}
}
