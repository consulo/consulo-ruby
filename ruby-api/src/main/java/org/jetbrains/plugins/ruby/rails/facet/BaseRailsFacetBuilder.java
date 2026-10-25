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

package org.jetbrains.plugins.ruby.rails.facet;

import com.intellij.facet.Facet;
import consulo.application.Application;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.application.progress.Task;
import consulo.content.ContentIterator;
import consulo.content.bundle.Sdk;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserFactory;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ModuleFileIndex;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;
import consulo.project.startup.StartupManager;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.Messages;
import consulo.util.lang.function.ThrowableRunnable;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.VirtualFile;
import consulo.util.io.FileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecModuleSettings;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecUtil;
import org.jetbrains.plugins.ruby.rails.RailsUtil;
import org.jetbrains.plugins.ruby.rails.actions.generators.GeneratorsUtil;
import org.jetbrains.plugins.ruby.rails.facet.configuration.BaseRailsFacetConfigurationLowLevel;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.RailsWizardSettingsHolder;
import org.jetbrains.plugins.ruby.rails.facet.versions.BaseRailsFacet;
import org.jetbrains.plugins.ruby.rails.module.view.RailsViewFoldersManager;
import org.jetbrains.plugins.ruby.rails.plugins.PluginsUtil;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.run.RubyScriptRunner;
import org.jetbrains.plugins.ruby.ruby.run.RunContentDescriptorFactory;
import org.jetbrains.plugins.ruby.settings.RApplicationSettings;
import org.jetbrains.plugins.ruby.support.utils.IdeaInternalUtil;
import org.jetbrains.plugins.ruby.support.utils.OSUtil;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Apr 10, 2008
 */
public class BaseRailsFacetBuilder
{
	private static final Logger LOG = Logger.getInstance(BaseRailsFacetBuilder.class.getName());

	/**
	 * Initializes settings of new facet instance.(for new created facet or loaded from disk)
	 *
	 * @param railsFacet Rails Facet
	 */
	public static void initFacetInstance(final BaseRailsFacet railsFacet)
	{
		// if you will need sdk, you should get it from initFacetInstance() signature,
		// because facet doesn't know sdk here
		final BaseRailsFacetConfigurationLowLevel conf = (BaseRailsFacetConfigurationLowLevel) railsFacet.getConfiguration();

		final Module uncommitedModule = railsFacet.getModule();
		conf.setModule(uncommitedModule);

		if(!conf.isInitialized())
		{
			BaseRailsFacetBuilder.setupGreenhornFacetForExistingModuleInstance(railsFacet);
		}
		else
		{
			// new instance of facet on open old (existing) project
			configureRakeTasksAndGenerators(railsFacet, conf.getSdk());
		}
	}


