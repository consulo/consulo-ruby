package consulo.ruby.impl.generation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.Document;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.documentation.RubyHelpUtil;
import org.jetbrains.plugins.ruby.ruby.lang.namesValidator.RubyNamesValidator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RBodyStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RCompoundStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RSingletonMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates method stubs with the signature of overridden or implemented methods and inserts them into a class.
 */
public final class RubyMethodGenerator {
    public static final String BODY_COMMENT = "# code here";

    private RubyMethodGenerator() {
    }

    /**
     * @return the declaration line of the stub, e.g. {@code self.name(a, *rest, &block)}, null for unknown elements
     */
    @Nullable
    @RequiredReadAction
    public static String getSignature(@Nonnull PsiElement method) {
        if (method instanceof RMethod rMethod) {
            List<String> parameters = new ArrayList<>();
            for (ArgumentInfo argument : rMethod.getArgumentInfos()) {
                parameters.add(switch (argument.getType()) {
                    case PREDEFINED -> argument.getName() + " = nil";
                    case ARRAY -> "*" + argument.getName();
                    case BLOCK -> "&" + argument.getName();
                    default -> argument.getName();
                });
            }
            return signature(rMethod instanceof RSingletonMethod, rMethod.getName(), parameters);
        }

        RubyInteropProvider provider = RubyInterop.forElement(method);
        if (provider != null && method instanceof PsiNamedElement namedMethod && namedMethod.getName() != null) {
            RubyNamesValidator validator = new RubyNamesValidator();
            List<String> parameters = new ArrayList<>();
            for (String name : provider.getParameterNames(method)) {
                parameters.add(validator.isKeyword(name, method.getProject()) ? name + "1" : name);
            }
            return signature(provider.isStaticMember(method), namedMethod.getName(), parameters);
        }
        return null;
    }

    private static String signature(boolean singleton, String name, List<String> parameters) {
        String parameterList = parameters.isEmpty() ? "" : "(" + String.join(", ", parameters) + ")";
        return (singleton ? "self." : "") + name + parameterList;
    }

    /**
     * @return the stub source of the method, null for unknown elements
     */
    @Nullable
    @RequiredReadAction
    public static String generate(@Nonnull PsiElement method) {
        String signature = getSignature(method);
        return signature != null ? "def " + signature + "\n  " + BODY_COMMENT + "\nend" : null;
    }

    /**
     * Inserts the methods into the class or module around the anchor: after the member holding it, else at the end
     *
     * @return the offset of the comment inside the first inserted method, where the caret goes, -1 if nothing was inserted
     */
    @RequiredWriteAction
    public static int insert(@Nonnull Project project, @Nonnull PsiElement anchor, @Nonnull List<String> methods) {
        RContainer container = PsiTreeUtil.getParentOfType(anchor, RClass.class, RModule.class);
        RBodyStatement body = container != null ? PsiTreeUtil.getChildOfType(container, RBodyStatement.class) : null;
        RCompoundStatement block = body != null ? body.getBlock() : null;
        PsiFile file = anchor.getContainingFile();
        PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
        Document document = file != null ? documentManager.getDocument(file) : null;
        if (block == null || document == null || methods.isEmpty()) {
            return -1;
        }

        List<RPsiElement> statements = block.getStatements();
        RPsiElement nextStatement = null;
        for (int i = 0; i < statements.size(); i++) {
            RPsiElement statement = statements.get(i);
            if (PsiTreeUtil.isAncestor(statement, anchor, false)) {
                nextStatement = i + 1 < statements.size() ? statements.get(i + 1) : null;
                break;
            }
            if (statement.getTextRange().getStartOffset() > anchor.getTextRange().getStartOffset()) {
                nextStatement = statement;
                break;
            }
        }

        String code = String.join("\n\n", methods);
        int offset;
        String text;
        if (nextStatement != null) {
            List<PsiComment> comments = RubyHelpUtil.getPsiComments(nextStatement);
            offset = (comments.isEmpty() ? nextStatement : comments.get(0)).getTextRange().getStartOffset();
            text = code + "\n\n";
        }
        else if (!statements.isEmpty()) {
            offset = statements.get(statements.size() - 1).getTextRange().getEndOffset();
            text = "\n\n" + code;
        }
        else {
            offset = block.getTextRange().getStartOffset();
            text = code + "\n";
        }

        documentManager.doPostponedOperationsAndUnblockDocument(document);
        document.insertString(offset, text);
        documentManager.commitDocument(document);
        CodeStyleManager.getInstance(project).reformatText(file, offset, offset + text.length());
        documentManager.doPostponedOperationsAndUnblockDocument(document);
        return document.getText().indexOf(BODY_COMMENT, offset);
    }
}
