foo do
  bar
rescue ArgumentError => e
  baz(e)
else
  ok
ensure
  qux
end

items.each do |item|
  process(item)
rescue StandardError
  next
end
