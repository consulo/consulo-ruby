# frozen_string_literal: true

module SimpleRuby
  module Shapes
    module_function

    def describe(shape)
      case shape
      in { type: :circle, radius: Numeric => r }
        "circle with area #{(Math::PI * r**2).round(2)}"
      in { type: :rect, width:, height: }
        "rectangle #{width}x#{height}"
      in [x, y]
        "point at #{x},#{y}"
      else
        "unknown"
      end
    end
  end
end