	/**
	 * Setups new greehorn facet for already loaded module.
	 * <p/>
	 * Is being invoked on Rails/JRails new instance
	 * * from Module Settings dialog
	 * * from OnTheFly facet detector
	 * This method setups facet if it was created from Module Settings dialog
	 * (not form New Module/Project Wizard and not deserialized)
	 * In such case we should setup RailsApplicationHome, Rake Tasks Actions and Generators
	 *
	 * @param railsFacet Commited Rails/JRails Facet
	 */
	public static void setupGreenhornFacetForExistingModuleInstance(final BaseRailsFacet railsFacet)
	{
		final BaseRailsFacetConfigurationLowLevel conf = (BaseRailsFacetConfigurationLowLevel) railsFacet.getConfiguration();

		final Module uncommitedModule = railsFacet.getModule();
		final Project project = uncommitedModule.getProject();

		assert !conf.isInitialized();

		final Sdk sdk = RModuleUtil.getModuleOrJRubyFacetSdk(uncommitedModule);
		///////////////// Rails Home Root /////////////////////////////////////////////////
		// Can occur only by adding new Rails/JRails Facet from Module Settings Dialog!
		final Ref<String> railsFacetHomePathRef = new Ref<String>();

		final ModuleRootManager rootManager = ModuleRootManager.getInstance(uncommitedModule);
		if(conf.getNullableRailsApplicationRootPath() != null)
		{
			String railsApplicationRootPath = conf.getNullableRailsApplicationRootPath();
			assert railsApplicationRootPath != null; //only for inspection
			setupGreenhornFacet(railsFacet, rootManager, railsApplicationRootPath, sdk);
		}
		else
		{
			//search rails under module content
			ProgressManager.getInstance().run(new Task.Modal(project, RBundle.message("rails.facet.builder.rails.application.searching"), true)
			{
				@Override
				public void run(final ProgressIndicator indicator)
				{
					indicator.setText(RBundle.message("progress.indicator.title.directories.scanning"));
					indicator.setText2(RBundle.message("progress.indicator.title.please.wait"));
					railsFacetHomePathRef.set(searchRailsAppInUnderDirectory(uncommitedModule, indicator));
				}
			});

			//setup rails application home path
			if(railsFacetHomePathRef.get() != null)
			{
				setupGreenhornFacet(railsFacet, rootManager, railsFacetHomePathRef.get(), sdk);
			}
			else
			{
				//Default App Home
				final String defaultAppHome = railsFacet.getDefaultRailsApplicationHomePath(rootManager.getModifiableModel());
				//Folder chooser
				final FileChooserDescriptor desc = FileChooserDescriptorFactory.createSingleFolderDescriptor();
				desc.setTitle(RBundle.message("rails.facet.builder.rails.application.choose.title"));
				desc.setDescription(RBundle.message("rails.facet.builder.rails.application.choose.relative.path"));
				//   desc.setContextModule(uncommitedModule);
				desc.setRoots(VirtualFileUtil.findFileByLocalPath(defaultAppHome));
				desc.setIsTreeRootVisible(true);
				//Folder Chooser Dialog
				FileChooserFactory.getInstance().newBuilder(desc).project(project).showSingle().whenComplete((path, error) -> {
					String railsApplicationRootPath = path != null ? FileUtil.toSystemIndependentName(path.toString()) : defaultAppHome;
					setupGreenhornFacet(railsFacet, rootManager, railsApplicationRootPath, sdk);
				});
			}
		}
	}

	private static void setupGreenhornFacet(BaseRailsFacet railsFacet, ModuleRootManager rootManager, String railsApplicationRootPath, @Nullable Sdk sdk)
	{
		final BaseRailsFacetConfigurationLowLevel conf = (BaseRailsFacetConfigurationLowLevel) railsFacet.getConfiguration();
		final Module uncommitedModule = railsFacet.getModule();

		// inits railsAppRootPath, SDK, etc
		initGreenhornFacet(railsFacet, rootManager.getModifiableModel(), railsApplicationRootPath, sdk);

		if(RailsUtil.containsRailsApp(conf.getRailsApplicationRootPath()))
		{
			///////////////// Rake tasks and Generators /////////////////////////////////////////////////
			configureRakeTasksAndGenerators(railsFacet, sdk);

			//////////////// Additional Sources, Rails View Custom folders
			configureRailsSourcePathes(RailsViewFoldersManager.getInstance(uncommitedModule), conf.getRailsApplicationRootPathUrl(), RSpecModuleSettings.getInstance(uncommitedModule).shouldUseRSpecTestFramework(), uncommitedModule);

		}
	}

	/**
	 * Setups new facet after commiting Facet Module
	 *
	 * @param railsFacet          Rails Facet
	 * @param rootModel           Modifiable Root Model
	 * @param applicationHomePath Rails Application Home Directory
	 * @param sdk                 SDK
	 */
	public static void initGreenhornFacet(final BaseRailsFacet railsFacet, @Nonnull final ModifiableRootModel rootModel, @Nonnull final String applicationHomePath, @Nullable final Sdk sdk)
	{

		/**
		 * WARNING: Module Root Model is uncommited!!!!!
		 */
		//        final Module uncommitedModule = rootModel.getModule();

		///////////////// Facet Configuration //////////////////////////////////////////////////////////
		final BaseRailsFacetConfigurationLowLevel facetConfiguration = (BaseRailsFacetConfigurationLowLevel) railsFacet.getConfiguration();

		facetConfiguration.setRailsApplicationRootPath(applicationHomePath);
		facetConfiguration.setInitialized();
		facetConfiguration.setSdk(sdk);
	}

