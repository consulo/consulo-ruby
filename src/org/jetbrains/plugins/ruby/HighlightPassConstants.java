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

package org.jetbrains.plugins.ruby;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Jul 5, 2007
 */
public interface HighlightPassConstants
{

	// Constants for ruby additional highlight pass

	public static final int RUBY_HIGHLIGHTER_RANGE_GROUP = 0x200;
	public static final int RUBY_LINE_MARKERS_GROUP = 0x400;

	public static final int CONTROLLER_TO_VIEW_MARKERS_GROUP = 0x800;
	public static final int VIEW_TO_ACTION_MARKERS_GROUP = 0x1600;
	public static final int VIEW_TO_CONTROLLER_MARKERS_GROUP = 0x3200;
}
