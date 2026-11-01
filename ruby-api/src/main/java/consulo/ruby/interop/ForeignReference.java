package consulo.ruby.interop;

import consulo.language.psi.PsiReference;

/**
 * A reference from Ruby code to an element of another language, e.g. a symbol naming a Java class.
 */
public interface ForeignReference extends PsiReference {
}