	/**
	 * Setups facet after commiting Facet Module
	 *
	 * @param rootModel  Modifiable Root Module
	 * @param railsFacet Rails Facet
	 * @param settings   Wizard ettings
	 */
	public static void setupGreenhornFacet(final ModifiableRootModel rootModel, final BaseRailsFacet railsFacet, @Nonnull final RailsWizardSettingsHolder settings)
	{

		final RailsWizardSettingsHolder.RSpecConfiguration rSpecConfiguration = settings.getRSpecConf();
		/**
		 * WARNING: Module Root Model is uncommited!!!!!
		 */
		final Module uncommitedModule = rootModel.getModule();
		/**
		 * We use module of facet SDK. Current implementation of facets
		 * invokes this before module RootModel commiting, but after facet was committed
		 * Thus SDK in case of jruby - is available from uncommited module,
		 * but in case of ruby module it is imposible to get it from module.
		 *
		 * Thus we should take SDK from settings and use it. This behaviour works both with
		 * jruby facet and ruby module type.
		 */
		final Sdk sdk = settings.getSdk();

		final String applicationHomePath = RailsFacetUtil.getRailsAppHomeDirPath(uncommitedModule);
		assert applicationHomePath != null;

		//////////////// Install components ////////////////////////////////////////////////////////////
		if(sdk != null)
		{
			// components installation require executing scripts and usind SDK
			installComponents(uncommitedModule, railsFacet, sdk, settings, applicationHomePath);
		}

		//////////////// Test::Unit test folder /////////////////////////////////////////////////////////////

		final StandardRailsPaths paths = railsFacet.getConfiguration().getPaths();

		final List<String> testUnitUrls = new ArrayList<String>(3);
		testUnitUrls.add(paths.getTestsStdUnitRootURL());
		//   rootManager.setTestUnitFolderUrls(testUnitUrls);

		///////////////// Creating rails run configurations ////////////////////////////////////////////////////////////////////
		RailsUtil.createRailsRunConfiguration(uncommitedModule);

		///////////////// Application Settings /////////////////////////
		final String svnPath = rSpecConfiguration.getSvnPath();
		if(!TextUtil.isEmpty(svnPath) && !OSUtil.isSVNInIDEALoadPath())
		{
			final RApplicationSettings ideaAppSettings = RApplicationSettings.getInstance();
			//noinspection ConstantConditions
			ideaAppSettings.additionalEnvPATH = OSUtil.appendToPATHenvVariable(ideaAppSettings.additionalEnvPATH, svnPath);
		}

		///////////////// Module Settings /////////////////////////
		// RSpec
		final RSpecModuleSettings.RSpecSupportType rSpecType;
		if(!settings.isRSpecSupportEnabled())
		{
			rSpecType = RSpecModuleSettings.RSpecSupportType.NONE;
		}
		else
		{
			rSpecType = rSpecConfiguration.shouldInstallRSpecPlugin() ? RSpecModuleSettings.RSpecSupportType.RAILS_PLUGIN : RSpecModuleSettings.RSpecSupportType.GEM;

		}
		RSpecModuleSettings.getInstance(uncommitedModule).setRSpecSupportType(rSpecType);

		///////////////// Synch files /////////////////
		//        RModuleUtil.refreshRubyModuleTypeContent(module);
	}

	private static void generateRailsApplication(final Module module, @Nonnull final Sdk sdk, final RunContentDescriptorFactory descriptorFactory, @Nonnull final RailsWizardSettingsHolder settings, @Nonnull final String applicationHomePath, @Nullable final Runnable onDone)
	{

		if(settings.getAppGenerateWay() == RailsWizardSettingsHolder.Generate.NEW)
		{
			final String preconfigureForDBName = settings.getDBNameToPreconfigure();

			// if directory for rails application home already exists
			// then try to find Rails Application in it
			final VirtualFile appHomeDir = VirtualFileUtil.findFileByLocalPath(applicationHomePath);

			// If rails application already exists ask user overwrite existing files or not?
			final boolean toOverwrite;
			if(appHomeDir != null && appHomeDir.isDirectory() &&
					RailsUtil.containsRailsApp(applicationHomePath))
			{
				final int dialogResult = Messages.showYesNoDialog(module.getProject(), RBundle.message("module.rails.generateapp.rails.new.overwrite.message", applicationHomePath), RBundle.message("module.rails.generateapp.rails.new.overwrite.title"), Messages.getQuestionIcon());
				toOverwrite = dialogResult == DialogWrapper.OK_EXIT_CODE;
			}
			else
			{
				toOverwrite = false;
			}
			RailsUtil.generateRailsApp(module, sdk, applicationHomePath, toOverwrite, preconfigureForDBName, descriptorFactory, onDone);
		}
	}

