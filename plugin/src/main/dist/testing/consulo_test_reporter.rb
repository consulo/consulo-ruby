# Reports test results to the IDE as service messages (##teamcity[...]) for its test tree.
# Loaded with -r before the tests: hooks minitest (5 and 6, Rails tests included) and test-unit.

require_relative "consulo_test_tree"

begin
  require "minitest"
rescue LoadError
  # not a minitest project
end

if defined?(Minitest::AbstractReporter)
  module ConsuloTestReporter
    # Rails prints its progress dots from Rails::TestUnitReporter instead of Minitest::ProgressReporter
    def self.progress_reporter?(reporter)
      reporter.is_a?(Minitest::ProgressReporter) || (defined?(Rails::TestUnitReporter) && reporter.is_a?(Rails::TestUnitReporter))
    end

    class MinitestReporter < Minitest::AbstractReporter
      def initialize(composite)
        super()
        @composite = composite
      end

      def start
        # the progress dots would share a line with the service messages, which must start one
        @composite.reporters.reject! { |reporter| ConsuloTestReporter.progress_reporter?(reporter) }
        @tree = Tree.new
      end

      def prerecord(klass, name)
        file, line = (klass.instance_method(name).source_location rescue nil)
        @tree.started(klass.name, name, file, line)
      end

      def record(result)
        suite = result.respond_to?(:klass) ? result.klass : result.class.name
        if result.skipped?
          @tree.ignored(suite, result.name, result.failure.message)
        elsif !result.passed?
          failure = result.failure
          details = failure.respond_to?(:backtrace) && failure.backtrace ? failure.backtrace.join("\n") : ""
          @tree.failed(suite, result.name, failure.message, details)
        end
        @tree.finished(suite, result.name, result.time)
      end

      def report
        @tree.done
      end

      def passed?
        true
      end
    end
  end

  module Minitest
    def self.plugin_consulo_init(options)
      reporter << ConsuloTestReporter::MinitestReporter.new(reporter)
    end
  end

  # minitest 6 loads no plugin by itself and selects tests with -i instead of -n
  if Minitest.respond_to?(:register_plugin)
    Minitest.register_plugin(:consulo)
    ARGV.map! { |argument| argument == "-n" ? "-i" : argument } if Gem::Version.new(Minitest::VERSION) >= Gem::Version.new("6")
  else
    Minitest.extensions << "consulo" unless Minitest.extensions.include?("consulo")
  end
end

begin
  require "test/unit"
  require "test/unit/ui/testrunner"
  require "test/unit/ui/testrunnermediator"
rescue LoadError
  # not a test-unit project
end

if defined?(Test::Unit::UI::TestRunner) && defined?(Test::Unit::AutoRunner)
  module ConsuloTestReporter
    class TestUnitRunner < Test::Unit::UI::TestRunner
      private

      def attach_to_mediator
        @tree = Tree.new
        @mediator.add_listener(Test::Unit::TestCase::STARTED_OBJECT) { |test| started(test) }
        @mediator.add_listener(Test::Unit::TestResult::FAULT) { |fault| fault(fault) }
        @mediator.add_listener(Test::Unit::TestCase::FINISHED_OBJECT) { |test| finished(test) }
        @mediator.add_listener(Test::Unit::UI::TestRunnerMediator::FINISHED) { |_| @tree.done }
      end

      def started(test)
        @current = test
        @started_at = Time.now
        file, line = (test.class.instance_method(test.method_name).source_location rescue nil)
        @tree.started(test.class.name, test.method_name, file, line)
      end

      def fault(fault)
        # the fault belongs to the running test
        name = @current.method_name
        suite = @current.class.name
        if fault.is_a?(Test::Unit::Omission) || fault.is_a?(Test::Unit::Pending)
          @tree.ignored(suite, name, fault.message)
        else
          @tree.failed(suite, name, fault.message, Array(fault.location).join("\n"))
        end
      end

      def finished(test)
        @tree.finished(test.class.name, test.method_name, Time.now - (@started_at || Time.now))
      end
    end
  end

  Test::Unit::AutoRunner.register_runner(:consulo) { |_| ConsuloTestReporter::TestUnitRunner }
  Test::Unit::AutoRunner.default_runner = :consulo
end
