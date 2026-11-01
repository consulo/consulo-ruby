package consulo.ruby.impl.bundler;

import consulo.externalSystem.model.ProjectSystemId;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.api.localize.RubyApiLocalize;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public interface BundlerConstants {
    ProjectSystemId SYSTEM_ID = new ProjectSystemId("BUNDLER", RubyApiLocalize.bundlerSystemName(), RubyApiIconGroup.rubyRubygems());

    String GEMFILE = "Gemfile";
    String GEMFILE_LOCK = "Gemfile.lock";
    String GEMSPEC_EXTENSION = ".gemspec";
    String RUBY_VERSION_FILE = ".ruby-version";

    String RAILS_GEM = "railties";

    String RUBY_EXTENSION_ID = "ruby";
    String RAILS_EXTENSION_ID = "ruby-on-rails";

    String TASK_INSTALL = "bundle install";
    String TASK_UPDATE = "bundle update";
    String TASK_GROUP_BUNDLER = "bundler";
    String TASK_GROUP_RAKE = "rake";
}
