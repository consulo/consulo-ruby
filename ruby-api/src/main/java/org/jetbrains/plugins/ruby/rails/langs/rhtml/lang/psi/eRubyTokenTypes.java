package org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi;

import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.parsing.RHTMLTokenTypeEx;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.tree.IRHTMLElementType;
import consulo.language.ast.IElementType;

/**
 * @author VISTALL
 * @since 24.09.13.
 */
public interface eRubyTokenTypes
{
	IElementType TEMPLATE_CHARACTERS_IN_RHTML = RHTMLTokenTypeEx.TEMPLATE_CHARACTERS_IN_RHTML;

	IElementType RHTML_INJECTION_IN_HTML = RHTMLTokenTypeEx.RHTML_INJECTION_IN_HTML;
}
