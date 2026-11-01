/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.rails.langs.rhtml.lang;

import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.FormattingDocumentModel;
import consulo.language.impl.psi.SourceTreeToPsiMap;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.template.TemplateLanguage;
import consulo.language.template.TemplateLanguageFileViewProvider;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.html.language.HTMLLanguage;
import consulo.xhtml.language.XHTMLLanguage;
import consulo.xml.psi.formatter.xml.HtmlPolicy;
import consulo.xml.psi.formatter.xml.ReadOnlyBlock;
import consulo.xml.psi.formatter.xml.XmlBlock;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.RHTMLFile;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.RHTMLPsiUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 02.04.2007
 */
public class eRubyLanguage extends Language implements TemplateLanguage
{
	public static final eRubyLanguage INSTANCE = new eRubyLanguage();

	public eRubyLanguage()
	{
		super("E-RUBY", "application/x-httpd-eruby");
	}


	public static Block createRHTMLRoot(final PsiElement element, final CodeStyleSettings settings, final FormattingDocumentModel documentModel)
	{
		final PsiFile file = element.getContainingFile();
		final RHTMLFile rhtmlFile = RHTMLPsiUtil.getRHTMLFileRoot(file);
		assert rhtmlFile != null;

		final TemplateLanguageFileViewProvider rhtmlViewProvider = rhtmlFile.getViewProvider();
		final Language templateLang = rhtmlViewProvider.getTemplateDataLanguage();


		if(templateLang == HTMLLanguage.INSTANCE || templateLang == XHTMLLanguage.INSTANCE)
		{
			final PsiFile psiRoot = rhtmlViewProvider.getPsi(templateLang);
			final ASTNode rootNode = SourceTreeToPsiMap.psiElementToTree(psiRoot);
			return new XmlBlock(rootNode, null, null, new HtmlPolicy(settings, documentModel), null, null);
		}
		else
		{
			return new ReadOnlyBlock(file.getNode());
		}
	}
	//    public static Block createRHTMLRoot(final PsiElement element,
	//                                        final CodeStyleSettings settings,
	//                                        final FormattingDocumentModel documentModel) {
	//        final PsiFile file = element.getContainingFile();
	//        final RHTMLFile rhtmlFile = RHTMLPsiUtil.getRHTMLFileRoot(file);
	//        assert rhtmlFile != null;
	//
	//        final RHTMLFileViewProvider rhtmlViewProvider = rhtmlFile.getViewProvider();
	//        final Language templateLang = rhtmlViewProvider.getTemplateDataLanguage();
	//
	//        //TODO leave only HTML, not XML
	//        if (templateLang == StdLanguages.HTML || templateLang == StdLanguages.XHTML) {
	//            final PsiFile psiRoot = rhtmlViewProvider.getPsi(RHTMLLanguage.RHTML);
	//            final ASTNode rootNode = SourceTreeToPsiMap.psiElementToTree(psiRoot);
	//            return new XmlBlock(rootNode, null, null, new HtmlPolicy(settings, documentModel), null, null);
	//        } else {
	//            return new ReadOnlyBlock(file.getNode());
	//        }
	//    }

}

