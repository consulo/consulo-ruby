result = items
  .select { |item| item.ok? }
  .map(&:name)
  &.first
other = value
  .strip
