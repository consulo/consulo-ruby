# Reports RSpec examples to the IDE as service messages (##teamcity[...]) for its test tree.
# Used with: rspec --require consulo_rspec_formatter.rb --format ConsuloRSpecFormatter

require_relative "consulo_test_tree"
require "rspec/core"
require "rspec/core/formatters/base_formatter"

# a suite per example group, nested like the groups; the RSpec ids keep the nodes apart
class ConsuloRSpecFormatter < RSpec::Core::Formatters::BaseFormatter
  RSpec::Core::Formatters.register self, :start, :example_group_started, :example_group_finished,
                                   :example_started, :example_passed, :example_failed, :example_pending

  def initialize(output)
    super
    @groups = []
  end

  def start(notification)
    super
    ConsuloTestReporter.message("enteredTheMatrix")
  end

  def example_group_started(notification)
    group = notification.group
    ConsuloTestReporter.message("testSuiteStarted", name: group.description, nodeId: group.id,
                                parentNodeId: @groups.last || "0", locationHint: location(group.metadata))
    @groups << group.id
  end

  def example_group_finished(notification)
    @groups.pop
    ConsuloTestReporter.message("testSuiteFinished", name: notification.group.description, nodeId: notification.group.id)
  end

  def example_started(notification)
    example = notification.example
    ConsuloTestReporter.message("testStarted", name: name(example), nodeId: example.id, parentNodeId: @groups.last || "0",
                                locationHint: location(example.metadata))
  end

  def example_passed(notification)
    finished(notification.example)
  end

  def example_failed(notification)
    example = notification.example
    exception = example.execution_result.exception
    backtrace = RSpec.configuration.backtrace_formatter.format_backtrace(exception.backtrace || [], example.metadata)
    ConsuloTestReporter.message("testFailed", name: name(example), nodeId: example.id,
                                message: exception.message.to_s.strip, details: backtrace.join("\n"))
    finished(example)
  end

  def example_pending(notification)
    example = notification.example
    ConsuloTestReporter.message("testIgnored", name: name(example), nodeId: example.id,
                                message: example.execution_result.pending_message)
    finished(example)
  end

  private

  def finished(example)
    ConsuloTestReporter.message("testFinished", name: name(example), nodeId: example.id,
                                duration: (example.execution_result.run_time.to_f * 1000).round)
  end

  # an example without a docstring is named after its expectation once it ran
  def name(example)
    description = example.description
    description.nil? || description.empty? ? example.id : description
  end

  def location(metadata)
    ConsuloTestReporter.location(metadata[:absolute_file_path] || metadata[:file_path], metadata[:line_number])
  end
end
