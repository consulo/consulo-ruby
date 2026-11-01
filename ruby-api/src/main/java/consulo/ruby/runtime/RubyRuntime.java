package consulo.ruby.runtime;

import java.util.List;

/**
 * What runs the Ruby code of a module: a Ruby SDK, or a runtime another plugin brings (JRuby from the build's jars).
 */
public interface RubyRuntime {
    /**
     * @return the program that runs Ruby: ruby, or java
     */
    String getExecutablePath();

    /**
     * @return the parameters before the Ruby options and the script: the classpath and main class of JRuby
     */
    default List<String> getExecutableParameters() {
        return List.of();
    }

    /**
     * @return the urls of the directories required files are looked up in, beside the module's own
     */
    default List<String> getLoadPathUrls() {
        return List.of();
    }

    /**
     * @return the url of the stub of the core classes (builtin.rb), indexed for completion and resolve
     */
    String getBuiltInStubsUrl();

    /**
     * @return true if the debug gem can debug the code - it needs CRuby
     */
    default boolean isDebuggable() {
        return true;
    }
}
