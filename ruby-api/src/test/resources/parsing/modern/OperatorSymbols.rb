undef_method :==, :!, :!=
[:to_s, :!~, :===, :<=>].each { |m| m }
value&.!= other
def !=(other)
  false
end
