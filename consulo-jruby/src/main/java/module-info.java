/**
 * @author VISTALL
 * @since 2026-05-15
 */
open module consulo.jruby
{
    requires consulo.ruby.api;

    requires consulo.application.api;
    requires consulo.application.content.api;
    requires consulo.code.editor.api;
    requires consulo.component.api;
    requires consulo.disposer.api;
    requires consulo.document.api;
    requires consulo.external.system.api;
    requires consulo.file.editor.api;
    requires consulo.language.api;
    requires consulo.language.editor.api;
    requires consulo.localize.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.module.ui.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.util.collection;
    requires consulo.util.lang;
    requires consulo.virtual.file.system.api;

    requires consulo.java.language.api;
    requires consulo.java.indexing.api;
    requires consulo.java.language.impl;
    requires consulo.java;

    exports consulo.jruby.impl.module.extension;
    exports org.jetbrains.plugins.ruby.jruby;
    exports consulo.jruby.impl.runtime;
    exports consulo.jruby.impl.interop;
    exports consulo.jruby.impl.intention;
    exports org.jetbrains.plugins.ruby.jruby.codeInsight.resolve;
    exports org.jetbrains.plugins.ruby.jruby.codeInsight.types;
    exports consulo.jruby.lang.searcher;
    exports org.jetbrains.plugins.ruby.jruby.impl.codeInsight.types;
    exports org.jetbrains.plugins.ruby.jruby.codeInsight.usages;
    exports org.jetbrains.plugins.ruby.jruby.codeInsight.usages.impl;
    exports org.jetbrains.plugins.ruby.jruby.inspections;
}
