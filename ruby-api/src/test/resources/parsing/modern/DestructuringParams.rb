def pair((a, b), c)
  a + b + c
end

[[1, [2, 3]]].each { |a, (b, c)| puts a + b + c }
