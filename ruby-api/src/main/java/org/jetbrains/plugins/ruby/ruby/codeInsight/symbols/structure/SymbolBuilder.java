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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure;

import consulo.application.ReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.ruby.interop.RubyInterop;
import consulo.logging.Logger;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.cache.psi.*;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.FieldAttrType;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.RFieldAttr;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.InterpretationMode;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.TypeSet;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RAliasStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RObjectClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RSingletonMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RConstantHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RFieldHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RGlobalVarHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RFileUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.FieldType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RField;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;
import org.jetbrains.plugins.ruby.ruby.roots.RubyModuleRootUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Mar 27, 2007
 */
public class SymbolBuilder {
    private static final Logger LOG = Logger.getInstance(SymbolBuilder.class.getName());

    private InterpretationMode myMode;
    private RFile myFile;
    private FileSymbol myFileSymbol;

    private enum Context {
        INSTANCE,
        CLASS
    }


    public SymbolBuilder(@Nonnull final FileSymbol fileSymbol, @Nonnull final RFile file, final InterpretationMode mode) {
        myFileSymbol = fileSymbol;
        myMode = mode;
        myFile = file;
    }

    public void process() {
        if (myMode == InterpretationMode.EXTERNAL) {
            for (RRequire require : myFile.getRequires()) {
                for (String name : require.getNames()) {
                    for (String url : RFileUtil.findUrlsForName(myFileSymbol, name, myFile)) {
                        myFileSymbol.process(url, InterpretationMode.FULL, false);
                    }
                }
            }
        }
        else {
            final Symbol rootSymbol = myFileSymbol.getRootSymbol();
            myFileSymbol.addPrototype(rootSymbol, myFile);
            process(myFile, rootSymbol, Context.INSTANCE);
        }
    }

    private void process(@Nonnull final RPsiElement virtualElement, @Nonnull final Symbol symbol, final Context context) {
        if (virtualElement instanceof RFieldHolder) {
            for (RField field : ((RFieldHolder) virtualElement).getVirtualFields()) {
                processField(field, symbol, context);
            }
        }
        if (virtualElement instanceof RConstantHolder) {
            for (RConstant constant : ((RConstantHolder) virtualElement).getVirtualConstants()) {
                processConstant(constant, symbol);
            }
        }
        if (virtualElement instanceof RGlobalVarHolder) {
            for (RGlobalVariable var : ((RGlobalVarHolder) virtualElement).getVirtualGlobalVars()) {
                processGlobalVar(var, symbol);
            }
        }

        if (virtualElement instanceof RContainer) {
            for (RStructuralElement element : ReadAction.computeNotNull(((RContainer) virtualElement)::getVirtualStructureElements)) {
                final StructureType type = ReadAction.compute(element::getType);
                if (type == StructureType.MODULE) {
                    processModule(((RModule) element), symbol);
                }
                else if (type == StructureType.CLASS) {
                    processClass(((RClass) element), symbol);
                }
                else if (type == StructureType.OBJECT_CLASS) {
                    processObjectClass(((RObjectClass) element), symbol);
                }
                else if (type == StructureType.SINGLETON_METHOD) {
                    processSingletonMethod(((RSingletonMethod) element), symbol);
                }
                else if (type == StructureType.METHOD) {
                    processMethod(((RMethod) element), symbol, context);
                }
                else if (type == StructureType.ALIAS) {
                    processAlias((RAliasStatement) element, symbol);
                }
                else
                    // require or load
                    if (type == StructureType.CALL_REQUIRE || type == StructureType.CALL_LOAD) {
                        if (myMode != InterpretationMode.IGNORE_EXTERNAL) {
                            assert element instanceof RRequire;
                            processRequireOrLoad((RRequire) element, symbol, myMode);
                        }
                    }
                    else
                        // include or extend
                        if (type == StructureType.CALL_INCLUDE || type == StructureType.CALL_EXTEND) {
                            assert element instanceof RInclude;
                            processIncludeOrExtend((RInclude) element, symbol);
                        }
                        else
                            // fieldAttr
                            if (type == StructureType.FIELD_ATTR_CALL) {
                                if (element instanceof RFieldAttr) {
                                    processFieldAttr((RFieldAttr) element, symbol);
                                }
                                else {
                                    // try to get requirements from call
                                    final RFieldAttr fieldAttr = (RFieldAttr) element;
                                    processFieldAttr(fieldAttr, symbol);
                                }
                            }
                            else
                                // include foreign classes and packages
                                if (type == StructureType.CALL_IMPORT) {
                                    processImportJavaClass((RImportJavaClass) element, symbol);
                                }
                                else if (type == StructureType.CALL_INCLUDE_CLASS) {
                                    processIncludeJavaClass((RIncludeJavaClass) element, symbol);
                                }
                                else if (type == StructureType.CALL_INCLUDE_PACKAGE) {
                                    processIncludeJavaPackage((RIncludeJavaPackage) element, symbol);
                                }
                                else {
                                    LOG.error("Cannot process: " + element);
                                }
            }

        }
    }

