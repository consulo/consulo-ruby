RSpec.describe Calculator do
  describe Calculator::Add do
    it "adds" do
      expect(1 + 1).to eq(2)
    end
  end
end

describe Calculator do
end

foo bar do
end

foo bar, baz do |x|
  x
end

task build: :compile do
end

assert_difference("Post.count") do
  post posts_url, params: { post: { title: "x" } }
end

list.each do |x|
  puts x
end

puts(items.map do |x| x end)

value = compute input do
  1
end
