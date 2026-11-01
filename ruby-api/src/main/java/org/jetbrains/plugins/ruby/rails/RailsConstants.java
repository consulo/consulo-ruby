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

package org.jetbrains.plugins.ruby.rails;

import java.io.File;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 30.09.2006
 */
public class RailsConstants
{
	@NonNls
	public static final String RAILS_GEM_EXECUTABLE = "rails";
	@NonNls
	public static final String RSPEC_GEM_EXECUTABLE = "spec";
	@NonNls
	public static final String GEM_EXECUTABLE = "gem";

	@NonNls
	public static final String INSTALL_PARAMETER = "install";
	@NonNls
	public static final String RSPEC_GEM_NAME = "rspec";

	// Rails application names suffixes
	@NonNls
	public static final String APPLICATION_NAME = "application";
	@NonNls
	public static final String APPLICATION_HELPER_FILE_NAME = "application_helper";

	@NonNls
	public static final String APPLICATION_CONTROLLER_NAME = "ApplicationController";
	@NonNls
	public static final String CONTROLLERS_FILE_NAME_SUFFIX = "_controller";
	@NonNls
	public static final String CONTROLLERS_CLASS_NAME_SUFFIX = "Controller";
	@NonNls
	public static final String HELPERS_FILE_NAME_SUFFIX = "_helper";
	@NonNls
	public static final String HELPERS_MODULE_NAME_SUFFIX = "Helper";

	// Rails application pathes and files
	@NonNls
	private static final String APPLICATION_FOLDER = "app";
	@NonNls
	private static final String MODEL_FOLDER = "models";
	@NonNls
	private static final String CONTROLLERS_FOLDER = "controllers";
	@NonNls
	private static final String HELPERS_FOLDER = "helpers";
	@NonNls
	private static final String VIEWS_FOLDER = "views";
	@NonNls
	private static final String SHARED_PARTIALS_FOLDER = "shared";
	@NonNls
	private static final String LAYOUTS_FOLDER = "layouts";
	@NonNls
	private static final String APIS_FOLDER = "apis";
	@NonNls
	private static final String SERVICES_FOLDER = "services";
	@NonNls
	public static final String PUBLIC_FOLDER = "public";
	@NonNls
	public static final String LIB_FOLDER = "lib";

	@NonNls
	public static final String MODEL_PATH = APPLICATION_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + MODEL_FOLDER;
	@NonNls
	public static final String CONTROLLERS_PATH = APPLICATION_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + CONTROLLERS_FOLDER;
	@NonNls
	public static final String HELPERS_PATH = APPLICATION_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + HELPERS_FOLDER;
	@NonNls
	public static final String VIEWS_PATH = APPLICATION_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + VIEWS_FOLDER;
	@NonNls
	public static final String SHARED_PARTIALS_PATH = VIEWS_PATH + VirtualFileUtil.VFS_PATH_SEPARATOR + SHARED_PARTIALS_FOLDER;
	@NonNls
	public static final String LAYOUTS_PATH = VIEWS_PATH + VirtualFileUtil.VFS_PATH_SEPARATOR + LAYOUTS_FOLDER;
	@NonNls
	public static final String SERVICES_PATH = APPLICATION_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + SERVICES_FOLDER;
	@NonNls
	public static final String APIS_PATH = APPLICATION_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + APIS_FOLDER;
	@NonNls
	public static final String COMPONENTS_PATH = "components";
	@NonNls
	public static final String TESTS_PATH = "test";
	@NonNls
	public static final String CONFIG_PATH = "config";
	@NonNls
	public static final String SCRIPTS_PATH = "script";
	@NonNls
	public static final String PLUGINS_AND_VENDORS_PACKAGES_LIB_PATH = "lib";
	@NonNls
	public static final String TMP_PATH = "tmp";
	@NonNls
	public static final String PLUGINS_INIT_FILE_PATH = "init.rb";
	@NonNls
	public static final String LIB_PATH = LIB_FOLDER;
	@NonNls
	public static final String VENDOR_PATH = "vendor";
	@NonNls
	public static final String PUBLIC_JAVASCRIPTS_PATH = PUBLIC_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + "javascripts";
	@NonNls
	public static final String PUBLIC_STYLESHEETS_PATH = PUBLIC_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + "stylesheets";
	@NonNls
	public static final String PUBLIC_IMAGES_PATH = PUBLIC_FOLDER + VirtualFileUtil.VFS_PATH_SEPARATOR + "images";
	@NonNls
	public static final String PLUGINS_PATH = VENDOR_PATH + VirtualFileUtil.VFS_PATH_SEPARATOR + "plugins";
	@NonNls
	public static final String EDGE_RAILS_PATH = VENDOR_PATH + VirtualFileUtil.VFS_PATH_SEPARATOR + "rails";
	@NonNls
	public static final String APP_PATH = APPLICATION_FOLDER;
	@NonNls
	public static final String TEST_MOCKS_ENVIROMENT_PATH = TESTS_PATH + "/mocks/enviroment";
	@NonNls
	public static final String DB_PATH = "db";
	@NonNls
	public static final String MIGRATIONS_PATH = DB_PATH + VirtualFileUtil.VFS_PATH_SEPARATOR + "migrate";
	@NonNls
	public static final String DB_SCHEMA_FILE = "schema.rb";

