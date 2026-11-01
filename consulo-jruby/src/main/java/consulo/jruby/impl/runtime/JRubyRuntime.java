package consulo.jruby.impl.runtime;

import consulo.ruby.runtime.BundledRubyStubs;
import consulo.ruby.runtime.RubyRuntime;

import java.util.List;

/**
 * JRuby from the dependencies of a Java module, started with the module's JDK.
 */
public class JRubyRuntime implements RubyRuntime {
    public static final String MAIN_CLASS = "org.jruby.Main";

    private final String myJavaExecutable;
    private final String myClasspath;
    private final List<String> myLoadPathUrls;

    public JRubyRuntime(String javaExecutable, String classpath, List<String> loadPathUrls) {
        myJavaExecutable = javaExecutable;
        myClasspath = classpath;
        myLoadPathUrls = loadPathUrls;
    }

    @Override
    public String getExecutablePath() {
        return myJavaExecutable;
    }

    @Override
    public List<String> getExecutableParameters() {
        return List.of("-cp", myClasspath, MAIN_CLASS);
    }

    @Override
    public List<String> getLoadPathUrls() {
        return myLoadPathUrls;
    }

    @Override
    public String getBuiltInStubsUrl() {
        return BundledRubyStubs.getBuiltInUrl();
    }

    @Override
    public boolean isDebuggable() {
        return false;
    }
}
