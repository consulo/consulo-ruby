package consulo.ruby.impl.refactoring.introduceVariable;

import consulo.ui.ex.dialog.DialogValue;

/**
 * The variable name and scope chosen in the Introduce Variable dialog.
 */
public record RubyIntroduceVariableResult(String name, boolean replaceAll) implements DialogValue {
}
