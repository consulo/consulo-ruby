/**
 * @author VISTALL
 * @since 2026-05-15
 */
open module consulo.ruby.impl
{
    requires consulo.ruby.api;

    requires consulo.external.system.api;
    requires consulo.module.creation.api;
    requires consulo.module.ui.api;
    requires consulo.file.template.api;
    requires consulo.localize.api;
    requires consulo.project.api;
    requires consulo.project.ui.api;
    requires consulo.project.ui.view.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.file.editor.api;
    requires consulo.code.editor.api;
    requires consulo.language.api;
    requires consulo.language.editor.api;
    requires consulo.language.editor.refactoring.api;
    requires consulo.execution.api;
    requires consulo.execution.debug.api;
    requires consulo.execution.debugger.dap;

    requires com.google.gson;

    exports consulo.ruby.impl.template;
    exports consulo.ruby.impl.generation;
    exports consulo.ruby.impl.rails.paramDef;
    exports consulo.ruby.impl.refactoring;
    exports consulo.ruby.impl.refactoring.introduceVariable;
    exports consulo.ruby.impl.bundler;
    exports consulo.ruby.impl.bundler.setting;
}
