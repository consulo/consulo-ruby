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

package org.jetbrains.plugins.ruby.rails.facet.converting;

import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.facet.FacetTypeRegistry;
import org.jetbrains.plugins.ruby.rails.facet.versions.BaseRailsFacet;
import org.jetbrains.plugins.ruby.rails.facet.versions.BaseRailsFacetType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Jun 9, 2008
 */
public class RailsModule_ConvertingContext {
     private Map<String, FacetTypeId<? extends BaseRailsFacet>> myModuleName2Type = new HashMap<String, FacetTypeId<? extends BaseRailsFacet>>();

  public void registerModule(String moduleName, FacetTypeId<? extends BaseRailsFacet> type) {
    myModuleName2Type.put(moduleName, type);
  }

  public static String getStringId(final FacetTypeId<? extends BaseRailsFacet> type) {
    final FacetType<?,?> facetType = FacetTypeRegistry.getInstance().findFacetType(type);
    if (!(facetType instanceof BaseRailsFacetType)) {
      throw new AssertionError(type);
    }
    return facetType.getStringId();
  }

  protected FacetTypeId<? extends BaseRailsFacet> getFacetType(final String moduleName) {
    return myModuleName2Type.get(moduleName);
  }
}
