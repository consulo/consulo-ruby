# Writes Ruby stubs of the core library of this interpreter from its RBS signatures:
# one file per class or module, and builtin.rb with the global variables, the constants and a require of each file.
#
# usage: ruby generate_stubs.rb <output directory>
require "rbs"
require "fileutils"

KEYWORDS = %w[BEGIN END alias and begin break case class def defined? do else elsif end ensure false for if in module
              next nil not or redo rescue retry return self super then true undef unless until when while yield
              __FILE__ __LINE__ __ENCODING__].freeze

out = ARGV[0] or abort("output directory expected")
FileUtils.mkdir_p(out)

env = RBS::Environment.from_loader(RBS::EnvironmentLoader.new).resolve_type_names

def plain_name(type_name)
  type_name.to_s.delete_prefix("::")
end

# The RBS comments start with an rdoc header: `<!-- rdoc-file=io.c - puts(*objects) -> nil -->`.
# Its call sequences become verbatim lines, the rest of it is dropped.
def comment_lines(comment, indent)
  return [] unless comment
  lines = []
  in_header = false
  comment.string.lines.map(&:rstrip).each do |line|
    if line.strip.match?(/\A<!--.*-->\z/)
      next
    elsif line.strip == "<!--"
      in_header = true
    elsif in_header && line.strip == "-->"
      in_header = false
      lines << ""
    elsif in_header
      lines << "    #{line.strip.delete_prefix("- ")}" if line.strip.start_with?("- ")
    else
      lines << line
    end
  end
  lines.shift while lines.first&.empty?
  lines.map { |line| "#{indent}# #{line}".rstrip }
end

def parameter_name(param, fallback, used)
  name = param.name&.to_s
  name = fallback if name.nil? || name.empty? || KEYWORDS.include?(name) || name !~ /\A[a-z_][A-Za-z0-9_]*\z/
  name = "#{name}#{used.size}" if used.include?(name)
  used << name
  name
end

def parameters(method_type)
  function = method_type.type
  return "*args" unless function.respond_to?(:required_positionals)
  used = []
  result = []
  index = 0
  function.required_positionals.each { |p| result << parameter_name(p, "arg#{index += 1}", used) }
  function.optional_positionals.each { |p| result << "#{parameter_name(p, "arg#{index += 1}", used)} = nil" }
  result << "*#{parameter_name(function.rest_positionals, "args", used)}" if function.rest_positionals
  function.trailing_positionals.each { |p| result << parameter_name(p, "arg#{index += 1}", used) }
  # a keyword named like a positional parameter is not valid Ruby
  keyword = ->(k) { !KEYWORDS.include?(k.to_s) && !used.include?(k.to_s) && (used << k.to_s) }
  function.required_keywords.each_key { |k| result << "#{k}:" if keyword.(k) }
  function.optional_keywords.each_key { |k| result << "#{k}: nil" if keyword.(k) }
  result << "**#{parameter_name(function.rest_keywords, "options", used)}" if function.rest_keywords
  result << "&block" if method_type.block
  result.join(", ")
end

def value_of(type)
  case type.to_s.delete_prefix("::")
  when "String" then '""'
  when "Integer" then "0"
  when "Float" then "0.0"
  when /\AArray/ then "[]"
  when /\AHash/ then "{}"
  when "Symbol" then ":symbol"
  when "bool", "true", "false" then "false"
  else "nil"
  end
end

files = []
env.class_decls.each do |type_name, entry|
  name = plain_name(type_name)
  decls = entry.respond_to?(:each_decl) ? entry.each_decl.to_a : entry.decls.map(&:decl)
  is_class = decls.any? { |d| d.is_a?(RBS::AST::Declarations::Class) }
  super_class = decls.map { |d| d.respond_to?(:super_class) ? d.super_class : nil }.compact.first
  header = is_class ? "class #{name}" : "module #{name}"
  header += " < #{plain_name(super_class.name)}" if super_class && name != "BasicObject"

  lines = ["# This is a stub generated from the RBS signatures of Ruby #{RUBY_VERSION}, used for indexing", ""]
  decls.each { |d| lines.concat(comment_lines(d.comment, "")) }
  lines << header
  decls.each do |decl|
    decl.members.each do |member|
      case member
      when RBS::AST::Members::Include, RBS::AST::Members::Extend
        keyword = member.is_a?(RBS::AST::Members::Include) ? "include" : "extend"
        lines << "  #{keyword} #{plain_name(member.name)}"
      when RBS::AST::Members::MethodDefinition
        method_type = member.overloads.first&.method_type
        params = method_type ? parameters(method_type) : ""
        # a module function is two methods - each gets the documentation
        comment = comment_lines(member.comment, "  ")
        if member.singleton? || member.kind == :singleton_instance
          lines.concat(comment)
          lines << "  def self.#{member.name}(#{params}); end"
        end
        if member.instance? || member.kind == :singleton_instance
          lines.concat(comment)
          lines << "  def #{member.name}(#{params}); end"
        end
      when RBS::AST::Members::Alias
        prefix = member.kind == :singleton ? "self." : ""
        lines << "  def #{prefix}#{member.new_name}(*args, &block); end"
      when RBS::AST::Members::AttrReader, RBS::AST::Members::AttrWriter, RBS::AST::Members::AttrAccessor
        keyword = { RBS::AST::Members::AttrReader => "attr_reader", RBS::AST::Members::AttrWriter => "attr_writer",
                    RBS::AST::Members::AttrAccessor => "attr_accessor" }[member.class]
        lines << "  #{keyword} :#{member.name}"
      end
    end
  end
  lines << "end"

  file = name.downcase.gsub("::", "__")
  File.write(File.join(out, "#{file}.rb"), lines.join("\n") + "\n")
  files << file
end

builtin = ["# This is a stub generated from the RBS signatures of Ruby #{RUBY_VERSION}, used for indexing", ""]
env.global_decls.each do |name, entry|
  # back references and match groups can not be assigned
  next if name.to_s =~ /\A\$([&`'+]|\d+)\z/
  decl = entry.respond_to?(:decl) ? entry.decl : entry
  builtin.concat(comment_lines(decl.comment, ""))
  builtin << "#{name} = #{value_of(decl.type)} #value is unknown, used for indexing."
end
builtin << ""
files.sort.each { |file| builtin << "require '#{file}'" }
builtin << ""
env.constant_decls.each do |type_name, entry|
  decl = entry.respond_to?(:decl) ? entry.decl : entry
  builtin.concat(comment_lines(decl.comment, ""))
  builtin << "#{plain_name(type_name)} = #{value_of(decl.type)}"
end
File.write(File.join(out, "builtin.rb"), builtin.join("\n") + "\n")
puts files.size
