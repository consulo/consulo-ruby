=begin
 This is a machine generated stub using stdlib-doc for <b>module Precision</b>
 Ruby sources used:  RUBY-1.8.6-p111
 Created on Fri Dec 07 14:11:55 +0300 2007 by IntelliJ IDEA Ruby Plugin.
=end

#   
#    Precision is a mixin for concrete numeric classes with
#    precision.  Here, `precision' means the fineness of approximation
#    of a real number, so, this module should not be included into
#    anything which is not a subset of Real (so it should not be
#    included in classes such as +Complex+ or +Matrix+).
#   
# 
module Precision
    public
    #   
    #    call_seq:
    #      included
    #   
    #    When the +Precision+ module is mixed-in to a class, this +included+
    #    method is used to add our default +induced_from+ implementation
    #    to the host class.
    #    
    # 
    def self.included(p1)
        #This is a stub, used for indexing
    end
    public
    #  
    # num.prec(klass)   => a_klass
    #   
    #     
    #     Converts _self_ into an instance of _klass_. By default,
    #     +prec+ invokes 
    #   
    #        klass.induced_from(num)
    #   
    #     and returns its value. So, if <code>klass.induced_from</code>
    #     doesn't return an instance of _klass_, it will be necessary
    #     to reimplement +prec+.
    #    
    # 
    def prec(klass)
        #This is a stub, used for indexing
    end
    public
    #  
    # num.prec_i  =>  Integer
    #   
    #     
    #     Returns an +Integer+ converted from _num_. It is equivalent 
    #     to <code>prec(Integer)</code>.
    #    
    # 
    def prec_i()
        #This is a stub, used for indexing
    end
    public
    #  
    # num.prec_f  =>  Float
    #   
    #     
    #     Returns a +Float+ converted from _num_. It is equivalent 
    #     to <code>prec(Float)</code>.
    #    
    # 
    def prec_f()
        #This is a stub, used for indexing
    end
end
