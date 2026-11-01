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

package org.jetbrains.plugins.ruby.ruby.lang.psi.assoc;

import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Dec 26, 2007
 */
public interface RAssoc extends RPsiElement
{
	/**
	 * @return the key expression of {@code key => value}, null for {@code key: value} where the key is a label
	 */
	@Nullable
	public RPsiElement getKey();

	/**
	 * @return the key as a name: {@code key} for {@code key: value}, {@code :key => value} and {@code "key" => value},
	 * the key text for other keys
	 */
	@Nullable
	public String getKeyName();

	@Nullable
	public RPsiElement getValue();
}
