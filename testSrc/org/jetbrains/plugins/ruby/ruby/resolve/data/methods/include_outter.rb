# Created by IntelliJ IDEA.
# User: oleg
# Date: Feb 21, 2008
# Time: 7:06:28 PM
# To change this template use File | Settings | File Templates.

module A
    def foo
    end
end

class B
    include A
    foo
end

B.new.fo#caret#o