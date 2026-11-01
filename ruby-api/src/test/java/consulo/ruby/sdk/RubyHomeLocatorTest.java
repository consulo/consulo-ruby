package consulo.ruby.sdk;

import consulo.platform.Platform;
import org.jetbrains.plugins.ruby.ruby.sdk.RubyHomeLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyHomeLocatorTest {
    @Test
    public void testManagerNames() {
        Assertions.assertEquals("rbenv", RubyHomeLocator.getManagerName(Path.of("/home/u/.rbenv/versions/3.3.0")));
        Assertions.assertEquals("rvm", RubyHomeLocator.getManagerName(Path.of("/home/u/.rvm/rubies/ruby-3.2.2")));
        Assertions.assertEquals("asdf", RubyHomeLocator.getManagerName(Path.of("/home/u/.asdf/installs/ruby/3.4.1")));
        Assertions.assertEquals("mise", RubyHomeLocator.getManagerName(Path.of("/home/u/.local/share/mise/installs/ruby/3.4")));
        Assertions.assertEquals("chruby", RubyHomeLocator.getManagerName(Path.of("/opt/rubies/ruby-3.3.0")));
        Assertions.assertEquals("Homebrew", RubyHomeLocator.getManagerName(Path.of("/opt/homebrew/opt/ruby")));
        Assertions.assertNull(RubyHomeLocator.getManagerName(Path.of("/usr")));
    }

    @Test
    public void testCollectFindsInstalledRuby() {
        List<Path> homes = new ArrayList<>();
        RubyHomeLocator.collect(Platform.current(), "ruby", homes::add);
        System.out.println("RUBY HOMES: " + homes);
        // every home has the interpreter, and the same installation is not reported twice
        for (Path home : homes) {
            Assertions.assertTrue(Files.isRegularFile(home.resolve("bin").resolve("ruby")), home.toString());
        }
        Assertions.assertEquals(homes.size(), homes.stream().distinct().count());
    }
}
