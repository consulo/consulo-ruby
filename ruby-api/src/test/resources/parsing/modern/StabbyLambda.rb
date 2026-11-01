l = ->(x) { x * 2 }
l2 = -> { 1 }
l3 = ->(x, y = 1) do
  x + y
end
l4 = -> x { x }
l.(1)
l[1]
l.call(1)
