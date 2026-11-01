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

package org.jetbrains.plugins.ruby.addins.rspec.run.configuration;

import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.AbstractRTestsRunConfigurationParams;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: May 22, 2008
 */
public interface RSpecRunConfigurationParams extends AbstractRTestsRunConfigurationParams
{
	public String getSpecArgs();

	/**
	 * @return the line of the example or group to run in the spec file
	 */
	public int getExampleLine();

	public void setSpecArgs(String specArgs);

	public void setExampleLine(int exampleLine);
}
