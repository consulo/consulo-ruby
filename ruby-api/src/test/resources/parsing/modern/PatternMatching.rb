case value
in Integer => n if n > 0
  :positive
in { type: :circle, radius: Numeric => r }
  r
in { type: :rect, width:, height: }
  width * height
in [x, y]
  x + y
in [Integer, *rest]
  rest
in String | Symbol
  :text
in ^expected
  :same
in nil
  :none
else
  :other
end

config => { db: { user: } }
42 => answer
result = value in Integer
