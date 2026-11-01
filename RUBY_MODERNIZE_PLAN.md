# Ruby plugin modernization plan

Test projects: `../ruby-test-projects/{simple-ruby, simple-gem, simple-rails}` (Ruby 3.4.10, Rails 8.1.4,
gems in the user gem dir `~/.local/share/gem/ruby/3.4.0`).

## Current state (survey 2026-11-01)

- Lexer: JFlex `ruby-api/.../ruby/lang/lexer/ruby.flex` + hand-written state managers. Parser: hand-written
  recursive descent, one class per Ruby 1.8 yacc rule (`.../lang/parser/parsing/**`). No language level.
- Nothing past Ruby 1.8 is supported (labels, kwargs, `->`, `&.`, `%i`, `<<~`, `1r`/`2i`, endless def,
  pattern matching, `...`, `**`, `{x:}`, rescue in do-blocks, modern block params, endless ranges).
- Parser tests (`testSrc/`, 349 data files) are not in any Maven module and use `com.intellij` APIs.
- SDK: only PATH `ruby` / `/usr`; `suggestSdkName` uses `VERSION` (gone since 1.9); every gem dir of the SDK
  is a root (`GemOrderRootType`). No Gemfile / Gemfile.lock / Bundler anywhere.
- Rails: `BaseRailsFacet.getInstance` hard-returns `null`, so every Rails feature is off. Rails 1/2
  assumptions everywhere (`script/server`, `script/generate`, `vendor/plugins`, `rails <path>` without `new`).
- Run: Ruby script, Test::Unit, Rails server (`script/server`), RSpec 1.x. No minitest, no SM test tree,
  no rake configuration.
- Legacy wizard (`RRModuleBuilder`, `RubyModuleBuilder`, facet wizard steps) is unregistered and dead.

## Phase 1 - Bundler external system (in `plugin`, package `consulo.ruby.impl.bundler`) - DONE, AWT-verified

Modelled on consulo-cmake.

- `BundlerConstants.SYSTEM_ID` (`BUNDLER`), `BundlerManager` (+ auto import: `Gemfile`, `Gemfile.lock`,
  `*.gemspec`, `.ruby-version`), settings / local settings / listener topic / project settings
  (SDK name) / execution settings (ruby executable + SDK name).
- `BundlerProjectResolver`: runs a Ruby probe under `bundler` (`Bundler.definition.specs`) that prints JSON
  (gems with full require paths, ruby version, direct dependencies, rails). Falls back to parsing
  `Gemfile.lock` when gems are not installed (module imported, gems missing, `install` task offered).
  Rake tasks from `rake -AT` (best effort).
- One module per Gemfile root: content root, sources `lib` (+ `app/*` for Rails), tests `test` / `spec`,
  excluded `vendor/bundle`, `.bundle`, `tmp`, `log`, `coverage`, `storage`, `node_modules`, `public/assets`.
- Gems: project libraries `Gem: name (version)` with the require paths as binary + source roots.
- `BundlerModuleData` key + data service: enables the `ruby` extension with the SDK and, when `railties` is
  locked, the `ruby-on-rails` extension (by id; null when the Rails plugin is absent).
- Task manager: `bundle install`, `bundle update`, rake tasks via `bundle exec rake`.
- Import provider, settings configurable factory, tool window, task configuration type, icon provider,
  system-only module extension.

Notes from the sandbox run (2026-11-01):
- Gem executables run as `ruby -e "load Gem.bin_path(gem, exe)" -- args` and rake as
  `ruby -rbundler/setup -e "load Gem.bin_path('rake','rake')" -- task` - the user gem bin dir is often not on the
  IDE PATH, so `ruby -S bundle` / `bundle exec` failed.
- Services read inside the import's write action are created before it (`BundlerLocalSettings` in the linker).
- Platform warns "Getting service from write-action" for `ProjectLibraryTableImpl` in `LibraryDependencyDataService`
  (platform, not Bundler).

Later: stop adding all SDK gem dirs as roots when a module is Bundler-managed.

## Phase 2 - New project/module builders - DONE, AWT-verified (all 3 templates)

Group `Ruby`: **Ruby** (Gemfile + `main.rb` + `lib/` + `test/`), **Ruby Gem** (`bundle gem`),
**Ruby on Rails** (`rails new`, Rails plugin). Each writes/generates the skeleton then links the Bundler
project. Delete the dead legacy wizard. Fix `RailsUtil.generateRailsApp` (`rails new`).

