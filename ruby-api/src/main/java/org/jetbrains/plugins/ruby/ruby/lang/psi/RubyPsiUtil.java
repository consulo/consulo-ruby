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
package org.jetbrains.plugins.ruby.ruby.lang.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.component.util.Iconable;
import consulo.document.Document;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.RSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RExpressionSubstitution;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RStringLiteral;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RAliasStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RCompoundStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RObjectClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpressionInParens;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RField;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;
import org.jetbrains.plugins.ruby.ruby.presentation.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oleg, Roman Chernyatchik
 * @since 2006-07-24
 */
public class RubyPsiUtil {
    private static final Logger LOG = Logger.getInstance(RubyPsiUtil.class);
    private static final String TEMP_FILE_NAME = "ruby_temp_file_" + RubyPsiUtil.class.hashCode() + "." + RubyFileType.INSTANCE.getDefaultExtension();

    /**
     * Creates Ruby File with text as content
     *
     * @param fileName  name of file
     * @param directory psiDirectory, where file will be created
     * @param text      Text
     * @return file
     * @throws IncorrectOperationException
     */
    @RequiredWriteAction
    @SuppressWarnings({"JavaDoc"})
    public static RFile createFileFromText(String fileName, PsiDirectory directory, String text) throws IncorrectOperationException {
        PsiFile file = directory.createFile(fileName);
        PsiFileFactory factory = getPsiElementFactory(directory.getProject());
        PsiFile templateFile = factory.createFileFromText(TEMP_FILE_NAME, text);
        file.addRange(templateFile.getFirstChild(), templateFile.getLastChild());
        return (RFile) file;
    }

    public static RFile createDummyRubyFile(Project project, @Nonnull String text) {
        FileType type = RubyFileType.INSTANCE;
        return (RFile) createFileFromText(project, TEMP_FILE_NAME, type, text);
    }


    public static List<RPsiElement> getTopLevelElements(Project project, @Nonnull String text) {
        RFile rFile = createDummyRubyFile(project, text);
        return rFile.getCompoundStatement().getStatements();
    }

    @Nonnull
    private static PsiFile createFileFromText(
        @Nonnull Project project,
        @Nonnull String name,
        @Nonnull FileType fileType,
        @Nonnull String text
    ) {
        return getPsiElementFactory(project).createFileFromText(name, fileType, text);
    }

    private static PsiFileFactory getPsiElementFactory(Project project) {
        return PsiFileFactory.getInstance(project);
    }

    /**
     * Returns elements in the psi tree
     *
     * @param filter  Types of expected child
     * @param element tree parent node
     * @return PsiElement - child psiElement
     */
    @Nonnull
    public static List<PsiElement> getChildrenByFilter(@Nonnull PsiElement element, @Nonnull TokenSet filter) {
        List<PsiElement> list = new ArrayList<>();
        ASTNode node = element.getNode();
        if (node != null) {
            for (ASTNode childNode : node.getChildren(filter)) {
                list.add(childNode.getPsi());
            }
        }
        return list;
    }

    /**
     * Returns child elements in the psi tree
     *
     * @param filter  Types of expected child
     * @param element tree parent node
     * @return PsiElement - child psiElement
     */

    @Nonnull
    public static List<PsiElement> getChildrenByFilter(@Nonnull PsiElement element, IElementType filter) {
        return getChildrenByFilter(element, TokenSet.create(filter));
    }

    /**
     * Returns child element in the psi tree
     *
     * @param filter  Types of expected child
     * @param number  number
     * @param element tree parent node
     * @return PsiElement - child psiElement
     */
    @Nullable
    public static PsiElement getChildByFilter(@Nonnull PsiElement element, @Nonnull TokenSet filter, int number) {
        List<PsiElement> list = getChildrenByFilter(element, filter);
        return (0 <= number && number < list.size()) ? list.get(number) : null;
    }

    /**
     * Returns child element in the psi tree
     *
     * @param filter  Types of expected child
     * @param number  number
     * @param element tree parent node
     * @return PsiElement - child psiElement
     */
    @Nullable
    public static PsiElement getChildByFilter(@Nonnull PsiElement element, IElementType filter, int number) {
        return getChildByFilter(element, TokenSet.create(filter), number);
    }

    /**
     * Returns first element in psiTree left to element of type c
     *
     * @param c       object of required type
     * @param element anchor node
     * @return RElement object if found, null otherwise
     */
    @Nullable
    @RequiredReadAction
    public static <T extends PsiElement> T getElementToLeftWithSameParent(@Nonnull PsiElement element, Class<T> c) {
        PsiElement left = element.getPrevSibling();
        while (left != null && !(c.isInstance(left))) {
            left = left.getPrevSibling();
        }
        //noinspection unchecked
        return left != null ? (T) left : null;
    }

