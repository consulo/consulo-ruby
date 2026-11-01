# frozen_string_literal: true

module SimpleRuby
  class Greeter
    attr_reader :name

    def initialize(name:, punctuation: "!")
      @name = name
      @punctuation = punctuation
    end

    def greet = "Hello, #{name}#{@punctuation}"

    private def shout(text) = text.upcase
  end
end