- `process()` schedules generation with `invokeLater`: `StartupManager.runAfterOpened` lost the activity when the
  wizard created the module while the new project was still in post-startup.
- Gemfile / Rakefile / gemspec / config.ru / Guardfile ... are now Ruby files (`RubyFileType.RUBY_FILE_NAMES`).

## Phase 3 - Ruby 3.x language - core DONE (uncommitted)

Status 2026-11-01:
- Tests: `ruby-api/src/test` - `RubyModernSyntaxParsingTest` (28 fixtures in `parsing/modern`, each must parse
  without `PsiErrorElement`) and `RubyCorpusParsingTest` (`-DargLine=-Druby.corpus=<dir>`, report in
  `target/ruby-corpus-report.txt`). Rails 8.1 gems + Ruby 3.4 stdlib: 2069 files, 25 with errors (was 122 after
  labels, every modern file failed before).
- Test classpath: `consulo-test-junit-impl` must be the FIRST dependency (its `PluginManagerInternal` wins the
  ServiceLoader lookup), and surefire excludes the impl modules / plugin impl jars whose services clash with the
  light application. Fixture names are the CamelCase test names (`testStabbyLambda` -> `StabbyLambda.rb`).
- Lexer: `tLABEL` (label allowed by lex state, never after `?`), `->`, `&.`, `**` (`tDSTAR`), `%i`/`%I`/`%s`,
  `<<~`, `1r`/`2i`/`1ri`, `0o17`, leading-dot method chains, `!=`/`!~`/`!` as operator method names.
- Parser: label hashes + `{x:}`, `"str": v`, keyword/`**`/`...`/anonymous `*`/`**`/`&` params and args, post-rest
  params, endless def, stabby lambda, `.()`, safe navigation, modern block params, rescue/else/ensure in do-blocks,
  beginless/endless ranges, `case/in` + `=>`/`in` patterns (`Pattern`), trailing commas, splats anywhere in arrays.
- New element types (generic PSI): `KEYWORD_ARGUMENT`, `KEYWORD_REST_ARGUMENT`, `FORWARD_ARGUMENT`, `LAMBDA`,
  `PATTERN`, `PATTERN_GUARD`, `PATTERN_MATCH`, `IN_CASE`. `**x` args and `...` reuse `ARRAY_TO_ARGS`; `%i` reuses
  the word-array tokens.

Corpus leftovers fixed (fixtures `MlhsSplatFirst`, `YieldParenArithmetic`, `KeywordMethodNames`,
`DestructuringParams`, `RescueSplat`, `CommandCallAssignment`): one splat anywhere in an mlhs; `yield(`/`super(` without a
space are primaries, not commands; `true/false/self/nil/super/__FILE__/__LINE__` after `.`/`&.`/`::` are method names
(`TokensManager.isMethodNameAfterDot`); `def f((a, b), *c)` destructuring params; prefix `*`/`&` put the lexer in BEG so
`*(…)` is a parenthesised expression. The corpus has not been re-run since (needs `-DargLine=-Druby.corpus=<dir>`).

Original order:

Order (each step: lexer tokens -> parser -> PSI where needed -> test data from `simple-ruby`):

1. Revive parser tests as a test module (port `testSrc` to current APIs).
2. Lexer: `tLABEL` (`a:` / `"a":`), `->`, `&.`, `**`, `%i`/`%I`/`%s`, `<<~`, `1r`/`2i`/`1ri`,
   `__dir__`/`__method__`/`__ENCODING__`, fix `%w`/`%W` swap.
3. Parser: label hashes + shorthand `{x:}`, keyword/`**`/`&` params and args, anonymous `*`/`**`/`&`, `...`,
   stabby lambda, safe navigation, endless def, block params (defaults, kwargs, `&b`, `|a; b|`),
   `rescue`/`ensure` in do-blocks, beginless/endless ranges, `case/in` pattern matching, `=>`/`in` one-line.
4. Semantics: `_1`/`it` implicit params, frozen_string_literal magic comment, `private def`.

## Phase 4 - Tooling

