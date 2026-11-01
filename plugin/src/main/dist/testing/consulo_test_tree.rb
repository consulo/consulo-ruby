# Writes the IDE test tree as service messages (##teamcity[...]), shared by the minitest, test-unit and RSpec reporters.

module ConsuloTestReporter
  ESCAPES = { "|" => "||", "'" => "|'", "\n" => "|n", "\r" => "|r", "[" => "|[", "]" => "|]" }.freeze

  module_function

  def message(name, attributes = {})
    text = attributes.map { |key, value| "#{key}='#{value.to_s.gsub(/[|'\n\r\[\]]/, ESCAPES)}'" }.join(" ")
    $stdout.puts("##teamcity[#{name} #{text}]")
    $stdout.flush
  end

  def location(file, line)
    file ? "file://#{File.expand_path(file)}:#{line}" : nil
  end

  def class_location(name)
    Object.const_source_location(name) if name && Object.respond_to?(:const_source_location)
  rescue NameError
    nil
  end

  # a suite per test class, a test per method - ids so parallel runs still build the right tree
  class Tree
    def initialize
      @suites = {}
      ConsuloTestReporter.message("enteredTheMatrix")
    end

    def suite_id(name, file = nil, line = nil)
      @suites[name] ||= begin
        file, line = ConsuloTestReporter.class_location(name) || [file, line]
        ConsuloTestReporter.message("testSuiteStarted", name: name, nodeId: name, parentNodeId: "0",
                                    locationHint: ConsuloTestReporter.location(file, line))
        name
      end
    end

    def started(suite, name, file, line)
      ConsuloTestReporter.message("testStarted", name: name, nodeId: "#{suite}##{name}", parentNodeId: suite_id(suite, file, line),
                                  locationHint: ConsuloTestReporter.location(file, line))
    end

    def failed(suite, name, message, details)
      ConsuloTestReporter.message("testFailed", name: name, nodeId: "#{suite}##{name}", message: message, details: details)
    end

    def ignored(suite, name, message)
      ConsuloTestReporter.message("testIgnored", name: name, nodeId: "#{suite}##{name}", message: message)
    end

    def finished(suite, name, seconds)
      ConsuloTestReporter.message("testFinished", name: name, nodeId: "#{suite}##{name}", duration: (seconds.to_f * 1000).round)
    end

    def done
      @suites.each_key { |name| ConsuloTestReporter.message("testSuiteFinished", name: name, nodeId: name) }
      @suites.clear
    end
  end
end
