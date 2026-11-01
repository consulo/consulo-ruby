/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.ruby.codeInsight.types;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.progress.ProgressManager;
import consulo.disposer.Disposable;
import consulo.ide.ServiceManager;
import consulo.language.psi.AnyPsiChangeListener;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiReference;
import consulo.project.Project;
import consulo.util.lang.ref.Ref;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.usages.Access;
import org.jetbrains.plugins.ruby.ruby.codeInsight.usages.AssignAccess;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.Instruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.RControlFlowOwner;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgumentList;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.dataFlow.DFAEngine;
import org.jetbrains.plugins.ruby.ruby.lang.psi.dataFlow.types.TypeDfaInstance;
import org.jetbrains.plugins.ruby.ruby.lang.psi.dataFlow.types.TypesSemilattice;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RBinaryExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RUnaryExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references.RReferenceBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Apr 21, 2008
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class TypeInferenceHelper implements Disposable {
    @Nonnull
    public static TypeInferenceHelper getInstance(@Nonnull Project project) {
        return ServiceManager.getService(project, TypeInferenceHelper.class);
    }

    private consulo.util.lang.ref.Ref<TypeInferenceContext> myTypeContext = new Ref<TypeInferenceContext>();

    @Inject
    public TypeInferenceHelper(Project project) {
        project.getMessageBus().connect(this).subscribe(AnyPsiChangeListener.class, new AnyPsiChangeListener() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {
                clearContext();
            }
        });
    }

    @Override
    public void dispose() {

    }

    public RType inferLocalVariableType(@Nonnull final RControlFlowOwner owner, @Nonnull final RIdentifier usage) {
        ProgressManager.getInstance().checkCanceled();

        TypeInferenceContext context = getContext();
        if (context == null) {
            return RType.NOT_TYPED;
        }
        Map<RIdentifier, RType> map = context.localVariablesTypesCache.get(owner);
        if (map == null) {
            map = new HashMap<RIdentifier, RType>();
            context.localVariablesTypesCache.put(owner, map);
            inferLocalVariablesTypes(context, owner, map);
        }
        RType type = map.get(usage);
        return type != null ? type : RType.NOT_TYPED;
    }

    public void inferLocalVariablesTypes(@Nonnull final TypeInferenceContext context, @Nonnull final RControlFlowOwner owner, final Map<RIdentifier, RType> map) {
        context.methodsBeingInferred.add(owner);
        Instruction[] flow = owner.getControlFlow();
        TypeDfaInstance dfaInstance = new TypeDfaInstance(context.fileSymbol, map);
        TypesSemilattice semilattice = new TypesSemilattice();
        DFAEngine<Map<String, RType>> engine = new DFAEngine<Map<String, RType>>(flow, dfaInstance, semilattice);
        engine.performDFA();
        context.methodsBeingInferred.remove(owner);
    }

    public RType inferCallType(final RCallBase call) {
        TypeInferenceContext context = getContext();
        if (context == null) {
            return RType.NOT_TYPED;
        }
        FileSymbol fileSymbol = context.fileSymbol;
        PsiElement command = call.getPsiCommand();

        // Constructors handling
        if (command instanceof RReference) {
            RReferenceBase ref = (RReferenceBase) command;
            if (ref.isConstructorLike()) {
                return ref.getType(fileSymbol);
            }
        }

        // Common call handling
        return inferTypeOfReference(command.getReference(), call.getArguments());
    }

    public RType inferBinaryExpressionType(final RBinaryExpression expression) {
        return inferTypeOfReference(expression.getReference(), Collections.singletonList(expression.getRightOperand()));
    }

    public RType inferUnaryExpressionType(final RUnaryExpression expression) {
        return inferTypeOfReference(expression.getReference(), Collections.singletonList(expression.getElement()));
    }

    public RType inferTypeOfReference(@Nullable final PsiReference ref, final List<RPsiElement> callArgs) {
        if (ref == null) {
            return RType.NOT_TYPED;
        }
        TypeInferenceContext context = getContext();
        if (context == null) {
            return RType.NOT_TYPED;
        }
        FileSymbol fileSymbol = context.fileSymbol;

        Symbol symbol = ResolveUtil.resolveToSymbol(fileSymbol, ref);
        if (symbol == null) {
            return RType.NOT_TYPED;
        }
        return inferCallTypeBySymbol(symbol, callArgs);
    }

    public RType inferCallTypeBySymbol(@Nonnull final Symbol symbol, final List<RPsiElement> callArgs) {
        ProgressManager.getInstance().checkCanceled();

        TypeInferenceContext context = getInstance(symbol.getProject()).getContext();
        if (context == null) {
            return RType.NOT_TYPED;
        }
        FileSymbol fileSymbol = context.fileSymbol;
        // Java method handling
        if (symbol.getType() == Type.FOREIGN_METHOD) {
            return RTypeUtil.createTypeBySymbol(fileSymbol, symbol, Context.ALL, true);
        }
        // Common ruby method call handling
        RPsiElement prototype = symbol.getLastVirtualPrototype(fileSymbol);
        if (prototype == null) {
            return RType.NOT_TYPED;
        }
        RPsiElement element = RVirtualPsiUtil.findPsiByVirtualElement(prototype, symbol.getProject());
        if (!(element instanceof RMethod)) {
            return RType.NOT_TYPED;
        }

        // Well, here we have all the types of arguments and can infer type of method
        return inferTypeOfMethodCall((RMethod) element, callArgs, context);
    }

    private RType inferTypeOfMethodCall(@Nonnull final RMethod method, final List<RPsiElement> callArgs, @Nonnull final TypeInferenceContext context) {
        FileSymbol fileSymbol = context.fileSymbol;
        RType commentType = TypeCommentsHelper.tryToExtractTypeFromComment(method, fileSymbol);
        if (commentType != null) {
            return commentType;
        }

        // Check for infinite loop
        if (context.methodsBeingInferred.contains(method) || context.depth > TypeInferenceContext.MAX_DEPTH) {
            return RType.NOT_TYPED;
        }

        try {
            context.methodsBeingInferred.add(method);
            context.depth++;

            // we remove method variables caches of method
            context.localVariablesTypesCache.remove(method);
            HashMap<RIdentifier, RType> localVariablesTypes = new HashMap<RIdentifier, RType>();
            context.localVariablesTypesCache.put(method, localVariablesTypes);

            // Here we set types of arguments of call
            RArgumentList argumentList = method.getArgumentList();
            if (argumentList != null) {
                // all other types are handled inside TypeDfaInstance.fun in MethodParameterAccess case
                List<RArgument> arguments = argumentList.getArguments();
                for (int i = 0; i < arguments.size(); i++) {
                    RArgument argument = arguments.get(i);
                    RIdentifier id = argument.getIdentifier();
                    ArgumentInfo.Type argType = argument.getType();
                    if (argType == ArgumentInfo.Type.SIMPLE || argType == ArgumentInfo.Type.PREDEFINED) {
                        RPsiElement arg = i < callArgs.size() ? callArgs.get(i) : null;
                        if (arg instanceof RExpression) {
                            localVariablesTypes.put(id, ((RExpression) arg).getType(fileSymbol));
                        }
                    }
                }
            }
            Instruction[] flow = method.getControlFlow();
            TypeDfaInstance dfaInstance = new TypeDfaInstance(fileSymbol, localVariablesTypes);
            TypesSemilattice semilattice = new TypesSemilattice();
            DFAEngine<Map<String, RType>> engine = new DFAEngine<Map<String, RType>>(flow, dfaInstance, semilattice);
            engine.performDFA();


            // here we iterate over last instructions to get joint type
            Instruction lastInstruction = flow[flow.length - 1];
            RType result = RType.NOT_TYPED;
            for (Instruction i : lastInstruction.allPred()) {
                // Here we should take types of statements!!!
                RPsiElement statement = RubyPsiUtil.getStatement(i.getElement());
                if (statement instanceof RExpression) {
                    result = RTypeUtil.joinOr(result, ((RExpression) statement).getType(fileSymbol));
                }
            }

            return result;
        }
        finally {
            context.methodsBeingInferred.remove(method);
            // we remove method variables caches of method
            context.localVariablesTypesCache.remove(method);
            context.depth--;
        }
    }

    @Nullable
    public TypeInferenceContext getContext() {
        return myTypeContext.get();
    }

    public void testAndSet(@Nullable final FileSymbol fileSymbol) {
        TypeInferenceContext context = getContext();
        if (context == null) {
            myTypeContext.set(new TypeInferenceContext(fileSymbol));
        }
    }

    public void clearContext() {
        myTypeContext.set(null);
    }

    public RType inferUsageType(final Access access) {
        TypeInferenceContext context = getContext();
        if (context == null) {
            return RType.NOT_TYPED;
        }
        if (access instanceof AssignAccess) {
            RPsiElement value = ((AssignAccess) access).getValue();
            if (value instanceof RExpression) {
                RExpression expression = (RExpression) value;
                // Prevent stack overflow
                if (context.expressionsBeingInferred.contains(expression)) {
                    return RType.NOT_TYPED;
                }
                try {
                    context.expressionsBeingInferred.add(expression);
                    return expression.getType(context.fileSymbol);
                }
                finally {
                    context.expressionsBeingInferred.remove(expression);
                }
            }
        }
        return RType.NOT_TYPED;
    }
}
