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

package org.jetbrains.plugins.ruby.ruby.lang.psi.visitors;

import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.RSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RStringLiteral;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RObjectClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RFunctionArgumentList;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RSingletonMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modifierStatements.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.RLBeginStatementImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockVariables;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCommandCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RFunctionCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RColonReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RDotReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RClassVariable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RInstanceVariable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 14.07.2006
 */
public abstract class RubyElementVisitor extends PsiElementVisitor
{

	@Override
	public void visitElement(PsiElement element)
	{
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////// Methods to ovveride //////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public void visitRListOfExpressions(RListOfExpressions rListOfExpressions)
	{
		visitElement(rListOfExpressions);
	}

	public void visitRFunctionCall(RFunctionCall rFunctionCall)
	{
		visitRCall(rFunctionCall);
	}

	public void visitRCommandCall(RCommandCall rCommandCall)
	{
		visitRCall(rCommandCall);
	}


	public void visitRSingletonMethod(RSingletonMethod rsMethod)
	{
		visitElement(rsMethod);
	}

	public void visitRMethod(RMethod rMethod)
	{
		visitElement(rMethod);
	}

	public void visitRObjectClass(RObjectClass rsClass)
	{
		visitElement(rsClass);
	}

	public void visitRClass(RClass rClass)
	{
		visitElement(rClass);
	}

	public void visitRModule(RModule rModule)
	{
		visitElement(rModule);
	}

	public void visitRFile(RFile rFile)
	{
		visitElement(rFile);
	}

	public void visitRConstant(RConstant rConstant)
	{
		visitElement(rConstant);
	}

	public void visitRGlobalVariable(RGlobalVariable rGlobalVariable)
	{
		visitElement(rGlobalVariable);
	}

	public void visitRInstanceVariable(RInstanceVariable rInstanceVariable)
	{
		visitElement(rInstanceVariable);
	}

	public void visitRClassVariable(RClassVariable rClassVariable)
	{
		visitElement(rClassVariable);
	}

	public void visitRPseudoConstant(RPseudoConstant rPseudoConstant)
	{
		visitElement(rPseudoConstant);
	}

	public void visitRIdentifier(RIdentifier rIdentifier)
	{
		visitElement(rIdentifier);
	}

	public void visitRFid(RFid rFid)
	{
		visitElement(rFid);
	}

	public void visitRMethodName(RMethodName rMethodName)
	{
		visitRName(rMethodName);
	}

	public void visitRClassName(RClassName rClassName)
	{
		visitRName(rClassName);
	}

	public void visitRModuleName(RModuleName rModuleName)
	{
		visitRName(rModuleName);
	}

	public void visitRSuperClass(RSuperClass rSuperClass)
	{
		visitRName(rSuperClass);
	}

	public void visitRFName(final RFName rFName)
	{
		visitRName(rFName);
	}

	public void visitRName(final RName name)
	{
		visitElement(name);
	}

	public void visitRBodyStatement(RBodyStatement rBodyStatement)
	{
		visitElement(rBodyStatement);
	}

	public void visitRCompoundStatement(RCompoundStatement rCompoundStatement)
	{
		visitElement(rCompoundStatement);
	}

	public void visitRStringLiteral(RStringLiteral rStringLiteral)
	{
		visitElement(rStringLiteral);
	}

	public void visitRColonReference(RColonReference rColonReference)
	{
		visitRReference(rColonReference);
	}

	public void visitRDotReference(RDotReference rDotReference)
	{
		visitRReference(rDotReference);
	}

	public void visitRAliasStatement(RAliasStatement rAliasStatement)
	{
		visitElement(rAliasStatement);
	}

	public void visitRLBeginStatement(RLBeginStatementImpl rlBeginStatement)
	{
		visitElement(rlBeginStatement);
	}

	public void visitRParameter(RArgument rArgument)
	{
		visitElement(rArgument);
	}

	public void visitRFunctionArgumentList(final RFunctionArgumentList list)
	{
		visitElement(list);
	}

	public void visitRAssignmentExpression(final RAssignmentExpression assignmentExpression)
	{
		visitElement(assignmentExpression);
	}

	public void visitRMultiAssignmentExpression(final RMultiAssignmentExpression multiAssignmentExpression)
	{
		visitElement(multiAssignmentExpression);
	}

	public void visitRSymbol(final RSymbol rSymbol)
	{
		visitElement(rSymbol);
	}

	public void visitRReference(final RReference rReference)
	{
		visitElement(rReference);
	}

	public void visitRCall(final RCall rCall)
	{
		visitElement(rCall);
	}

	public void visitRBinaryExpression(final RBinaryExpression rBinaryExpression)
	{
		visitElement(rBinaryExpression);
	}

	public void visitRUnaryExpression(final RUnaryExpression rUnaryExpression)
	{
		visitElement(rUnaryExpression);
	}

	public void visitRBlockVariables(final RBlockVariables blockVariables)
	{
		visitElement(blockVariables);
	}

	public void visitRIfStatement(final RIfStatement ifStatement)
	{
		visitElement(ifStatement);
	}

	public void visitRBreakStatement(final RBreakStatement breakStatement)
	{
		visitElement(breakStatement);
	}

	public void visitRCaseStatement(final RCaseStatement rCaseStatement)
	{
		visitElement(rCaseStatement);
	}

	public void visitRConditionalStatement(final RConditionalStatement rConditionalStatement)
	{
		visitElement(rConditionalStatement);
	}

	public void visitRCondition(final RCondition rCondition)
	{
		visitElement(rCondition);
	}

	public void visitRDefinedStatement(final RDefinedStatement rDefinedStatement)
	{
		visitElement(rDefinedStatement);
	}

	public void visitRForStatement(final RForStatement rForStatement)
	{
		visitElement(rForStatement);
	}

	public void visitRLEndStatement(final RLEndStatement rlEndStatement)
	{
		visitElement(rlEndStatement);
	}

	public void visitRNextStatement(final RNextStatement rNextStatement)
	{
		visitElement(rNextStatement);
	}

	public void visitRRedoStatement(final RRedoStatement rRedoStatement)
	{
		visitElement(rRedoStatement);
	}

	public void visitRRetryStatement(final RRetryStatement rRetryStatement)
	{
		visitElement(rRetryStatement);
	}

	public void visitRReturnStatement(final RReturnStatement rReturnStatement)
	{
		visitElement(rReturnStatement);
	}

	public void visitRUndefStatement(final RUndefStatement rUndefStatement)
	{
		visitElement(rUndefStatement);
	}

	public void visitRUnlessStatement(final RUnlessStatement rUnlessStatement)
	{
		visitElement(rUnlessStatement);
	}

	public void visitRUntilStatement(final RUntilStatement rUntilStatement)
	{
		visitElement(rUntilStatement);
	}

	public void visitRWhenCase(final RWhenCase rWhenCase)
	{
		visitElement(rWhenCase);
	}

	public void visitRWhileStatement(final RWhileStatement rWhileStatement)
	{
		visitElement(rWhileStatement);
	}

	public void visitRYieldStatement(final RYieldStatement rYieldStatement)
	{
		visitElement(rYieldStatement);
	}

	public void visitRBeginEndBlockStatement(final RBeginEndBlockStatement rBeginEndBlockStatement)
	{
		visitElement(rBeginEndBlockStatement);
	}

	public void visitRElseBlock(final RElseBlock rElseBlock)
	{
		visitElement(rElseBlock);
	}

	public void visitRElsifBlock(final RElsifBlock rElsifBlock)
	{
		visitElement(rElsifBlock);
	}

	public void visitREnsureBlock(final REnsureBlock rEnsureBlock)
	{
		visitElement(rEnsureBlock);
	}

	public void visitRRescueBlock(final RRescueBlock rRescueBlock)
	{
		visitElement(rRescueBlock);
	}

	public void visitRIfModStatement(final RIfModStatement rIfModStatement)
	{
		visitElement(rIfModStatement);
	}


	public void visitRRescueModStatement(final RRescueModStatement rRescueModStatement)
	{
		visitElement(rRescueModStatement);
	}

	public void visitRUnlessModStatement(final RUnlessModStatement rUnlessModStatement)
	{
		visitElement(rUnlessModStatement);
	}

	public void visitRUntilModStatement(final RUntilModStatement rUntilModStatement)
	{
		visitElement(rUntilModStatement);
	}

	public void visitRWhileModStatement(final RWhileModStatement rWhileModStatement)
	{
		visitElement(rWhileModStatement);
	}
}