    /**
     * Returns  first element in psiTree right to element of type c
     *
     * @param c       object of required type
     * @param element anchor node
     * @return RElement object if found, null otherwise
     */
    @RequiredReadAction
    public static <T extends PsiElement> @Nullable T getElementToRightWithSameParent(@Nonnull PsiElement element, Class<T> c) {
        PsiElement right = element.getNextSibling();
        while (right != null && !(c.isInstance(right))) {
            right = right.getNextSibling();
        }
        //noinspection unchecked
        return right != null ? (T) right : null;
    }

    /**
     * Run formatter for minimal formatting block, containing given element
     *
     * @param element PsiElement to reformat
     */
    public static void reformat(@Nonnull PsiElement element) {
        Project project = element.getProject();
        PsiFile file = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        assert document != null;
        // Commit document before formatting!
        PsiDocumentManager.getInstance(project).commitDocument(document);
        try {
            CodeStyleManager.getInstance(project).reformat(element);
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }

    @Nonnull
    @RequiredReadAction
    public static <T extends PsiElement> List<T> getChildrenByType(RPsiElement rPsiElement, Class<T> c) {
        List<T> list = new ArrayList<>();
        for (PsiElement psiElement : rPsiElement.getChildren()) {
            if (c.isInstance(psiElement)) {
                //noinspection unchecked
                list.add((T) psiElement);
            }
        }

        return list;
    }

    @Nullable
    @RequiredReadAction
    public static <T extends PsiElement> T getChildByType(RPsiElement rPsiElement, Class<T> c, int number) {
        List<T> list = getChildrenByType(rPsiElement, c);
        return list.size() > number ? list.get(number) : null;
    }

    public static boolean isBefore(PsiElement element1, PsiElement element2) {
        return element1.getTextOffset() < element2.getTextOffset();
    }

    /**
     * Returns presentable name for each rPsiElement, used in Folding, Structure view
     *
     * @param element element to get presentable name for
     * @return String - name
     */
    @RequiredReadAction
    public static LocalizeValue getPresentableName(@Nonnull PsiElement element) {
        return switch (element) {
            case RFile rFile -> LocalizeValue.of(rFile.getName());
            case RObjectClass objectClass -> objectClass.getPresentableName();
            case RModule module -> LocalizeValue.of(module.getFullName());
            case RClass rClass -> LocalizeValue.of(rClass.getFullName());
            case RMethod method -> method.getPresentableName();
            case RField field -> LocalizeValue.of(field.getText());
            case RConstant constant -> LocalizeValue.of(constant.getName());
            case RGlobalVariable globalVar -> LocalizeValue.of(globalVar.getText());
            case RAliasStatement alias -> alias.getPresentableText();
            case PsiFile file -> LocalizeValue.of(file.getName());
            default -> LocalizeValue.of(element.getText());
        };
    }

    /**
     * Returns icon for each rPsiElement
     *
     * @param element element to get icon for
     * @return Icon
     */
    @RequiredReadAction
    public static Image getIcon(PsiElement element) {
        return switch (element) {
            case RFile file -> RFilePresentationUtil.getIconByRFile(file);
            case RObjectClass objectClass -> IconDescriptorUpdaters.getIcon(objectClass, Iconable.ICON_FLAG_VISIBILITY);
            case RContainer container -> RContainerPresentationUtil.getIconWithModifiers(container);
            case RField field -> RFieldPresentationUtil.getIconByRField(field);
            case RAliasStatement alias -> RubyIcons.RUBY_ALIAS_NODE;
            case RCall call -> {
                StructureType type = call.getType();
                if (type == StructureType.CALL_REQUIRE) {
                    yield RubyIcons.RUBY_REQUIRE_NODE;
                }
                if (type == StructureType.CALL_INCLUDE) {
                    yield RubyIcons.RUBY_INCLUDE_NODE;
                }
                if (type == StructureType.CALL_EXTEND) {
                    yield RubyIcons.RUBY_INCLUDE_NODE;
                }
                if (type == StructureType.CALL_INCLUDE_CLASS || type == StructureType.CALL_INCLUDE_PACKAGE) {
                    yield RubyIcons.RUBY_INCLUDE_NODE;
                }
                if (type == StructureType.FIELD_ATTR_CALL) {
                    yield RFieldAttrPresentationUtil.getAttrIcon(((RCallBase) call).getFieldAttrType());
                }
                yield null;
            }
            case RConstant constant when constant.isInDefinition() -> RConstantPresentationUtil.getIcon();
            case RGlobalVariable globalVar when globalVar.isInDefinition() -> RGlobalVariablePresentationUtil.getIcon();
            default -> null;
        };
    }

    @Nullable
    public static RFile getRFile(@Nonnull PsiElement element) {
        return element.getContainingFile() instanceof RFile file ? file : null;
    }

    @RequiredWriteAction
    public static void replaceInParent(@Nonnull PsiElement oldElement, @Nonnull PsiElement... newElements) {
        addBeforeInParent(oldElement, newElements);
        removeElements(oldElement);
    }

    @RequiredWriteAction
    public static void addToEnd(@Nonnull PsiElement psiElement, @Nonnull PsiElement... newElements) {
        ASTNode psiNode = psiElement.getNode();
        LOG.assertTrue(psiNode != null);
        for (PsiElement newElement : newElements) {
            //noinspection ConstantConditions
            psiNode.addChild(newElement.getNode());
        }
    }

    @RequiredWriteAction
    public static void addBeforeInParent(@Nonnull PsiElement anchor, @Nonnull PsiElement... newElements) {
        PsiElement psiParent = anchor.getParent();
        LOG.assertTrue(psiParent != null);
        ASTNode parentNode = psiParent.getNode();
        ASTNode anchorNode = anchor.getNode();
        LOG.assertTrue(parentNode != null);
        LOG.assertTrue(anchorNode != null);
        for (PsiElement newElement : newElements) {
            //noinspection ConstantConditions
            parentNode.addChild(newElement.getNode(), anchorNode);
        }
    }

    @RequiredWriteAction
    public static void removeElements(@Nonnull PsiElement... elements) {
        ASTNode parentNode = elements[0].getParent().getNode();
        LOG.assertTrue(parentNode != null);
        for (PsiElement element : elements) {
            //noinspection ConstantConditions
            parentNode.removeChild(element.getNode());
        }
    }

    @Nullable
    public static RPsiElement getCoveringRPsiElement(@Nonnull PsiElement psiElement) {
        PsiElement current = psiElement;
        while (current != null) {
            if (current instanceof RPsiElement element) {
                return element;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Search the most top level RClass (in RFile) that contains given element.
     * Returns given element if it is such top level class.
     *
     * @param rContainer given element
     * @return Null if element hasn't any class that contains him
     */
    @Nullable
    public static RClass getContainingUpperRClass(@Nonnull RContainer rContainer) {
        RClass upperRClass = rContainer instanceof RClass rClass ? rClass : null;
        RClass curr = getContainingRClassByContainer(rContainer);

        while (curr != null) {
            upperRClass = curr;
            curr = getContainingRClassByContainer(curr);
        }
        return upperRClass;
    }

    @Nullable
    public static RClass getContainingRClassByContainer(@Nonnull RContainer rContainer) {
        RContainer current = rContainer.getParentContainer();
        while (current != null) {
            if (current instanceof RClass rClass) {
                return rClass;
            }
            current = current.getParentContainer();
        }
        return null;
    }

    @Nullable
    public static RContainer getParentVContainer(@Nonnull PsiElement psiElement) {
        PsiElement element = psiElement;
        while (element != null) {
            if (element instanceof RContainer container) {
                return container.getVirtualParentContainer();
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    public static RClass getContainingRClass(@Nonnull PsiElement psiElement) {
        PsiElement element = psiElement;
        while (element != null) {
            if (element instanceof RContainer) {
                break;
            }
            element = element.getParent();
        }

        if (element == null) {
            return null;
        }

        RContainer current = (RContainer) element;
        while (current != null) {
            if (current instanceof RClass rClass) {
                return rClass;
            }
            current = current.getParentContainer();
        }
        return null;
    }

    /**
     * Gets ruby method for given psi element. If element is method return element
     *
     * @param psiElement Given psi element
     * @return null or method
     */
    @Nullable
    public static RMethod getContainingRMethod(@Nonnull PsiElement psiElement) {
        PsiElement element = psiElement;
        while (element != null) {
            if (element instanceof RContainer) {
                break;
            }
            element = element.getParent();
        }

        if (element == null) {
            return null;
        }

        RContainer current = (RContainer) element;
        while (current != null) {
            if (current instanceof RMethod method) {
                return method;
            }
            current = current.getParentContainer();
        }
        return null;
    }

    /*
     * Looks for statement, containing this PsiElement
     */
    @Nullable
    public static RPsiElement getStatement(PsiElement element) {
        PsiElement statement = element;
        // RUBY-1605. We want real statement, but not expressions in parens or expression substitutions in strings
        do {
            statement = PsiTreeUtil.getParentOfType(statement, RCompoundStatement.class);
        }
        while (statement != null && (statement.getParent() instanceof RExpressionInParens || statement.getParent() instanceof RExpressionSubstitution));
        return statement instanceof RCompoundStatement compoundStmt ? getStatement(compoundStmt, element) : null;
    }

    /*
     * Looks for statement, containing this PsiElement
     */
    @Nullable
    public static RPsiElement getStatement(@Nonnull RPsiElement compoundStatement, PsiElement element) {
        while (element != null && element.getParent() != compoundStatement) {
            element = element.getParent();
        }
        return element instanceof RPsiElement ? (RPsiElement) element : null;
    }

    @Nonnull
    @RequiredReadAction
    public static String evaluate(@Nonnull PsiElement element) {
        if (element instanceof RSymbol symbol) {
            return evaluate(symbol.getObject());
        }
        // String like processing
        if (element instanceof RStringLiteral stringLiteral && !stringLiteral.hasExpressionSubstitutions()) {
            return stringLiteral.getContent();
        }
        return element.getText();
    }
}