SDK detection DONE (uncommitted): `RubySdkType` is a `PlatformAwareSdkType`. `RubyHomeLocator` scans PATH, /usr,
/usr/local, Homebrew, MacPorts, rbenv (`RBENV_ROOT`), rvm, asdf (`ASDF_DATA_DIR`), mise (`MISE_DATA_DIR`), chruby,
frum, RubyInstaller dirs; version = `RUBY_VERSION` (JRuby `JRUBY_VERSION`); name `Ruby SDK 3.4.10 (rbenv)` with the
manager read from the directory layout. Active rubies come through the platform env mechanism
(`getEnvironmentVariables`: `RUBY_ROOT`, `MY_RUBY_HOME`; JRuby `JRUBY_HOME`) - SDK named after the variable, its home
is skipped by the scan. No more Swing error dialogs while probing.

Rails EDT error DONE: `RailsHighlightPassFactory` removes stale markers on the UI thread.

Rails detection DONE: `RailsModuleSupport` - Rails support = enabled `ruby-on-rails` extension (Bundler import enables it
for `railties`), app root = content root with `config/application.rb`; `RailsFacetUtil` no longer asks the dead facet.
The plugin sandbox loads `consulo-ruby-on-rails` too.

Stubs DONE: Rails stubs deleted (the Bundler gems are the Rails sources; `RAILS_BUILT_IN` reuses the core stubs plus
the gem loading of `RailsBuiltInCachedSymbol`). Core stubs are generated per SDK from the interpreter's RBS signatures
(`ruby-stubs/generate_stubs.rb`, `RubyStubsGenerator`, system dir `ruby-stubs/<id>/rubystubs`, 336 files for 3.4);
the bundled 1.8 stubs stay only as the fallback (JRuby, Ruby < 3.0). Later: real RBS support.

Icons DONE: expui light + dark from intellij-icons (RubyIcons/icons/expui); legacy-only slots use platform expui icons.

Tests DONE (AWT-sandbox verified on simple-rails and simple-rspec):
- Reporters in `plugin/src/main/dist/testing/`: `consulo_test_tree.rb` (service messages), `consulo_test_reporter.rb`
  (minitest 5/6 incl. Rails, test-unit; drops `Minitest::ProgressReporter` and `Rails::TestUnitReporter` dots; `-n` -> `-i`
  on minitest 6; suite location = `Object.const_source_location`), `consulo_rspec_formatter.rb` (RSpec 3, nested groups,
  `rspec-core` via `Gem.bin_path`, CLI `--format` overrides `.rspec`).
- Producers on the new `RunConfigurationProducer` API (the gutter `ExecutorAction` only asks that EP; the legacy
  `RuntimeConfigurationProducer`s are deleted): `RubyScriptRunConfigurationProducer`, `RTestsRunConfigurationProducer`
  (shared analysis `RubyRunConfigurationType.findTarget` -> `RubyRunTarget`), `RSpecRunConfigurationProducer`
  (folder / file / example or group by `path:line`). Rails `test "..."` blocks run as `test_...` methods.
- Gutter: `RTestRunLineMarkerContributor` (test case classes, `test_*` methods, `test "..."`),
  `RSpecRunLineMarkerContributor` (describe/context/it...), both show the last result (`file://path:line` state keys).
- The tests factory has its own id (`RubyTests`) - both Ruby factories used to share the type id.
- RSpec 1 options gone (custom runner, colored output, run separately); `exampleLine` option + "Example or group" target.
- Folder targets by content (`*_spec.rb` vs `*_test.rb`/`test_*.rb`), not by the legacy facet settings.
- Tests run in the content root (Rails needs it), not the test file's folder.
- Parser: a do block after command arguments without parentheses binds to the command (`RBuilder.setDoBlockAllowed`,
  test `RubyBlockBindingParsingTest`). `RSpec.describe X do` used to bind the block to `X`.

Debugger DONE (AWT-sandbox verified: script, RSpec example with test tree, breakpoints, frames, variables, evaluate,
stepping): `consulo.ruby.impl.debugger` on consulo-dap (`<depends>consulo.dap</depends>`).
- `RubyDebugRunner` (Debug executor, any `AbstractRubyRunConfiguration`, not JRuby) adds `RUBYOPT=-r<plugin>/debugger/
  consulo_debug_loader.rb` + `RUBY_DEBUG_HOST/PORT` + `RUBY_DEBUG_LOG_LEVEL=ERROR` to the state
  (`ColouredCommandLineState.addRequiredLibrary/addEnvironment`) and runs the state's own `execute` - test runs keep
  their SM tree. `RubyDebugProcess` = `DAPDebugProcess` over a socket; the base `launch` sets the gem's nonstop mode.
