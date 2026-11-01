def f(a, b = 1, *rest, c:, d: 2, **opts, &blk)
end

def g(name:, age: nil)
  [name, age]
end

def h(**options)
end
