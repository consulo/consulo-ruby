package consulo.ruby.parser;

import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.language.SimpleParsingTest;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Modern Ruby syntax must parse without errors.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyModernSyntaxParsingTest extends SimpleParsingTest<Object> {
    public RubyModernSyntaxParsingTest() {
        super("parsing/modern", "rb");
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return RubyFileType.INSTANCE;
    }

    @Override
    protected void checkResult(Context context, @Nullable Object testContext, PsiFile file) throws Exception {
        Assertions.assertFalse(file.getText().isEmpty(), "no source for " + file.getName());

        Collection<PsiErrorElement> errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class);
        if (errors.isEmpty()) {
            return;
        }

        String text = file.getText();
        List<String> messages = new ArrayList<>();
        for (PsiErrorElement error : errors) {
            messages.add(position(text, error) + " " + error.getErrorDescription());
        }
        Assertions.fail(String.join("\n", messages) + "\n\n" + toParseTreeText(file, true, false));
    }

    private static String position(String text, PsiElement element) {
        int offset = element.getTextRange().getStartOffset();
        int line = 1;
        int column = 1;
        for (int i = 0; i < offset && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                column = 1;
            }
            else {
                column++;
            }
        }
        return line + ":" + column;
    }

    @Test
    public void testArgForwarding(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testArraySplat(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testBlockParams(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testBlockRescue(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCorpusGemspec(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCorpusGreeter(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCorpusGreeterTest(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCorpusPostsController(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCorpusRoutes(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCorpusShapes(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testEndlessDef(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testHashLabels(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testHashShorthand(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testKeywordParams(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testLeadingDot(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testMagicNames(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testNumberedParams(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testNumericLiterals(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testOperatorSymbols(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPatternMatching(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testPercentSymbols(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRanges(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRightwardAndMisc(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSafeNavigation(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSplatCalls(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testSquigglyHeredoc(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testStabbyLambda(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testTrailingComma(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testMlhsSplatFirst(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testYieldParenArithmetic(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testKeywordMethodNames(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testDestructuringParams(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testRescueSplat(Context context) throws Exception {
        doTest(context, null);
    }

    @Test
    public void testCommandCallAssignment(Context context) throws Exception {
        doTest(context, null);
    }
}
