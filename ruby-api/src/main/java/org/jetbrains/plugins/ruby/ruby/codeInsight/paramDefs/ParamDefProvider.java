package org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import jakarta.annotation.Nonnull;

/**
 * Registers the parameter definitions of library methods: what the arguments of e.g. {@code render partial: ...} refer to.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface ParamDefProvider {
    void registerParamDefs(@Nonnull ParamDefManager manager);
}
