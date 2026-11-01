ERRORS = [IOError, SystemCallError]

begin
  work
rescue *ERRORS => e
  puts e
rescue *(ERRORS + [TypeError])
  retry
end
