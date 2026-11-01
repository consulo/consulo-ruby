/**
 * The Maven integration of JRuby: loaded only when the Maven plugin is enabled.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
module consulo.jruby.maven {
    requires consulo.ruby.api;
    requires consulo.module.api;
    requires consulo.module.content.api;

    requires org.jetbrains.idea.maven;
    requires org.jetbrains.idea.maven.server.common;
}
