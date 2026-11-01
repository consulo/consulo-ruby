text = <<~EOS
  hello
    world
EOS
raw = <<~'EOS'
  no #{interpolation}
EOS
puts(<<~SQL.strip)
  SELECT 1
SQL
