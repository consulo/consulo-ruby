package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols;

import jakarta.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolFilter;

public class Type
{
	private static int ourLastId = 0;
	private final TypeSet mySet;

	private String myDebugName;
	private int myId;
	private final SymbolFilter myFilter = new SymbolFilter()
	{
		@Override
		public boolean accept(@Nonnull final Symbol symbol)
		{
			return symbol.getType() == Type.this;
		}
	};

	public Type(@NonNls String debugName)
	{
		myDebugName = debugName;
		myId = getNextId();
		mySet = new TypeSet(this);
	}

	private static int getNextId()
	{
		assert ourLastId < 63 : "Too many symbol types";
		return ourLastId++;
	}

	public TypeSet asSet()
	{
		return mySet;
	}

	public SymbolFilter getFilter()
	{
		return myFilter;
	}

	public static final Type NOT_DEFINED = new Type("NOT_DEFINED");

	public static final Type FILE = new Type("FILE");
	public static final Type MODULE = new Type("MODULE");
	public static final Type CLASS = new Type("CLASS");
	public static final Type FOREIGN_PROXY_CLASS = new Type("FOREIGN_PROXY_CLASS");
	public static final Type SUPERCLASS = new Type("SUPERCLASS");

	public static final Type CLASS_FIELD = new Type("CLASS_FIELD");
	public static final Type INSTANCE_FIELD = new Type("INSTANCE_FIELD");
	public static final Type CLASS_INSTANCE_FIELD = new Type("CLASS_INSTANCE_FIELD");
	public static final Type CONSTANT = new Type("CONSTANT");
	public static final Type GLOBAL_VARIABLE = new Type("GLOBAL_VARIABLE");

	public static final Type CLASS_METHOD = new Type("CLASS_METHOD");
	public static final Type INSTANCE_METHOD = new Type("INSTANCE_METHOD");

	public static final Type ALIAS = new Type("ALIAS");

	public static final Type ARG_SIMPLE = new Type("ARG_SIMPLE");
	public static final Type ARG_PREDEFINED = new Type("ARG_PREDEFINED");
	public static final Type ARG_ARRAY = new Type("ARG_ARRAY");
	public static final Type ARG_BLOCK = new Type("ARG_BLOCK");

	public static final Type INCLUDE = new Type("INCLUDE");
	public static final Type EXTEND = new Type("EXTEND");

	public static final Type FIELD_READER = new Type("FIELD_READER");
	public static final Type FIELD_WRITER = new Type("FIELD_WRITER");
	public static final Type ATTRIBUTE = new Type("ATTRIBUTE");

	// attributes for Rails
	public static final Type ATTR_INTERNAL = new Type("ATTR_INTERNAL");
	public static final Type CATTR_ACCESSOR = new Type("CATTR_ACCESSOR");
	public static final Type FIELD_INTERNAL = new Type("FIELD_INTERNAL");

	public static final Type LOCAL_VARIABLE = new Type("LOCAL_VARIABLE");

	public static final Type CALL_ACCESS = new Type("CALL_ACCESS");
	public static final Type CONSTANT_ACCESS = new Type("CONSTANT_ACCESS");
	public static final Type FIELD_WRITE_ACCESS = new Type("FIELD_WRITE_ACCESS");

	// symbols of another language, e.g. Java classes in JRuby
	public static final Type FOREIGN_METHOD = new Type("FOREIGN_METHOD");
	public static final Type FOREIGN_CLASS = new Type("FOREIGN_CLASS");
	public static final Type FOREIGN_PACKAGE = new Type("FOREIGN_PACKAGE");
	public static final Type FOREIGN_FIELD = new Type("FOREIGN_FIELD");

	@NonNls
	public String toString()
	{
		return myDebugName;
	}

	public int getId()
	{
		return myId;
	}
}
