=begin
 This is a machine generated stub using stdlib-doc for <b>class SOCKSSocket</b>
 Ruby sources used:  RUBY-1.8.6-p111
 Created on Fri Dec 07 14:01:11 +0300 2007 by IntelliJ IDEA Ruby Plugin.
=end

#   
#    Class +Socket+ provides access to the underlying operating system
#    socket implementations. It can be used to provide more operating system
#    specific functionality than the protocol-specific socket classes but at the
#    expense of greater complexity. In particular, the class handles addresses
#    using +struct sockaddr+ structures packed into Ruby strings, which can be
#    a joy to manipulate.
#    
#    === Exception Handling
#    Ruby's implementation of +Socket+ causes an exception to be raised
#    based on the error generated by the system dependent implementation.
#    This is why the methods are documented in a way that isolate
#    Unix-based system exceptions from Windows based exceptions. If more
#    information on particular exception is needed please refer to the 
#    Unix manual pages or the Windows WinSock reference.
#    
#    
#    === Documentation by
#    * Zach Dennis
#    * Sam Roberts
#    * <em>Programming Ruby</em> from The Pragmatic Bookshelf.  
#    
#    Much material in this documentation is taken with permission from  
#    <em>Programming Ruby</em> from The Pragmatic Bookshelf.  
#    
class SOCKSSocket < TCPSocket
end