    public void registerContainerSymbol(@Nonnull final RContainer container, @Nonnull final Symbol symbol) {
        myFileSymbol.registerContainerSymbol(container, symbol);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// Processing virtual containers
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void processModule(@Nonnull final RModule rVirtualModule, @Nonnull final Symbol symbol) {
        final List<String> path = ReadAction.computeNotNull(rVirtualModule::getFullPath);
        final Symbol moduleSymbol = SymbolCoreUtil.create(myFileSymbol, symbol, path, rVirtualModule.isGlobal(), Type.MODULE, rVirtualModule);
        registerContainerSymbol(rVirtualModule, moduleSymbol);
        process(rVirtualModule, moduleSymbol, Context.INSTANCE);
    }

    private void processClass(@Nonnull final RClass rVirtualClass, @Nonnull final Symbol symbol) {
        // creating superclass
        Symbol superClassSymbol = null;
        final RVirtualName superClass = ReadAction.compute(rVirtualClass::getVirtualSuperClass);
        if (superClass != null) {
            superClassSymbol = findForeignOrRuby(symbol, rVirtualClass, superClass, new TypeSet(Type.CLASS, Type.FOREIGN_PROXY_CLASS));
        }

        // creating class
        final List<String> path = ReadAction.computeNotNull(rVirtualClass::getFullPath);
        final Symbol classSymbol = SymbolCoreUtil.create(myFileSymbol, symbol, path, rVirtualClass.isGlobal(), Type.CLASS, rVirtualClass);
        if (superClassSymbol != null) {
            myFileSymbol.addChild(classSymbol, new SpecialSymbol(myFileSymbol, classSymbol, superClassSymbol, Type.SUPERCLASS));
        }

        registerContainerSymbol(rVirtualClass, classSymbol);
        process(rVirtualClass, classSymbol, Context.INSTANCE);
    }

    private void processObjectClass(@Nonnull final RObjectClass rVirtualObjectClass, @Nonnull final Symbol symbol) {
        final String name = ReadAction.compute(rVirtualObjectClass::getName);
        final Symbol objectToAdd = SymbolCoreUtil.find(myFileSymbol, symbol, Arrays.asList(name), false, true, Types.MODULE_OR_CLASS);
        addPrototypeIfNeeded(objectToAdd, rVirtualObjectClass);

        // In this case we should link object class manually
        Symbol symbol2Link = objectToAdd;
        while (symbol2Link != null && !new TypeSet(Type.FILE, Type.CLASS, Type.MODULE).contains(symbol2Link.getType())) {
            symbol2Link = symbol2Link.getParentSymbol();
        }
        assert symbol2Link != null;
        registerContainerSymbol(rVirtualObjectClass, objectToAdd);
        process(rVirtualObjectClass, objectToAdd, Context.CLASS);
    }

    private void processSingletonMethod(@Nonnull final RSingletonMethod singletonMethod, @Nonnull final Symbol symbol) {
        final List<String> path = ReadAction.computeNotNull(singletonMethod::getFullPath);
        final Symbol singletonMethodSymbol;
        final String anObject = path.get(0);
        // self and class name processing
        if (path.size() == 2 && anObject.equals(RubyTokenTypes.kSELF.toString())) {
            final List<String> name = new ArrayList<>(1);
            name.add(ReadAction.computeNotNull(singletonMethod::getName));
            singletonMethodSymbol = SymbolCoreUtil.create(myFileSymbol, symbol, name, false, Type.CLASS_METHOD, singletonMethod);
        }
        else {
            singletonMethodSymbol = SymbolCoreUtil.create(myFileSymbol, symbol, path, false, Type.CLASS_METHOD, singletonMethod);
        }
        myFileSymbol.setEmptyChildren(singletonMethodSymbol);
        addParameters(singletonMethod, singletonMethodSymbol);
        registerContainerSymbol(singletonMethod, singletonMethodSymbol);
        process(singletonMethod, singletonMethodSymbol, Context.INSTANCE);
    }

    private void processMethod(@Nonnull final RMethod rVirtualMethod, @Nonnull final Symbol symbol, final Context context) {
        final String name = ReadAction.computeNotNull(rVirtualMethod::getName);
        final Type type = context == Context.INSTANCE && !RMethod.INITIALIZE.equals(name) ? Type.INSTANCE_METHOD : Type.CLASS_METHOD;
        final Symbol methodSymbol = SymbolCoreUtil.create(myFileSymbol, symbol, Arrays.asList(name), false, type, rVirtualMethod);
        myFileSymbol.setEmptyChildren(methodSymbol);
        addParameters(rVirtualMethod, methodSymbol);
        registerContainerSymbol(rVirtualMethod, methodSymbol);
        process(rVirtualMethod, methodSymbol, Context.INSTANCE);
    }

    private void addParameters(@Nonnull final RMethod method, @Nonnull final Symbol symbol) {
        for (ArgumentInfo arg : ReadAction.computeNotNull(method::getArgumentInfos)) {
            final Type type = getArgumentType(arg.getType());
            myFileSymbol.addChild(symbol, new Symbol(myFileSymbol, arg.getName(), type, symbol, null));
        }
    }

    private void processField(@Nonnull final RField field, @Nonnull final Symbol symbol, final Context context) {
        if (field.getType() == FieldType.CLASS_VARIABLE && context == Context.CLASS) {
            return;
        }

        Type type;
        if (field.getType() == FieldType.CLASS_VARIABLE) {
            type = Type.CLASS_FIELD;
        }
        else if (field.getType() == FieldType.INSTANCE_VARIABLE && context == Context.CLASS) {
            type = Type.CLASS_INSTANCE_FIELD;
        }
        else {
            type = Type.INSTANCE_FIELD;
        }
        myFileSymbol.addChild(symbol, new Symbol(myFileSymbol, ReadAction.compute(field::getName), type, symbol, field));
    }

    private void processGlobalVar(@Nonnull final RGlobalVariable var, @Nonnull final Symbol symbol) {
        myFileSymbol.addChild(symbol, new Symbol(myFileSymbol, var.getText(), Type.GLOBAL_VARIABLE, symbol, var));
    }

    private void processConstant(@Nonnull final RConstant constant, @Nonnull final Symbol symbol) {
        myFileSymbol.addChild(symbol, new Symbol(myFileSymbol, ReadAction.computeNotNull(constant::getName), Type.CONSTANT, symbol, constant));
    }

    private void processIncludeOrExtend(@Nonnull final RInclude include, @Nonnull final Symbol symbol) {
        final Type type = ReadAction.compute(() -> include.getType()) == StructureType.CALL_INCLUDE ? Type.INCLUDE : Type.EXTEND;
        for (RVirtualName path : ReadAction.computeNotNull(include::getVirtualNames)) {
            final Symbol includeSymbol = findForeignOrRuby(symbol, include, path, new TypeSet(Type.MODULE, Type.FOREIGN_PROXY_CLASS));
            // adding to symbol
            if (includeSymbol != null) {
                myFileSymbol.addChild(symbol, new SpecialSymbol(myFileSymbol, symbol, includeSymbol, type));
            }
        }
    }

    private void processFieldAttr(@Nonnull final RFieldAttr fieldAttr, @Nonnull final Symbol symbol) {
        final FieldAttrType type = ReadAction.compute(fieldAttr::getFieldAttrType);
        for (String name : ReadAction.computeNotNull(fieldAttr::getNames)) {
            final Symbol field = SymbolCoreUtil.find(myFileSymbol, symbol, Arrays.asList(name), false, true, Types.FIELDS);
            final RPsiElement prototype = field.getLastVirtualPrototype(myFileSymbol);
            if (type == FieldAttrType.ATTR_READER || type == FieldAttrType.ATTR_ACCESSOR) {
                final SpecialSymbol reader = new SpecialSymbol(myFileSymbol, name, symbol, field, Type.FIELD_READER);
                if (prototype != null) {
                    myFileSymbol.addPrototype(reader, prototype);
                }
                myFileSymbol.addPrototype(reader, fieldAttr);
                myFileSymbol.addChild(symbol, reader);
            }
            if (type == FieldAttrType.ATTR_WRITER || type == FieldAttrType.ATTR_ACCESSOR) {
                final SpecialSymbol writer = new SpecialSymbol(myFileSymbol, name + "=", symbol, field, Type.FIELD_WRITER);
                if (prototype != null) {
                    myFileSymbol.addPrototype(writer, prototype);
                }
                myFileSymbol.addPrototype(writer, fieldAttr);
                myFileSymbol.addChild(symbol, writer);
            }
            if (type == FieldAttrType.ATTR_INTERNAL) {
                final SpecialSymbol internal = new SpecialSymbol(myFileSymbol, name, symbol, field, Type.ATTR_INTERNAL);
                if (prototype != null) {
                    myFileSymbol.addPrototype(internal, prototype);
                }
                myFileSymbol.addPrototype(internal, fieldAttr);
                myFileSymbol.addChild(symbol, internal);
            }
            if (type == FieldAttrType.CATTR_ACCESSOR) {
                final SpecialSymbol caccessor = new SpecialSymbol(myFileSymbol, name, symbol, field, Type.CATTR_ACCESSOR);
                if (prototype != null) {
                    myFileSymbol.addPrototype(caccessor, prototype);
                }
                myFileSymbol.addPrototype(caccessor, fieldAttr);
                myFileSymbol.addChild(symbol, caccessor);
            }
        }
    }

    private void processAlias(@Nonnull final RAliasStatement alias, @Nonnull final Symbol symbol) {
        // Searching in parent!
        final Symbol original = SymbolCoreUtil.find(myFileSymbol, symbol, Arrays.asList(ReadAction.computeNotNull(alias::getOldName)), false, true, Types.ALIAS_OBJECTS);
        addPrototypeIfNeeded(original, alias);
        final Symbol parent = original.getParentSymbol();
        if (parent != null) {
            final SpecialSymbol aliasSymbol = new SpecialSymbol(myFileSymbol, ReadAction.compute(alias::getNewName), parent, original, Type.ALIAS);
            final RPsiElement prototype = original.getLastVirtualPrototype(myFileSymbol);
            if (prototype != null) {
                myFileSymbol.addPrototype(aliasSymbol, prototype);
            }
            myFileSymbol.addPrototype(aliasSymbol, alias);
            myFileSymbol.addChild(parent, aliasSymbol);
        }
    }

    private void processRequireOrLoad(@Nonnull final RRequire require, @Nonnull final Symbol symbol, @Nonnull final InterpretationMode mode) {

        for (String name : ReadAction.computeNotNull(require::getNames)) {
            for (String url : RFileUtil.findUrlsForName(myFileSymbol, name, myFile)) {
                if (mode == InterpretationMode.ONLY_TESTS_EXTERNAL) {
                    if (RubyModuleRootUtil.isUnderTestUnitRoot(symbol.getProject(), url)) {
                        myFileSymbol.process(url, mode, false);
                    }
                }
                else {
                    myFileSymbol.process(url, InterpretationMode.FULL, false);
                }
            }
        }
    }

    private void processImportJavaClass(@Nonnull final RImportJavaClass importJavaClass, @Nonnull final Symbol symbol) {
        final Symbol context = SymbolUtil.getClassModuleFileSymbol(symbol);
        LOG.assertTrue(context != null, "Context cannot be null");
        for (RVirtualName path : importJavaClass.getVirtualNames()) {
            final PsiElement clazzz = RubyInterop.compute(provider -> {
                final PsiElement element = provider.findPackageOrClass(symbol.getProject(), path.getPath());
                return element != null && provider.isClass(element) ? element : null;
            });
            if (clazzz != null) {
                myFileSymbol.addChild(context, new ProxyForeignSymbol(myFileSymbol, path.getName(), clazzz, context, importJavaClass));
            }
        }
    }

    private void processIncludeJavaClass(@Nonnull final RIncludeJavaClass includeJavaClass, @Nonnull final Symbol symbol) {
        final Symbol context = SymbolUtil.getClassModuleFileSymbol(symbol);
        LOG.assertTrue(context != null, "Context cannot be null");
        final String fullName = includeJavaClass.getQualifiedName();
        if (fullName == null) {
            return;
        }
        final PsiElement clazzz = RubyInterop.compute(provider -> {
            final PsiElement element = provider.findPackageOrClass(symbol.getProject(), fullName);
            return element != null && provider.isClass(element) ? element : null;
        });
        if (clazzz instanceof PsiNamedElement namedClass) {
            myFileSymbol.addChild(context, new ProxyForeignSymbol(myFileSymbol, namedClass.getName(), clazzz, context, includeJavaClass));
        }
    }

    private void processIncludeJavaPackage(@Nonnull final RIncludeJavaPackage includeJavaPackage, @Nonnull final Symbol symbol) {
        final Symbol context = SymbolUtil.getClassModuleFileSymbol(symbol);
        LOG.assertTrue(context != null, "Context cannot be null");
        final String fullName = includeJavaPackage.getQualifiedName();
        if (fullName == null) {
            return;
        }
        final List<PsiElement> classes = RubyInterop.compute(provider -> {
            final PsiElement element = provider.findPackageOrClass(symbol.getProject(), fullName);
            return element != null && provider.isPackage(element) ? provider.getPackageClasses(element) : null;
        });
        if (classes != null) {
            for (PsiElement clazzz : classes) {
                if (clazzz instanceof PsiNamedElement namedClass) {
                    myFileSymbol.addChild(context, new ProxyForeignSymbol(myFileSymbol, namedClass.getName(), clazzz, context, includeJavaPackage));
                }
            }
        }
    }

    private Symbol findForeignOrRuby(@Nonnull final Symbol symbol, @Nonnull final RPsiElement prototype, @Nonnull final RVirtualName path, final TypeSet typeSet) {
        final List<String> fullPath = path.getPath();
        // Try to find a foreign package or class
        if (myFileSymbol.isInteropEnabled() && !path.isGlobal() && TextUtil.isIdentifier(fullPath.get(0))) {
            final Symbol foreignSymbol = RubyInterop.findSymbol(symbol.getProject(), fullPath);
            if (foreignSymbol != null) {
                return foreignSymbol;
            }
        }
        // Searching in parent!
        final Symbol includeSymbol = SymbolCoreUtil.find(myFileSymbol, symbol, fullPath, path.isGlobal(), true, typeSet);
        addPrototypeIfNeeded(includeSymbol, prototype);
        return includeSymbol;
    }

    private void addPrototypeIfNeeded(@Nonnull final Symbol symbol, @Nonnull final RPsiElement element) {
        if (symbol.getType() == Type.NOT_DEFINED) {
            myFileSymbol.addPrototype(symbol, element);
        }
    }

    private Type getArgumentType(final ArgumentInfo.Type type) {
        if (type == ArgumentInfo.Type.SIMPLE) {
            return Type.ARG_SIMPLE;
        }
        if (type == ArgumentInfo.Type.PREDEFINED) {
            return Type.ARG_PREDEFINED;
        }
        if (type == ArgumentInfo.Type.ARRAY) {
            return Type.ARG_ARRAY;
        }
        if (type == ArgumentInfo.Type.BLOCK) {
            return Type.ARG_BLOCK;
        }
        throw new IllegalArgumentException("Wrong parameter type");
    }

}
