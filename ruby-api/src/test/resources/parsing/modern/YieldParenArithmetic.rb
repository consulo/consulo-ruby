def total
  yield(1) - yield(2)
end

def value(x)
  super(x) || super
end
