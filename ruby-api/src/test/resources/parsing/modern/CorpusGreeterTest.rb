# frozen_string_literal: true

require "minitest/autorun"
require_relative "../lib/simple_ruby"

class GreeterTest < Minitest::Test
  def test_greet
    assert_equal "Hello, Ruby!", SimpleRuby::Greeter.new(name: "Ruby").greet
  end

  def test_shapes
    assert_equal "rectangle 2x3", SimpleRuby::Shapes.describe({ type: :rect, width: 2, height: 3 })
    assert_equal "point at 1,2", SimpleRuby::Shapes.describe([1, 2])
  end
end
