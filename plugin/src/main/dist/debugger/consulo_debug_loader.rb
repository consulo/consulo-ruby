# Starts the debug gem's DAP server before the program runs (loaded through RUBYOPT by the IDE).
# The IDE connects to RUBY_DEBUG_HOST:RUBY_DEBUG_PORT; the program waits until it did.

module ConsuloDebugLoader
  module_function

  def require_debugger
    require "debug/open"
  rescue LoadError
    # the debug gem is not in the bundle - use the newest installed one
    spec = Gem.path.flat_map { |base| Dir[File.join(base, "specifications", "debug-*.gemspec")] }
              .filter_map { |file| Gem::Specification.load(file) }
              .select { |candidate| candidate.name == "debug" }
              .max_by(&:version)
    abort "The debug gem is not installed: gem install debug" unless spec
    $LOAD_PATH.unshift(*spec.full_require_paths)
    require "debug/open"
  end
end

# the debugger suspends after the file requiring it, which would be this one - the program waits instead
ENV["RUBY_DEBUG_INITIAL_SUSPEND_PATH"] = $0
begin
  ConsuloDebugLoader.require_debugger
ensure
  ENV.delete("RUBY_DEBUG_INITIAL_SUSPEND_PATH")
end
