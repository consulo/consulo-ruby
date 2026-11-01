package consulo.ruby.parser;

import consulo.application.ReadAction;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.language.SimpleParsingTest;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.RHTMLFileType;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Parses every Ruby file under {@code -Druby.corpus=<dir>} and reports the files with parse errors to
 * {@code target/ruby-corpus-report.txt}. Skipped without the property.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyCorpusParsingTest extends SimpleParsingTest<Object> {
    public RubyCorpusParsingTest() {
        super("parsing/modern", "rb");
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return RubyFileType.INSTANCE;
    }

    @Test
    public void testCorpus(Context context) throws Exception {
        String corpus = System.getProperty("ruby.corpus");
        Assumptions.assumeTrue(corpus != null && !corpus.isEmpty(), "no -Druby.corpus");

        List<Path> files;
        try (Stream<Path> walk = Files.walk(Path.of(corpus))) {
            files = walk.filter(path -> (path.toString().endsWith(".rb") || path.toString().endsWith(".erb")) && Files.isRegularFile(path)).sorted().toList();
        }

        Map<String, Integer> messages = new TreeMap<>();
        List<String> failed = new ArrayList<>();
        for (Path path : files) {
            String text = Files.readString(path);
            List<String> errors = ReadAction.compute(() -> {
                String name = path.getFileName().toString();
                PsiFile file = name.endsWith(".erb")
                    ? context.psiFileFactory().createFileFromText(name, RHTMLFileType.INSTANCE, text)
                    : context.psiFileFactory().createFileFromText(name, RubyLanguage.INSTANCE, text);
                // a template has a tree per language: the eRuby one and the HTML one
                Collection<PsiErrorElement> found = new ArrayList<>();
                for (PsiFile root : file.getViewProvider().getAllFiles()) {
                    ensureParsed(root);
                    found.addAll(PsiTreeUtil.findChildrenOfType(root, PsiErrorElement.class));
                }
                List<String> result = new ArrayList<>();
                for (PsiErrorElement error : found) {
                    int offset = error.getTextRange().getStartOffset();
                    int line = 1;
                    for (int i = 0; i < offset && i < text.length(); i++) {
                        if (text.charAt(i) == '\n') {
                            line++;
                        }
                    }
                    result.add(line + ": " + error.getErrorDescription());
                }
                return result;
            });
            if (!errors.isEmpty()) {
                failed.add(path + " " + errors.get(0) + (errors.size() > 1 ? " (+" + (errors.size() - 1) + ")" : ""));
                String first = errors.get(0);
                messages.merge(first.substring(first.indexOf(": ") + 2), 1, Integer::sum);
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("files: ").append(files.size()).append(", with errors: ").append(failed.size()).append("\n\n");
        messages.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(entry -> report.append(entry.getValue()).append("  ").append(entry.getKey()).append("\n"));
        report.append("\n");
        failed.forEach(line -> report.append(line).append("\n"));
        Files.writeString(Path.of("target", "ruby-corpus-report.txt"), report);
        System.out.println("RUBY CORPUS: files " + files.size() + ", with errors " + failed.size());
    }
}
