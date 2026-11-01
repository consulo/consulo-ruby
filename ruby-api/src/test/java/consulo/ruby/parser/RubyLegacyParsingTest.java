package consulo.ruby.parser;

import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.test.junit.impl.language.SimpleParsingTest;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The parser data of the original plugin: the source, a line of dashes, the expected PSI tree.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyLegacyParsingTest extends SimpleParsingTest<Object> {
    private static final String DATA_PATH = "parsing/legacy";
    private static final Pattern SEPARATOR = Pattern.compile("-{3,}\\R(?=Ruby file)");

    public RubyLegacyParsingTest() {
        super(DATA_PATH, "txt");
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return RubyFileType.INSTANCE;
    }

    @TestFactory
    public Stream<DynamicTest> testLegacyData(Context context) throws Exception {
        URL root = getClass().getResource("/" + DATA_PATH);
        Assertions.assertNotNull(root, "no " + DATA_PATH);
        Path rootPath = Path.of(root.toURI());
        List<Path> files;
        try (Stream<Path> walk = Files.walk(rootPath)) {
            files = walk.filter(path -> path.toString().endsWith(".txt")).sorted().toList();
        }
        return files.stream().map(path -> DynamicTest.dynamicTest(rootPath.relativize(path).toString(), () -> check(context, rootPath, path)));
    }

    private void check(Context context, Path rootPath, Path path) throws Exception {
        String data = Files.readString(path, StandardCharsets.UTF_8).replace("\r\n", "\n");
        Matcher separator = SEPARATOR.matcher(data);
        Assertions.assertTrue(separator.find(), "no separator in " + path);

        String source = data.substring(0, separator.start());
        String expected = data.substring(separator.end());

        PsiFile file = createFile(context, null, "test.rb", getFileType(context, null), source);
        Assertions.assertEquals(source, file.getText());
        String actual = toParseTreeText(file, false, false);
        if (!expected.trim().equals(actual.trim())) {
            // the tree of today next to the expected one, for review: target/legacy-parsing-actual
            Path actualFile = Path.of("target", "legacy-parsing-actual").resolve(rootPath.relativize(path).toString());
            Files.createDirectories(actualFile.getParent());
            Files.writeString(actualFile, source + "-----------\n" + actual, StandardCharsets.UTF_8);
        }
        Assertions.assertEquals(expected.trim(), actual.trim(), path.toString());
    }
}
