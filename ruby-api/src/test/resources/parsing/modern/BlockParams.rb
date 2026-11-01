items.each { |a, b = 1, *c, d:, e: 2, **f, &g| a }
items.each { |a; b| b = a }
items.each { |(a, b), c| a }
items.each do |key, value|
  key
end