- The loader requires `debug/open`, falling back to the newest installed debug gem when the bundle lacks it, and sets
  `RUBY_DEBUG_INITIAL_SUSPEND_PATH=$0` (the gem suspends after the file requiring it - the loader - otherwise).
- `RUBY_DEBUG_DAP_SHOW_PROTOCOL=1` in the IDE's environment dumps the protocol into the debug console.
- Debug gem quirk: Step Over out of an endless `def` in another file runs to the end (gem behaviour, same with rdbg).
- `RubyLineBreakpointType` (+ resolver), `RubyDebuggerEditorsProvider` (Ruby fragments for evaluate/conditions).
- `RubyStructureViewModel` gets the editor (looked one up from focus off the UI thread before).

RDoc DONE: the RDoc tool window, `ri` search action and panes are deleted; Quick Documentation shows the RBS docs of the
generated stubs (generator v2: rdoc headers become call-seq lines, module functions documented on both defs). Existing
SDKs pick the new stubs up when their paths are set up again.

Rails server DONE (sandbox: run + debug with a breakpoint hit by a request): `bin/rails server [-u server] -p -b -e`,
binds 127.0.0.1 by default, any host/IPv6 accepted, validation = SDK + Rails module (not Rails gems in the SDK).
The Bundler import adds "Rails server: <module>" (`RailsUtil.ensureRailsServerConfiguration`) and selects it when
nothing is selected; the factory is applicable only with a Rails module.

Rake DONE: gutter icons on `task :name` / `task name: deps` / `task "name"` in Rakefiles and `*.rake` (namespaces give
`db:seed`), run through the Bundler task configuration (`RakeTaskRunConfigurationProducer`). Rakefiles are no longer
offered as Ruby scripts.

Rails generators DONE (sandbox: model generated, files refreshed): `consulo.ruby.impl.rails.RailsGenerateAction`
("Rails Generator…" in New and Tools) reads `bin/rails generate --help` (`RailsGenerators.parse`), asks for the
generator and its arguments (uni `InputBoxBuilder`), runs `bin/rails generate` in a Run console and refreshes the app.
The Rails 2 menus (facet-loaded generators, old rake group, Run Rails script) are unregistered.

Legacy cleanup DONE (sandbox start clean): deleted `rails/actions/{generators,rake,execution,shortcuts}`, the RSpec
generator names, `RProjectSettings`/`RProjectUtil` (generator options only), `BaseRailsFacet` + builder, the facet wizard
and settings UI (`rails/facet/ui/**`, RSpec wizard tab, `RailsApplicationSettings`, `RailsWizardSettingsHolderImpl`),
the JRuby facet UI/provider/structure, the module root panels (`ruby/module/ui/**`) and the `com.intellij.facet.ui`
shims. The facet configuration keeps only what `RailsModuleSupport` uses. `JRubyFacet` stays (library-name helper of
`JRubySdkTableListener`, `ModuleLayer`).

Rails in the project view DONE: `RailsProjectViewNodeDecorator` gives the application root and app/controllers,
app/helpers, app/models, db/migrate their Rails icons in the regular Project view. A separate Rails pane is not
possible from a plugin: panes register through `AbstractProjectViewPane.EP_NAME` in `consulo.ide.impl` only - it
needs a pane extension point in project-ui-view-api first.

Legacy parser data DONE: the 346 parser data files of `testSrc` are `ruby-api/src/test/resources/parsing/legacy/**`, run by
`RubyLegacyParsingTest` (one dynamic test per file: source, dashes, expected tree; a mismatch writes today's tree to
`target/legacy-parsing-actual/`). 296 matched the 2008 trees unchanged; the 50 others were reviewed and updated: stub
element names (`Ruby:METHOD` for `Method`, ...), modern syntax that is valid now (`def f(a = 1, b)`, `foo(a,)`), trailing
newline whitespace bound at file level, and error recovery of invalid input (`{a=>b c=>d}` recovers as a pattern match).
Found and fixed by it: `for x in` wrapped the variable in `Argument` (block variables accept `in` as their end).
The rest of `testSrc` (800 files: ERB lexer/parser 164, editor actions 110, symbols, resolve, formatter, ...) needs
editor/resolve fixtures; the ERB lexer/parser data can be ported the same way.