	private static Runnable createOnRailsAppGenerated(final RunContentDescriptorFactory.PinTabsFactory descFactory, final Module uncommitedModule, final Sdk sdk, final BaseRailsFacet railsFacet, final RailsWizardSettingsHolder settings, final String applicationHomePath, final Project project, final String moduleName)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				// Load rake tasks and generators
				regenerateRakeTasksAndGeneratorsSettings(railsFacet, sdk);

				// Update uncommitedModule content and rails uncommitedModule settings
				// will be invoked later
				final consulo.util.lang.function.ThrowableRunnable<Exception> updateModuleAndSettings = new consulo.util.lang.function.ThrowableRunnable<Exception>()
				{
					@Override
					public void run() throws Exception
					{
						//now we can unmark content
						descFactory.unpinAll();

						//after installing plugins(i.e. generators and spec may be changed)
						regenerateRakeTasksAndGeneratorsSettings(railsFacet, sdk);

						// Synchronizes externally added files after installation
						// (otherwize they will not be processed by IDEA)
						RailsFacetUtil.refreshRailsAppHomeContent(uncommitedModule);

						//////////////// Additional Sources, Rails View Custom folders
						configureRailsSourcePathes(RailsViewFoldersManager.getInstance(uncommitedModule), railsFacet.getConfiguration().getRailsApplicationRootPathUrl(), RSpecModuleSettings.getInstance(uncommitedModule).shouldUseRSpecTestFramework(), uncommitedModule);

					}
				};

