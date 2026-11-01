package consulo.ruby.impl.generation;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.codeEditor.Editor;
import consulo.language.Language;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.action.LanguageCodeInsightActionHandler;
import consulo.language.editor.generation.ClassMember;
import consulo.language.editor.generation.MemberChooserBuilder;
import consulo.language.editor.generation.MemberChooserObject;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Chooses methods of the superclasses and included modules of the class at the caret and generates stubs for them.
 */
public abstract class RubyGenerateMethodsHandlerBase implements LanguageCodeInsightActionHandler {
    @Nonnull
    protected abstract LocalizeValue getTitle();

    /**
     * @return the method symbols to offer, in the order they are shown
     */
    @Nonnull
    @RequiredReadAction
    protected abstract List<Symbol> collectMethods(@Nullable FileSymbol fileSymbol, @Nonnull Symbol classSymbol);

    @Nonnull
    @RequiredReadAction
    private List<RubyMethodMember> collectMembers(@Nullable FileSymbol fileSymbol, @Nonnull Symbol classSymbol) {
        List<RubyMethodMember> members = new ArrayList<>();
        for (Symbol method : collectMethods(fileSymbol, classSymbol)) {
            RubyMethodMember member = createMember(fileSymbol, method);
            if (member != null) {
                members.add(member);
            }
        }
        return members;
    }

    @Override
    @RequiredReadAction
    public boolean isValidFor(Editor editor, PsiFile file) {
        return findClassSymbol(editor, file) != null;
    }

    @Override
    @RequiredUIAccess
    public void invoke(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
        PsiElement anchor = file.findElementAt(editor.getCaretModel().getOffset());
        Symbol classSymbol = ReadAction.compute(() -> findClassSymbol(editor, file));
        if (anchor == null || classSymbol == null) {
            return;
        }
        List<RubyMethodMember> members = ReadAction.compute(() -> collectMembers(getFileSymbol(project), classSymbol));
        if (members.isEmpty()) {
            return;
        }

        MemberChooserBuilder<RubyMethodMember> builder = MemberChooserBuilder.create(members.toArray(new RubyMethodMember[0]));
        builder.withTitle(getTitle());
        builder.withMultipleSelection();
        builder.showAsync(project, dataHolder -> {
            List<ClassMember> selected = dataHolder.getUserData(ClassMember.KEY_OF_LIST);
            if (selected == null || selected.isEmpty()) {
                return;
            }
            WriteCommandAction.runWriteCommandAction(project, getTitle().get(), null, () -> {
                List<String> methods = new ArrayList<>();
                for (ClassMember member : selected) {
                    String method = RubyMethodGenerator.generate(((RubyMethodMember) member).getElement());
                    if (method != null) {
                        methods.add(method);
                    }
                }
                int caretOffset = RubyMethodGenerator.insert(project, anchor, methods);
                if (caretOffset >= 0) {
                    editor.getCaretModel().moveToOffset(caretOffset);
                }
            }, file);
        });
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return RubyLanguage.INSTANCE;
    }

    @Nullable
    protected static FileSymbol getFileSymbol(@Nonnull Project project) {
        return LastSymbolStorage.getInstance(project).getSymbol();
    }

    /**
     * @return the symbol of the class or module around the caret
     */
    @Nullable
    @RequiredReadAction
    protected static Symbol findClassSymbol(@Nonnull Editor editor, @Nonnull PsiFile file) {
        if (!(file instanceof RFile)) {
            return null;
        }
        FileSymbol fileSymbol = getFileSymbol(file.getProject());
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        RContainer container = PsiTreeUtil.getParentOfType(element, RContainer.class);
        while (container != null) {
            Symbol symbol = SymbolUtil.getSymbolByContainer(fileSymbol, container);
            if (symbol != null && (symbol.getType() == Type.CLASS || symbol.getType() == Type.MODULE)) {
                return symbol;
            }
            container = PsiTreeUtil.getParentOfType(container, RContainer.class);
        }
        return null;
    }

    /**
     * @return the chooser entry of a method symbol, null if it has no element
     */
    @Nullable
    @RequiredReadAction
    private static RubyMethodMember createMember(@Nullable FileSymbol fileSymbol, @Nonnull Symbol method) {
        PsiElement element;
        String owner = null;
        if (method instanceof ForeignSymbol foreignSymbol) {
            element = foreignSymbol.getPsiElement();
            RubyInteropProvider provider = RubyInterop.forElement(element);
            owner = provider != null ? provider.getContainingClassName(element) : null;
        }
        else {
            RPsiElement prototype = method.getLastVirtualPrototype(fileSymbol);
            element = prototype != null ? RVirtualPsiUtil.findPsiByVirtualElement(prototype, method.getProject()) : null;
            Symbol parent = method.getParentSymbol();
            owner = parent != null ? parent.getName() : null;
        }
        if (element == null || !element.isValid()) {
            return null;
        }
        String signature = RubyMethodGenerator.getSignature(element);
        if (signature == null) {
            return null;
        }
        PsiElement ownerElement = element.getParent() != null ? PsiTreeUtil.getParentOfType(element, RContainer.class) : null;
        MemberChooserObject ownerNode = new RubyMemberOwner(owner != null ? owner : "", ownerElement != null ? IconDescriptorUpdaters.getIcon(ownerElement, 0) : null);
        return new RubyMethodMember(element, signature, ownerNode);
    }
}
