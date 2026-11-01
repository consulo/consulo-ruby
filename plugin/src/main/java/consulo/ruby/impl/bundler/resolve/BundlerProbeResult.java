package consulo.ruby.impl.bundler.resolve;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * What the Ruby probe reports about a Gemfile.
 *
 * @param missingMessage set when some locked gems are not installed
 * @param errorMessage   set when Bundler cannot read the Gemfile at all
 * @author VISTALL
 * @since 2026-11-01
 */
public record BundlerProbeResult(@Nullable String rubyVersion,
                                 List<String> dependencies,
                                 List<BundlerGem> gems,
                                 @Nullable String missingMessage,
                                 @Nullable String errorMessage) {
}