				final RailsWizardSettingsHolder.RSpecConfiguration rSpecConf = settings.getRSpecConf();
				if(rSpecConf.enableRSpecSupport() || rSpecConf.enableRSpecRailsSupport())
				{
					final String[] args = {
							GeneratorsUtil.GENERATE_SCRIPT,
							RSpecUtil.SPEC_GENERATOR_NAME
					};
					final String processTitle = RBundle.message("new.generate.spec.generating.title");
					final String errorTitle = RBundle.message("new.generate.common.error.title");

					final consulo.util.lang.function.ThrowableRunnable<Exception> generateRSpecStubIfNecessary = new consulo.util.lang.function.ThrowableRunnable<Exception>()
					{
						@Override
						public void run() throws Exception
						{
							// Generates RSpec Stub
							RailsFacetUtil.refreshRailsAppHomeContent(uncommitedModule);

							if(!RSpecUtil.isSpecScriptSupportInstalledInRailsProject(VirtualFileUtil.constructLocalUrl(applicationHomePath)))
							{
								GeneratorsUtil.invokeGenerator(uncommitedModule, processTitle, errorTitle, args, descFactory, updateModuleAndSettings, sdk);
							}
							else
							{
								IdeaInternalUtil.runInsideWriteAction(updateModuleAndSettings);
							}
						}
					};
					// Install RSpec
					installRSpecSupport(uncommitedModule, descFactory, generateRSpecStubIfNecessary, settings.getRSpecConf(), sdk);
				}
				else
				{
					try
					{
						updateModuleAndSettings.run();
					}
					catch(Exception e)
					{
						showError(e, project, moduleName);
					}
				}
			}
		};
	}

	private static void installComponents(@Nonnull final Module uncommitedModule, @Nonnull final BaseRailsFacet railsFacet, @Nonnull final Sdk sdk, @Nonnull final RailsWizardSettingsHolder settings, @Nonnull final String applicationHomePath)
	{

		final String moduleName = uncommitedModule.getName();
		final Project project = uncommitedModule.getProject();

		final Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				//marks that all tabs are pined, i.e. set-up processes in different tabs
				final RunContentDescriptorFactory.PinTabsFactory descFactory = new RunContentDescriptorFactory.PinTabsFactory();

				// Generating rails application in modal
				generateRailsApplication(uncommitedModule, sdk, descFactory, settings, applicationHomePath, createOnRailsAppGenerated(descFactory, uncommitedModule, sdk, railsFacet, settings, applicationHomePath, project, moduleName));
			}
		};
		StartupManager.getInstance(project).runWhenProjectIsInitialized(runnable);
	}

	public static void regenerateRakeTasksAndGeneratorsSettings(final BaseRailsFacet railsFacet, @Nullable final Sdk sdk)
	{
		loadRakeTasksAndGeneratorsSettings(railsFacet, sdk, true);
	}

	private static void loadRakeTasksAndGeneratorsSettings(final BaseRailsFacet railsFacet, @Nullable final Sdk sdk)
	{
		loadRakeTasksAndGeneratorsSettings(railsFacet, sdk, false);
	}

	private static void loadRakeTasksAndGeneratorsSettings(final BaseRailsFacet railsFacet, @Nullable final Sdk sdk, final boolean forceRegenerate)
	{
		// Reloads list of tasks and generators
		final BaseRailsFacetConfigurationLowLevel configuration = (BaseRailsFacetConfigurationLowLevel) railsFacet.getConfiguration();
		//regenerates generators list after installation
		configuration.loadGenerators(forceRegenerate, sdk);
		//regenerates rake tasks list after installation
		configuration.loadRakeTasks(forceRegenerate, sdk);
	}

	private static void installRSpecSupport(final Module module, final RunContentDescriptorFactory descFactory, @Nullable final consulo.util.lang.function.ThrowableRunnable<Exception> nextAction, final RailsWizardSettingsHolder.RSpecConfiguration rSpecConf, @Nonnull final Sdk sdk)
	{

		final consulo.util.lang.function.ThrowableRunnable<Exception> installRSpecRails = new ThrowableRunnable<Exception>()
		{
			@Override
			public void run()
			{
				if(rSpecConf.enableRSpecRailsSupport() && rSpecConf.shouldInstallRSpecRailsPlugin())
				{
					PluginsUtil.installPluginAndUpdateModuleContent(sdk, module, rSpecConf.getRSpecRailsArgs(), descFactory, nextAction);
				}
			}
		};

		if(rSpecConf.enableRSpecSupport() && rSpecConf.shouldInstallRSpecPlugin())
		{
			PluginsUtil.installPluginAndUpdateModuleContent(sdk, module, rSpecConf.getRSpecArgs(), descFactory, installRSpecRails);
		}
		else
		{
			// install plugin if is needed
			try
			{
				installRSpecRails.run();
			}
			catch(Exception e)
			{
				showError(e, module.getProject(), module.getName());
			}
		}
	}


	private static void showError(final Exception e, final Project project, final String moduleName)
	{
		final String title = RBundle.message("module.create.dialog.init.errors.title", moduleName);
		RubyScriptRunner.showErrorMessage(project, title, e);
		LOG.warn(e);
	}

	private static void configureRakeTasksAndGenerators(final Facet facet, @Nullable final Sdk sdk)
	{
		final Module module = facet.getModule();
		/**
		 * If project is initialized
		 *   1. We create new java module with rails and jruby facets
		 *    .    SDK + (jruby facet)
		 *   2. We create new ruby module with facets
		 *    .    SDK - (uncommited module)  => on module added
		 *
		 *   If module exist:
		 *   3. We add new rails/jrails facet in existing module
		 *    .    SDK + (jruby facet)
		 *    .    SDK + (sdk from ruby module after ROOTs chaned)
		 *
		 * otherwise we create/load new project and
		 *
		 *   1. We create rails facet for ruby or module+jruby
		 *  +.     SDK + (jruby facet)
		 *  +.     SDK - (uncommited module)  => on module added
		 *
		 *   2. We load rails facet for ruby or module+jruby
		 *   .     SDK + (from saved jruby facet)
		 *   .     SDK + (from save ruby module)
		 */
		loadRakeTasksAndGeneratorsIfArentLoadedRunnalbe(sdk, facet).run();
	}

	private static Runnable loadRakeTasksAndGeneratorsIfArentLoadedRunnalbe(@Nullable final Sdk sdk, final Facet facet)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				loadRakeTasksAndGeneratorsSettings((BaseRailsFacet) facet, sdk);
			}
		};
	}

	@Nullable
	private static String searchRailsAppInUnderDirectory(@Nonnull final Module module, @Nonnull final ProgressIndicator indicator)
	{
		final Ref<String> railsRootPathRef = new Ref<String>();
		final ModuleFileIndex moduleFileIndex = ModuleRootManager.getInstance(module).getFileIndex();
		moduleFileIndex.iterateContent(new ContentIterator()
		{
			@Override
			public boolean processFile(final VirtualFile fileOrDir)
			{
				if(indicator.isCanceled())
				{
					//has been canceled
					return false;
				}

				if(fileOrDir.isDirectory())
				{
					final String path = fileOrDir.getPath();
					if(RailsUtil.containsRailsApp(path))
					{
						railsRootPathRef.set(path);
						//exit
						return false;
					}
				}
				//continue
				return true;
			}
		});
		return railsRootPathRef.get();
	}

	private static void configureRailsSourcePathes(final RailsViewFoldersManager rootManager, @Nonnull final String railsAppHomeRootUrl, final boolean isRSpecSupportEnabled, final Module module)
	{
		// add rails view custom folders
		addAdditionalRailsViewFolders(railsAppHomeRootUrl, rootManager, isRSpecSupportEnabled);

		// excluded folders use Root model and it should be commited
		final Runnable addExcludedFolders = new Runnable()
		{
			@Override
			public void run()
			{
				IdeaInternalUtil.runInEDThreadInWriteAction(new consulo.util.lang.function.ThrowableRunnable<Exception>()
				{
					@Override
					public void run()
					{
						@Nonnull final ModifiableRootModel modifiableModel = ModuleRootManager.getInstance(module).getModifiableModel();

						final ContentEntry[] entries = modifiableModel.getContentEntries();
						if(entries.length == 0)
						{
							return;
						}

						ContentEntry railsContentEntry = null;
						for(ContentEntry entry : entries)
						{
							if(railsAppHomeRootUrl.contains(entry.getUrl()))
							{
								railsContentEntry = entry;
								break;
							}
						}

						assert railsContentEntry != null; //because railsAppHome is under Content Root

						// add rails excluded folder(tmp, components)
						addDefaultRailsExcludedFolders(railsAppHomeRootUrl, railsContentEntry);

						modifiableModel.commit();
					}
				}, Application.get().getDefaultModalityState());
			}
		};


		addExcludedFolders.run();
	}

	private static void addDefaultRailsExcludedFolders(final String railsAppHomeRootURL, final ContentEntry contentEntry)
	{
	   /* final String componentsUrl = StandardRailsPaths.buildComponentsPath(railsAppHomeRootURL);
        final VirtualFileManager manager = VirtualFileManager.getInstance();

        final String tmpUrl = StandardRailsPaths.buildTmpPath(railsAppHomeRootURL);
        final VirtualFile tmp = manager.findFileByUrl(tmpUrl);
        if (tmp != null) {
            contentEntry.addExcludeFolder(tmp);
        }

        final VirtualFile components = manager.findFileByUrl(componentsUrl);
        if (components != null) {
            contentEntry.addExcludeFolder(components);
        }  */
	}

	private static void addAdditionalRailsViewFolders(@Nonnull final String railsAppHomeRootUrl, @Nonnull final RailsViewFoldersManager rootManager, final boolean isRSpecSupportEnabled)
	{

		final LinkedList<String> urlsList = new LinkedList<String>();
		if(isRSpecSupportEnabled)
		{
			urlsList.add(RSpecUtil.getRailsSpecFolderPathOrUrl(railsAppHomeRootUrl)); // spec
		}
		urlsList.add(StandardRailsPaths.buildDBPath(railsAppHomeRootUrl)); //db
		urlsList.add(StandardRailsPaths.buildLibPath(railsAppHomeRootUrl));  //lib
		urlsList.add(StandardRailsPaths.buildEdgeRailsPath(railsAppHomeRootUrl)); //vendor/rails
		urlsList.add(StandardRailsPaths.buildPluginsPath(railsAppHomeRootUrl));  // vendor/plugins
		urlsList.add(StandardRailsPaths.buildJavaScriptsPath(railsAppHomeRootUrl)); // public/javascripts
		urlsList.add(StandardRailsPaths.buildStyleSheetsPath(railsAppHomeRootUrl)); // public/stylesheets

		rootManager.setRailsViewUserFolderUrls(urlsList);
	}
}