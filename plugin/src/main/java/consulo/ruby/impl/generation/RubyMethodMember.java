package consulo.ruby.impl.generation;

import consulo.language.editor.generation.ClassMemberWithElement;
import consulo.language.editor.generation.MemberChooserObject;
import consulo.language.editor.generation.PsiElementMemberChooserObject;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;

/**
 * A Ruby or foreign method offered for overriding or implementing.
 */
public class RubyMethodMember extends PsiElementMemberChooserObject implements ClassMemberWithElement {
    private final MemberChooserObject myOwner;

    public RubyMethodMember(PsiElement method, String text, MemberChooserObject owner) {
        super(method, text, IconDescriptorUpdaters.getIcon(method, 0));
        myOwner = owner;
    }

    @Override
    public PsiElement getElement() {
        return getPsiElement();
    }

    @Override
    public MemberChooserObject getParentNodeDelegate() {
        return myOwner;
    }
}