	// If IDEA can find all these files, it suggests, that rails application is already generated in path
	@NonNls
	public static final String RAILS_APP_CORE_FILES_BOOT_FILE = "boot.rb";
	@NonNls
	public static final String[] RAILS_APP_CORE_FILES = new String[]{
			"app",
			"db",
			"config" + File.separatorChar + RAILS_APP_CORE_FILES_BOOT_FILE
	};

	// Scripts params
	@NonNls
	public static final String SPECS_SCRIPT_PATH = SCRIPTS_PATH + VirtualFileUtil.VFS_PATH_SEPARATOR + "spec";
	/**
	 * The rails command of an application, which loads its bundle itself
	 */
	@NonNls
	public static final String RAILS_SCRIPT = "bin/rails";
	@NonNls
	public static final String SERVER_COMMAND = "server";

	@NonNls
	public static final String PARAM_FORCE_OVERWRITE = "--force";
	@NonNls
	public static final String PARAM_SKIP = "--skip";
	@NonNls
	public static final String PARAM_SERVER_PORT = "-p";
	@NonNls
	public static final String PARAM_SERVER_IP = "-b";
	@NonNls
	public static final String PARAM_SERVER_ENVIRONMENT = "-e";
	@NonNls
	public static final String PARAM_SERVER_USING = "-u";
	@NonNls
	public static final String PARAM_VERSION = "--version";
	@NonNls
	public static final String PARAM_DATABASE = "--database";

	//SDK pathes and files
	@NonNls
	public static final String SDK_ACTION_VIEW = "action_view"; //see LoadPath/action_view.rb
	@NonNls
	public static final String SDK_ACTION_VIEW_HELPERS_DIR = "action_view/helpers";
	@NonNls
	public static final String SDK_ACTIVE_RECORD = "active_record";
	@NonNls
	public static final String SDK_ACTIVE_RECORD_ADAPTERS_DIR_NAME = "connection_adapters";
	@NonNls
	public static final String SDK_ACTIVE_RECORD_MODULE = "ActiveRecord";
	@NonNls
	public static final String SDK_ACTION_MAILER_MODULE = "ActionMailer";
	@NonNls
	public static final String SDK_ACTION_WEB_SERVICE = "ActionWebService";
	@NonNls
	public static final String SDK_ACTION_WEB_SERVICE_API_MODULE_NAME = "API";
	@NonNls
	public static final String SDK_ACTION_CONTROLLER = "action_controller";
	@NonNls
	public static final String SDK_ACTIVE_SUPPORT = "active_support";
	@NonNls
	public static final String SDK_ACTIVE_SUPPORT_DIR = "active_support";
	@NonNls
	public static final String SDK_ACTIVE_SUPPORT_CORE_EXT = "core_ext";
	@NonNls
	public static final String BASE_CLASS = "Base";

	// For Unit tests:
	// Test::Unit::TestCase
	@NonNls
	public static final String TEST_CASE_CLASS_NAME = "TestCase";
	@NonNls
	public static final String UNIT_MODULE_NAME = "Unit";
	@NonNls
	public static final String TEST_MODULE_NAME = "Test";
	@NonNls
	public static final String TEST_UNIT_TESTCASE = "Test::Unit::TestCase";

	// Action Mailer built-in gems
	@NonNls
	public static final String ACTION_MAILER_BUILT_IN_GEMS_TMAIL_NAME = "tmail";
	@NonNls
	public static final String ACTION_MAILER_BUILT_IN_GEMS_VENDOR_NAME = "vendor";
}
