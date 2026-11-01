package consulo.ruby.parser;

import consulo.language.ast.ASTNode;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiErrorElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.test.junit.impl.language.SimpleParsingTest;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * A do block after command arguments without parentheses belongs to the command, not to its last argument.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyBlockBindingParsingTest extends SimpleParsingTest<Object> {
    public RubyBlockBindingParsingTest() {
        super("parsing/blocks", "rb");
    }

    @Override
    protected LanguageFileType getFileType(Context context, @Nullable Object testContext) {
        return RubyFileType.INSTANCE;
    }

    @Override
    protected void checkResult(Context context, @Nullable Object testContext, PsiFile file) throws Exception {
        Assertions.assertFalse(file.getText().isEmpty(), "no source for " + file.getName());
        Assertions.assertTrue(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement.class).isEmpty(), () -> toParseTreeText(file, true, false));

        List<String> misplaced = new ArrayList<>();
        collectMisplacedDoBlocks(file.getNode(), misplaced);
        Assertions.assertTrue(misplaced.isEmpty(), () -> String.join("\n", misplaced) + "\n\n" + toParseTreeText(file, true, false));
    }

    private static void collectMisplacedDoBlocks(ASTNode node, List<String> misplaced) {
        if (node.getElementType() == RubyElementTypes.DO_CODE_BLOCK) {
            ASTNode blockCall = node.getTreeParent();
            ASTNode list = blockCall == null ? null : blockCall.getTreeParent();
            ASTNode owner = list == null ? null : list.getTreeParent();
            if (list != null && list.getElementType() == RubyElementTypes.LIST_OF_EXPRESSIONS && owner != null && owner.getElementType() == RubyElementTypes.COMMAND_CALL) {
                misplaced.add("do block bound to the argument: " + blockCall.getText().lines().findFirst().orElse(""));
            }
        }
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            collectMisplacedDoBlocks(child, misplaced);
        }
    }

    @Test
    public void testDoBlockBindsToCommand(Context context) throws Exception {
        doTest(context, null);
    }
}
