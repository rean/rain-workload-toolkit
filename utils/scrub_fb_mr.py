import sys
import uuid
import getopt
import hashlib

class FBLogScrubber:
    '''Class for scrubbing/anonymizing Facebook mapreduce logs'''
    
    JOB_ID_COL        = 0
    JOB_NAME_COL      = 1
    INTER_ARRIVAL_GAP = 6
    COLUMN_SEPARATOR = "\t"

    @staticmethod
    def scrub( input_fname, output_fname, deterministic=False ):
        print "Scrubbing {0}, output: {1}".format( input_fname, output_fname )

        # -open input_fname
        # -read every line
        # -for each line, we expect a specific format
        # -we're anonymizing the jobids and jobnames
        #  keep a hash table for jobids and jobnames
        #  when we read a jobid, see if it's in the
        #  hash table, if it is, then use that alias in the output file
        #  if the jobid is not in the hash table then generate a
        #  suitable alias (e.g., a uuid) to write out
        #  add the alias to the hash table for future reference
        try:
            input_file = None
            output_file = None

            input_file = open( input_fname, 'r' )
            output_file = open( output_fname, 'w' )
            
            jobid_aliases = {}
            jobname_aliases = {}

            # We need to shift up the inter-arrival gap info
            # Each line in the file contains the time since the
            # last job, however, since we'll be doing the job
            # submit and then sleeping, rather than sleeping
            # and then submitting the job the inter-arrival gap
            # from the next job is the sleep time after the current
            # job, so shifting inter-arrival gaps by one will
            # allow us to submit-then-sleep rather than sleep-then-submit
            interarrivals = []

            # Go through the file to get all the interarrival gaps
            for line in input_file:
                columns = line.split( FBLogScrubber.COLUMN_SEPARATOR )
                interarrival_gap = columns[FBLogScrubber.INTER_ARRIVAL_GAP]
                interarrivals.append( interarrival_gap )

            # Go back to the start of the file
            input_file.seek(0)
            line_count = 0
            
            for line in input_file:
                #print line
                columns = line.split( FBLogScrubber.COLUMN_SEPARATOR )
                #print len(columns)
                jobid = columns[FBLogScrubber.JOB_ID_COL]
                jobname = columns[FBLogScrubber.JOB_NAME_COL]
                
                #print jobid, jobname
                
                jobid_alias = None
                jobname_alias = None

                # look up jobid alias
                if jobid_aliases.has_key( jobid ):
                    jobid_alias = jobid_aliases[jobid]
                else:
                    # Use a non-deterministic alias, e.g., a uuid
                    # or something deterministic e.g. a hash function
                    if not deterministic:
                        jobid_alias = "jobid-" + str( uuid.uuid4() )
                    else:
                        jobid_alias = "jobid-" + \
                            hashlib.sha256( jobid ).hexdigest()
                    jobid_aliases[jobid] = jobid_alias

                # look up the jobname alias
                if jobname_aliases.has_key( jobname ):
                    jobname_alias = jobname_aliases[jobname]
                else:
                    # Use a non-deterministic alias, e.g., a uuid
                    # or something deterministic e.g. a hash function
                    if not deterministic:
                        jobname_alias = "jobname-" + str( uuid.uuid4() )
                    else:
                        jobname_alias = "jobname-" + \
                            hashlib.sha256( jobname ).hexdigest()
                    jobname_aliases[jobname] = jobname_alias
                    
                scrubbed_line = ""
                # Create the anonymized line to write out
                for i in range( len(columns) ):
                    if i == FBLogScrubber.JOB_ID_COL:
                        scrubbed_line += jobid_alias
                    elif i == FBLogScrubber.JOB_NAME_COL:
                        scrubbed_line += jobname_alias
                    elif i == FBLogScrubber.INTER_ARRIVAL_GAP:
                        # grab the gap from the next line (if it exists)
                        if (line_count + 1) < len(interarrivals):
                            scrubbed_line += str(interarrivals[line_count+1])
                        else: 
                            scrubbed_line += "0"
                    else:
                        scrubbed_line += columns[i]
                        
                    if (i+1) < len(columns):
                        scrubbed_line += FBLogScrubber.COLUMN_SEPARATOR
                
                #print scrubbed_line
                output_file.write( scrubbed_line + "\n" );
                line_count = line_count + 1
        finally:
            if input_file != None and not input_file.closed:
                input_file.close()
            if output_file != None and not output_file.closed:
                output_file.close()

def usage():
    print( "Usage: {0} [--input <filename>] [--output <filename>] [--hash]"\
               .format( sys.argv[0] ) )
    print( "--input (required)" )
    print( "--output (optional, input file appended with" \
               " '.scrub' suffix if unspecified)" )
    print( "--hash (optional, uses a uuid rather than a" \
           " deterministic transform)" )

if __name__ == '__main__':
    input_fname = None 
    output_fname = None 
    deterministic = False

    argv = sys.argv[1:]
    
    # Parse command line options
    try:
        opts, args = getopt.getopt( argv, "h", ["input=", "output=", \
                                                 "hash", "help"] )    
    except getopt.GetoptError:
        print sys.exec_info()
        usage()
        sys.exit(2)
        
    for opt, arg in opts:
        if opt in ( "-h", "--help" ):
            usage()
            sys.exit(0)
        elif opt == "--input":
            input_fname = arg
        elif opt == "--output":
            output_fname = arg
        # Use a deterministic transform instead of a uuid
        elif opt == "--hash": 
            deterministic = True
        else:
            usage()
            sys.exit(2)
    
    if input_fname == None:
        usage()
        sys.exit(2)

    if output_fname == None:
        output_fname = input_fname + ".scrub"

    FBLogScrubber.scrub( input_fname, output_fname, deterministic )
