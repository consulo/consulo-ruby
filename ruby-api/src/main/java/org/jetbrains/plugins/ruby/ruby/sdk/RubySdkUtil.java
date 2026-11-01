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

package org.jetbrains.plugins.ruby.ruby.sdk;

import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.PlatformAwareSdkType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkType;
import consulo.content.bundle.SdkTypeId;
import consulo.platform.Platform;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.ruby.sdk.gemRootType.GemOrderRootType;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 17, 2007
 */
public class RubySdkUtil
{
	@NonNls
	private static final String GEM_LIB_DIR = File.separator + "lib";


	/**
	 * Checks if sdk is a Ruby SDK
	 *
	 * @param sdk sdk to inspect
	 * @return true, if sdk is Ruby type
	 */
	public static boolean isKindOfRubySDK(@Nullable final Sdk sdk)
	{
		return sdk != null && sdk.getSdkType() instanceof RubySdkType;
	}

	public static boolean isSDKValid(@Nullable final Sdk sdk)
	{
		return isKindOfRubySDK(sdk) && isSDKHomeExist(sdk);
	}

	public static boolean isSDKHomeExist(@Nullable final Sdk sdk)
	{
		if(sdk == null)
		{
			return false;
		}
		final VirtualFile sdkHomeDir = sdk.getHomeDirectory();
		if(sdkHomeDir == null)
		{
			return false;
		}
		if(sdk.getSdkType() instanceof PlatformAwareSdkType platformAwareSdkType)
		{
			return platformAwareSdkType.isValidSdkHome(Platform.current(), Path.of(sdkHomeDir.getPath()));
		}
		return ((SdkType) sdk.getSdkType()).isValidSdkHome(sdkHomeDir.getPath());
	}

	/**
	 * @param sdk Ruby SDK
	 * @return Gems bin folder path for given GEM.
	 */
	@Nonnull
	public static String getGemsBinFolderPath(@Nonnull final Sdk sdk)
	{
		return RubySdkType.getInstance().getGemsBinDirectory(sdk);
	}

	private static String[] getSdkContentRootUrls(@Nonnull final Sdk sdk)
	{
		return sdk.getRootProvider().getUrls(BinariesOrderRootType.ID);
	}

	/**
	 * Returns all the roots with all gems
	 *
	 * @param sdk Sdk to get roots for
	 * @return array of roots
	 */
	public static String[] getSdkRootsWithAllGems(@Nonnull final Sdk sdk)
	{
		final ArrayList<String> urls = new ArrayList<>();
		for(String rootUrl : sdk.getRootProvider().getUrls(BinariesOrderRootType.ID))
		{
			if(isGemsRootUrl(rootUrl))
			{
				final VirtualFileManager manager = VirtualFileManager.getInstance();
				final VirtualFile gemsRoot = manager.findFileByUrl(rootUrl);
				if(gemsRoot != null)
				{
					urls.addAll(getAllGemsLibUrls(gemsRoot));
				}
			}
			else
			{
				urls.add(rootUrl);
			}
		}
		return urls.toArray(new String[urls.size()]);
	}

	@Nullable
	public static String getRubyStubsDirUrl(@Nonnull final Sdk sdk)
	{
		for(String url : getSdkContentRootUrls(sdk))
		{
			if(isRubystubsDirUrl(url))
			{
				return url;
			}
		}
		return null;
	}

	/**
	 * Determinates whether directory ends with "/gems" e.g. "gem" subdirectory of some gems lookup path
	 *
	 * @param url directory url
	 * @return if directory is gems root directory
	 */
	public static boolean isGemsRootUrl(@Nonnull final String url)
	{
		return url.endsWith(RubySdkType.GEMS_SUBDIR);
	}

	public static boolean isRubystubsDirUrl(@Nonnull final String url)
	{
		return url.endsWith(RubySdkType.RUBYSTUBS_DIR);
	}


	static void addToSourceAndClasses(@Nonnull final SdkModificator sdkModificator, @Nullable final VirtualFile vFile)
	{
		if(vFile != null)
		{
			sdkModificator.addRoot(vFile, BinariesOrderRootType.ID);
			sdkModificator.addRoot(vFile, SourcesOrderRootType.ID);
		}
	}


	@Nonnull
	public static List<String> getAllGemsLibUrls(@Nonnull final VirtualFile gemRoot)
	{
		final List<String> gemsUrls = new ArrayList<>();
		for(VirtualFile file : gemRoot.getChildren())
		{
			gemsUrls.add(file.getUrl() + GEM_LIB_DIR);
		}
		return gemsUrls;
	}

	@Nonnull
	public static List<GemInfo> getAllGems(@Nonnull final VirtualFile gemRoot)
	{
		final List<GemInfo> gemsUrls = new ArrayList<>();
		for(VirtualFile gemFile : gemRoot.getChildren())
		{
			gemsUrls.add(GemInfo.create(gemFile));
		}
		return gemsUrls;
	}

	/**
	 * Returns presentable version of url. If url is to one of gems,
	 * then /gem_name/ is added to presentation
	 *
	 * @param sdk Current ruby sdk
	 * @param url Url to get presentable text for
	 * @return String - presentable text
	 */
	@Nullable
	public static String getPresentableLocation(@Nullable final Sdk sdk, @Nonnull final String url)
	{
		if(sdk != null && RubySdkUtil.isKindOfRubySDK(sdk))
		{
			final String[] gemsRoots = sdk.getRootProvider().getUrls(GemOrderRootType.ID);
			for(String gemsRoot : gemsRoots)
			{
				if(url.startsWith(gemsRoot))
				{
					final String gemName = GemInfo.getGemNameByUrl(gemsRoot, url);
					if(gemName != null)
					{
						return ".../" + gemName + "/.../" + VirtualFileUtil.getFileName(url);
					}
				}
			}
		}
		return null;
	}

	public static String getVMExecutablePath(Sdk sdk)
	{
		SdkTypeId sdkType = sdk.getSdkType();
		if(sdkType instanceof RubySdkType)
		{
			return ((RubySdkType) sdkType).getVMExecutablePath(sdk);
		}
		return null;
	}

	public static String getBinPath(Sdk sdk)
	{
		SdkTypeId sdkType = sdk.getSdkType();
		if(sdkType instanceof RubySdkType)
		{
			return ((RubySdkType) sdkType).getBinPath(sdk);
		}
		return null;
	}
}
