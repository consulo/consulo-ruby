=begin
 This is a machine generated stub using stdlib-doc for <b>class Symbol</b>
 Ruby sources used:  RUBY-1.8.6-p111
 Created on Fri Dec 07 14:01:02 +0300 2007 by IntelliJ IDEA Ruby Plugin.
=end

#   
#     <code>Symbol</code> objects represent names and some strings
#     inside the Ruby
#     interpreter. They are generated using the <code>:name</code> and
#     <code>:"string"</code> literals
#     syntax, and by the various <code>to_sym</code> methods. The same
#     <code>Symbol</code> object will be created for a given name or string
#     for the duration of a program's execution, regardless of the context
#     or meaning of that name. Thus if <code>Fred</code> is a constant in
#     one context, a method in another, and a class in a third, the
#     <code>Symbol</code> <code>:Fred</code> will be the same object in
#     all three contexts.
#        
#        module One
#          class Fred
#          end
#          $f1 = :Fred
#        end
#        module Two
#          Fred = 1
#          $f2 = :Fred
#        end
#        def Fred()
#        end
#        $f3 = :Fred
#        $f1.id   #=> 2514190
#        $f2.id   #=> 2514190
#        $f3.id   #=> 2514190
#        
#    
class Symbol < Object
    public
    #  
    # Symbol.all_symbols    => array
    #   
    #     
    #     Returns an array of all the symbols currently in Ruby's symbol
    #     table.
    #        
    #        Symbol.all_symbols.size    #=> 903
    #        Symbol.all_symbols[1,20]   #=> [:floor, :ARGV, :Binding, :symlink,
    #                                        :chown, :EOFError, :$;, :String, 
    #                                        :LOCK_SH, :"setuid?", :$<, 
    #                                        :default_proc, :compact, :extend, 
    #                                        :Tms, :getwd, :$=, :ThreadGroup,
    #                                        :wait2, :$>]
    #    
    # 
    def self.all_symbols()
        #This is a stub, used for indexing
    end
    public
    #  
    # sym.to_i      => fixnum
    #   
    #     
    #     Returns an integer that is unique for each symbol within a
    #     particular execution of a program.
    #        
    #        :fred.to_i           #=> 9809
    #        "fred".to_sym.to_i   #=> 9809
    #    
    # 
    def to_i()
        #This is a stub, used for indexing
    end
    public
    #    :nodoc:   
    # 
    def to_int()
        #This is a stub, used for indexing
    end
    public
    #  
    # sym.inspect    => string
    #   
    #     
    #     Returns the representation of <i>sym</i> as a symbol literal.
    #        
    #        :fred.inspect   #=> ":fred"
    #    
    # 
    def inspect()
        #This is a stub, used for indexing
    end
    public
    #  
    # sym.id2name   => string
    # sym.to_s      => string
    #   
    #     
    #     Returns the name or string corresponding to <i>sym</i>.
    #        
    #        :fred.id2name   #=> "fred"
    #    
    # 
    # 
    def to_s()
        #This is a stub, used for indexing
    end
    public
    #  
    # sym.id2name   => string
    # sym.to_s      => string
    #   
    #     
    #     Returns the name or string corresponding to <i>sym</i>.
    #        
    #        :fred.id2name   #=> "fred"
    #    
    # 
    # 
    def id2name()
        #This is a stub, used for indexing
    end
    public
    #  
    # sym.to_sym   => sym
    #   
    #    
    #    In general, <code>to_sym</code> returns the <code>Symbol</code> corresponding
    #    to an object. As <i>sym</i> is already a symbol, <code>self</code> is returned
    #    in this case.
    #    
    # 
    def to_sym()
        #This is a stub, used for indexing
    end
    public
    #  
    # obj == other        => true or false
    # obj.equal?(other)   => true or false
    # obj.eql?(other)     => true or false
    #   
    #     
    #     Equality---At the <code>Object</code> level, <code>==</code> returns
    #     <code>true</code> only if <i>obj</i> and <i>other</i> are the
    #     same object. Typically, this method is overridden in descendent
    #     classes to provide class-specific meaning.
    #   
    #     Unlike <code>==</code>, the <code>equal?</code> method should never be
    #     overridden by subclasses: it is used to determine object identity
    #     (that is, <code>a.equal?(b)</code> iff <code>a</code> is the same
    #     object as <code>b</code>).
    #   
    #     The <code>eql?</code> method returns <code>true</code> if
    #     <i>obj</i> and <i>anObject</i> have the
    #     same value. Used by <code>Hash</code> to test members for equality.
    #     For objects of class <code>Object</code>, <code>eql?</code> is
    #     synonymous with <code>==</code>. Subclasses normally continue this
    #     tradition, but there are exceptions. <code>Numeric</code> types, for
    #     example, perform type conversion across <code>==</code>, but not
    #     across <code>eql?</code>, so:
    #        
    #        1 == 1.0     #=> true
    #        1.eql? 1.0   #=> false
    #    
    # 
    def ===(p1)
        #This is a stub, used for indexing
    end
end
