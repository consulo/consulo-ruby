package org.jetbrains.plugins.ruby.ruby.roots;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ContentFolderType;
import com.intellij.openapi.roots.ModuleRootManager;

/**
 * @author VISTALL
 * @since 24.09.13.
 */
public class RubyModuleRootUtil
{
	public static boolean isUnderTestUnitRoot(@NotNull Project project, @NotNull final String url)
	{
		ModuleManager moduleManager = ModuleManager.getInstance(project);

		for(Module module : moduleManager.getModules())
		{
			ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
			for(ContentEntry contentEntry : moduleRootManager.getContentEntries())
			{
				for(String t : contentEntry.getFolderUrls(ContentFolderType.TEST))
				{
					if(url.equals(t))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}