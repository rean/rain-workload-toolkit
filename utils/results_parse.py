import os
import sys
import math
from run_manager import RainOutputParser
# use numpy for all the stats stuff
import numpy

if __name__=='__main__':
    
    repeats = 4
    results_dir = "300secs"
    #input_fname = "{0}/expt_2V1P_users_100_size_4096_run_0.log".format( results_dir )

    #results_file = open( input_fname, "r" )
    #results = results_file.read()
    #track_results = RainOutputParser.parse_output( results )
    #RainOutputParser.print_results( track_results )

    expt_tag = "2V1P"
    users = [100, 200, 400, 800, 1600, 3200]
    sizes = [4096, 8192]
    header = False

    # At the end of the day we want:
    # users, avg tput, sd tput, 95% conf tput, avg resp, avg get, 90th get, 99th get, avg put, 90th put, 99th put

    # Manually print
    size_stats = {}
    for size in sizes:
        user_stats = {}
        for u in users:
            
            user_tputs = []
            user_resp_times = []
            user_gets_avg = []
            user_gets_90th = []
            user_gets_99th = []
            user_puts_avg = []
            user_puts_90th = []
            user_puts_99th = []

            for run in range(0, repeats):
                log_fname = "expt_{0}_users_{1}_size_{2}_run_{3}.log".format( expt_tag, u, size, run )
                results_file = None
                try:
                    input_fname = "{0}/{1}".format( results_dir, log_fname )
                    results_file = open( input_fname, "r" )
                    results = results_file.read()
                    track_results = RainOutputParser.parse_output( results )
                    for result in track_results:
                        result.pct_overhead_ops_threshold=10.0
                        user_tputs.append( result.effective_load_ops_per_sec )
                        user_resp_times.append( result.average_op_response_time_sec )
                        user_gets_avg.append( result.op_response_times["Get"][2] )
                        user_gets_90th.append( result.op_response_times["Get"][0] )
                        user_gets_99th.append( result.op_response_times["Get"][1] )
                        user_puts_avg.append( result.op_response_times["Put"][2] )
                        user_puts_90th.append( result.op_response_times["Put"][0] )
                        user_puts_99th.append( result.op_response_times["Put"][1] )

                    if not header:
                        print( RainOutputParser.RESULTS_HEADER + '\n' )
                        header = True
                    
                    print( str(track_results) + "\t" + str(size/1024) + "K" )
                    
                except Exception as ex:
                    print ex
                finally:
                    if results_file != None:
                        results_file.close()
            
            #print user_tputs
            avg_tput = numpy.average( user_tputs )
            std_tput = numpy.std( user_tputs )
            # Compute the 95% confidence intervals
            conf95_tput = 1.96 * (std_tput/math.sqrt(len(user_tputs) ) )
            avg_resp_time = numpy.average( user_resp_times )
            std_resp_time = numpy.std( user_resp_times )
            conf95_resp_time = 1.96 * (std_resp_time/math.sqrt( len(user_resp_times) ) )
            # Compute the average get, and put
            avg_get_time = numpy.average( user_gets_avg )
            std_get_time = numpy.std( user_gets_avg )
            conf95_get_time = 1.96 * (std_get_time/math.sqrt( len(user_gets_avg) ) )

            avg_put_time = numpy.average( user_puts_avg )
            std_put_time = numpy.std( user_puts_avg )
            conf95_put_time = 1.96 * (std_put_time/math.sqrt( len(user_puts_avg) ) )
            # Compute 90th get and put
            avg_get_90th = numpy.average( user_gets_90th )
            std_get_90th = numpy.std( user_gets_90th )
            conf95_get_90th = 1.96 * (std_get_90th/math.sqrt( len(user_gets_90th) ) )

            avg_put_90th = numpy.average( user_puts_90th )
            std_put_90th = numpy.std( user_puts_90th )
            conf95_put_90th = 1.96 * (std_put_90th/math.sqrt( len(user_puts_90th) ) )
            # Compute 99th get and put
            avg_get_99th = numpy.average( user_gets_99th )
            std_get_99th = numpy.std( user_gets_99th )
            conf95_get_99th = 1.96 * (std_get_99th/math.sqrt( len(user_gets_99th) ) )

            avg_put_99th = numpy.average( user_puts_99th )
            std_put_99th = numpy.std( user_puts_99th )
            conf95_put_99th = 1.96 * (std_put_99th/math.sqrt( len(user_puts_99th) ) )

            # Now tally up things per-user
            user_stats[int(u)] = (avg_tput, conf95_tput, avg_resp_time, conf95_resp_time, avg_get_time, conf95_get_time, avg_put_time, conf95_put_time, avg_get_90th, conf95_get_90th, avg_put_90th, conf95_put_90th, avg_get_99th, conf95_get_99th, avg_put_99th, conf95_put_99th )

            #print user_stats[u]


    
        # Save the results per size
        size_stats[int(size)] = user_stats
    
    for size in sorted (size_stats.keys()):
        per_user = size_stats[size]
        # print header
        print("size,#users,avg_tput,tput_95conf,avg_resp(s),resp_95conf,avg_get(s),get_95conf,avg_put(s),put_95conf,90th_get(s),90th_get_95conf,90th_put(s),90th_put_conf,99th_get(s),99th_get_95conf,99th_put(s),99th_put_conf" )
        
        for num_users in sorted(per_user.keys()):
            stats = per_user[num_users]
            print( "{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11},{12},{13},{14},{15},{16},{17}".format( size, num_users, stats[0], stats[1], stats[2], stats[3], stats[4], stats[5], stats[6], stats[7], stats[8], stats[9], stats[10], stats[11], stats[12], stats[13], stats[14], stats[15] ) )
    