JRuby detection DONE (sandbox: Maven project with `org.jruby:jruby-complete` gets the `jruby` extension, Ruby code
navigates to the jar's Java classes): JRuby is enabled from the build's dependencies on `org.jruby:{jruby,
jruby-complete,jruby-core,jruby-base}` (`JRubyUtil.ARTIFACT_IDS`, `isJRubyLibraryName`).
- Maven: `consulo-jruby-maven` (jar, `consulo.jruby.maven`, `META-INF/plugin-requires.xml` on org.jetbrains.idea.maven,
  `<depends optional="true">` in consulo-jruby) with `JRubyMavenImporter`.
- Gradle and any other external system: `JRubyLibraryDependencyDataService` on `ProjectKeys.LIBRARY_DEPENDENCY` in
  consulo-jruby (not sandbox-tested - the sandbox has no Gradle plugin).
- Found on the way: Ruby completion threw NPE in `RPsiFileBase.getContainingFileUrl` (completion copies have no
  virtual file) - now the original file's, else the view provider's.

JRuby move to consulo-jruby (user: "Move to consulo-jruby") - JRuby only from a Maven/Gradle dependency, no JRuby SDK.
- Stage 1 DONE (sandbox: jruby-maven `hello.rb` prints via `java -cp ... org.jruby.Main`, Debug greyed out; CRuby
  RSpec run + debug with breakpoint unchanged): `consulo.ruby.runtime.{RubyRuntime, RubyRuntimeProvider (EP),
  SdkRubyRuntime(+Provider, order last), BundledRubyStubs}` in ruby-api; run configs build the command line from
  `getRuntime()` (alternative SDK or the module's provider); `RubyDebugRunner` asks `isDebuggable()`; built-in stubs
  fall back to the bundled ones when there is no Ruby SDK. consulo-jruby `JRubyRuntimeProvider`: module JDK
  (`JavaModuleExtension` + `JavaSdkType.getBinPath`), runtime classpath of the module, stdlib load path =
  `META-INF/jruby.home/lib/ruby/stdlib` of the classpath jars. Deleted `JRubySdkType`, `JRubySdkUtil`,
  `JRubySdkTableListener`, `JRubyFacet`, ruby-api `JRubyModuleExtension` (now SDK-less in consulo-jruby),
  `OwnJavaParameters`/CLASSPATH plumbing in `Runner`/`OSUtil`; `getModuleOrJRubyFacetSdk` -> `getModuleSdk`.
  Also fixed: completion copies made `FileSymbolUtil.getFileSymbol` assert (uses the original file now).
- Stages 2-4 DONE (compiled; NOT sandbox-tested - the `workspace` goal 404s on api.consulo.io for platform 4624):
  ruby-api has no Java code or `consulo.java` dependency left (module-info, pom, core plugin.xml `<depends>`).
  Symbol types `FOREIGN_{CLASS,PROXY_CLASS,PACKAGE,METHOD,FIELD}`, `ForeignSymbol`/`ProxyForeignSymbol`,
  `ForeignLookupItem`, `ForeignClassReference`, `ForeignImportPresentationUtil`, `Types.FOREIGN`.
  EP `consulo.ruby.interop.RubyInteropProvider` (+ `RubyInterop` lookups, `ForeignReference` marker) covers
  symbol lookup, imports (`java_import`/`include_class`/`include_package`), children, constructors, the `Java`
  namespace type, completion roots, presentation, documentation, override/implement gutter, parameter info,
  string references, annotator checks. consulo-jruby: `JRubyInteropProvider` + moved `JavaResolveUtil`,
  `JavaReference(sBuilder)`, `JRubyDuckTypeUtil`, `JRubyExtentionsUtil`, `JRubyTypeConventions`, `JavaPsiUtil`,
  `JavaNamespaceType` (was `JavaModuleType`: the RType of the `Java` constant, not a module type). Deleted
  `RJavaType(Impl)`, `JRubyIcons`, `JRubyComponents`.
- Flag + settings DONE (compiled): `isJRubyEnabled`/`jrubyEnabled` -> `isInteropEnabled`/`interopEnabled`, computed by
  `RubyInteropProvider.isEnabled(Module)`; `JRubyUtil` moved to consulo-jruby (consulo-jruby-maven keeps its own
  constants - it cannot depend on the plugin artifact); `jruby.facet` -> `settings.RSupportPerModuleSettings{Impl,
  Externalizer}` (stored id `JRUBY_FACET_CONFIG_ID` kept).
- Old in-IDE Ruby scripts (consulo-jruby `dist/rb`, run through an embedded JRuby, loaders commented out = dead) ported
  to Java and deleted with their Java bridges, `rb.implement.ImplementHandler`, `testSrc/rb`, `ExternalRailsSettings`:
  - Override / Implement Methods: `consulo.ruby.impl.generation` (`RubyOverrideMethodHandler`,
    `RubyImplementMethodHandler`, `RubyMethodGenerator` text insert + reformat); member selection in ruby-api
    `RubyOverrideImplementUtil.getMethodsToOverride/getMethodsToImplement` (skips Class/Module meta methods). AWT
    sandbox verified (override, caret on body comment, gutter markers).
  - Introduce Variable: `consulo.ruby.impl.refactoring` support provider + uni `DialogDescriptor` (Swing .form
    dialog deleted). Sandbox verified.
  - Intentions: `StringToSymbolIntention` (verified), `AppendCurrentDirToPath`/`RelativePathToAbsolutePath` now
    registered; consulo-jruby `QualifiedNameToIncludeClass/ImportClass`, `JavaFullClassName` (not verified).
  - Rails param defs: `ParamDefProvider` EP + `consulo.ruby.impl.rails.paramDef` (render/redirect_to/url_for/link_to/
    form_tag, before_action & co, layout, helper, asset tags, associations) - Rails 7 and old FQNs; roots come from
    `StandardRailsPaths`; assets search `app/assets/*`, `app/javascript`, `app/assets/builds`, then `public/*`.
    Verified (simple-rails, Rails 8.1): `render partial: "f"` completes `posts/form` and Ctrl+B opens `_form.html.erb`;
    `has_many :co` completes `comments`. Fixes this needed: `RAssoc` for `key: value` labels (`getKey()` returned the
    value, new `getKeyName()`), `ParamContext.getModule()` through the original file (completion copy had no
    module), and an unresolved-call fallback by method name in Rails modules (`ParamDefManager.getParamDefsByMethodName`)
    because Rails 8 gem sources do not resolve (Zeitwerk, no requires) - real Rails symbol resolution is Phase 5.
  - Inline handler was an empty stub - not ported. `dist/jrubystubs` kept (not loaded, see stage 1 note).
- JRuby symbol fixes found on the way (AWT sandbox verified on jruby-maven: Implement Methods offers
  `java.lang.Runnable#run` and generates it with the implement marker; Import Java class intention works):
  - `java_import` recognised (only the old `import` alias was).
  - `RCallBase` implements `RImportJavaClass` / `RIncludeJavaPackage` - their virtual-cache impls were deleted in
    ff9ef77, so the symbol builder could not process any Java import; dispatch on these now goes by
    `StructureType`, not `instanceof` (every call is an instance of them).
  - include of a constant falls back to the Ruby lookup when the foreign lookup misses (java_import'ed names).
  - SDK-less modules (JRuby) get the module layer + bundled core stubs (they had no built-ins at all).
  - Sandbox gotcha: a hand-written `sdk.table.xml` JDK without class roots makes every `java.*` lookup fail; the
    plugin sandbox now has the full temurin-21 roots.
- ERB highlighting fixed (was plain text): `ERubySyntaxHighlighterFactory` registers the base layer (the old
  `eRubyLanguage.getSyntaxHighlighter` hook is never called); `eRubyTokenTypes` now aliases the lexer's
  `RHTMLTokenTypeEx` tokens, so the HTML template data of `.erb` files is no longer empty.
- Left in core (Java-free, JRuby-flavoured names): `RImportJavaClass`/`RIncludeJavaClass`/`RIncludeJavaPackage`
  PSI (Ruby call shapes), `FileSymbol.isJRubyEnabled`, `JRubyUtil`, `jruby.facet` (load-path settings), the
  JRuby icon in `RubyApiIconGroup`.

## Phase 5 - Rails 7/8 + cleanup

Rails detection on the `ruby-on-rails` extension (replace `BaseRailsFacet`), Zeitwerk layout, `bin/rails`
generators, routes. Remove facet shims, `consulo.ide.impl` usages, Swing forms -> uni UI, RBundle -> localize.
