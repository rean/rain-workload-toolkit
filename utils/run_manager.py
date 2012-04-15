import sys
import subprocess
import time
import re

class TrackValidation:
    '''
    Class for deciding whether a track summary from a run should be
    considered valid based on the overheads and response times recorded
    '''
    def __init__(self, trackname):
        self.track_name = trackname
        self.interval_name = None
        self.pct_overhead_ops = 0.0
        self.pct_overhead_ops_acceptable = False
        self.pct_ops_failed = 0.0
        self.pct_failed_ops_acceptable = False
        self.op_response_time_targets_met = True

    def is_acceptable(self):
        return self.pct_overhead_ops_acceptable and \
            self.pct_failed_ops_acceptable and \
            self.op_response_time_targets_met

class TrackSummary:
    '''
    Class for storing the summary results of a load track
    '''
    def __init__(self, trackname):
        self.name = trackname
        self.interval_name = None
        self.offered_load_ops_per_sec = 0
        self.effective_load_ops_per_sec = 0
        self.littles_estimate_ops_per_sec = 0
        self.effective_load_reqs_per_sec = 0
        self.operations_successful = 0
        self.operations_failed = 0
        self.average_op_response_time_sec = 0
        self.average_users = 0
        #self.op_response_time_targets_met = True
        self.op_response_times = {}
        self.op_proportions = {}
        # Specifiy validation thresholds
        self.pct_overhead_ops_threshold=5.0
        self.pct_failed_ops_threshold=5.0
        self.op_response_time_thresholds = {}
        self.validation_note = "n/a"

    def validate( self ):
        result = TrackValidation(self.name)
        # check overheads
        
        if self.littles_estimate_ops_per_sec > 0:
            result.pct_overhead_ops = \
                ((self.littles_estimate_ops_per_sec - \
                      self.effective_load_ops_per_sec)/\
                     self.littles_estimate_ops_per_sec)*100.0

            if result.pct_overhead_ops <= self.pct_overhead_ops_threshold:
                result.pct_overhead_ops_acceptable = True
            else:
                result.pct_overhead_ops_acceptable = False
                self.validation_note = "little's law overhead > {0}%"\
                    .format(self.pct_overhead_ops_threshold)
        else:
            result.pct_overhead_ops_acceptable = True

        total_ops = self.operations_successful + self.operations_failed
        if total_ops > 0:
            result.pct_ops_failed = \
                (float(self.operations_failed)/float(total_ops))*100.0
        else: 
            result.pct_ops_failed = 0
            self.validation_note = "slept through steady state"
        
        if result.pct_ops_failed <= self.pct_failed_ops_threshold:
            result.pct_failed_ops_acceptable = True
        else:
            self.validation_note = "pct ops failed > {0}%"\
                .format(self.pct_failed_ops_threshold)

        # For each of the op_response_time thresholds
        # see if they've been met
        for k,v in self.op_response_time_thresholds.items():
            if self.op_response_times.has_key( k ):
                observed = self.op_response_times[k]
                if observed[0] > v[0] or observed[1] > v[1]:
                    result.op_response_time_targets_met = False
                    self.validation_note = "response time target(s) not met"
        return result

    def __repr__(self):
        '''
        Craft our own string representation
        '''
        # Validate ourselves first
        # Construct the string with our results
        # callers can print this out, write it to a file, etc.
        validation = self.validate()                                 
                
        return RainOutputParser.RESULTS_DATA.format(\
            self.name, \
            self.effective_load_ops_per_sec, \
            self.littles_estimate_ops_per_sec,\
            self.effective_load_reqs_per_sec, \
            validation.pct_overhead_ops, \
            self.average_op_response_time_sec, \
            self.average_users, \
            self.operations_successful, \
            self.operations_failed, \
            validation.pct_ops_failed, \
            str(validation.is_acceptable()),\
            self.validation_note )

class RainOutputParser:
    '''
    Class for parsing the output from Rain. Returns a list of track summaries
    '''
    RESULTS_HEADER = "{0:<20} {1:<10s} {2:<10s} {3:<10s} {4:<10s} {5:<10s} "\
                "{6:<10s} {7:<10s} {8:<10s} {9:<10s} {10:<5s} {11}".format(\
                "track",\
                "eff-ops/s",\
                "ltl-ops/s",\
                "eff-reqs/s",\
                "%ovh-ops",\
                "avg-resp(s)",\
                "avg-users",\
                "succeeded",\
                "failed",\
                "%failed",\
                "passed",\
                "(note)")
    RESULTS_DATA = "{0:<20} {1:10.4f} {2:10.4f} {3:10.4f} {4:10.4f} {5:10.4f}"\
                   "{6:10.4f} {7:10} {8:10} {9:10.4f} {10:<5s} ({11})"


    @staticmethod
    def parse_interval_output( output ):
        # Get each track and for each track find the interval markers
        track_results = []
        track_pattern = \
            re.compile( '\[TRACK: (.*)\] starting load scheduler' )
        tracks = \
            track_pattern.findall( output )
        
        # for each track go get some numbers from the final results section
        for track in tracks:
            #print track
            
            # sub-pattern of the end of a result line from rain
            # <metric><space*>:<space+><decimal value>
            number_pattern = '([-+]?(\d+(\.\d*)?|\.\d+)([eE][-+]?\d+)?)'
            result_line_tail_pattern = \
                '\s*:\s+([-+]?(\d+(\.\d*)?|\.\d+)([eE][-+]?\d+)?)'
            name_tail_pattern = \
                '\s*:\s+(.*)'
            
            track_final_results_pattern = \
              re.compile( '\[SCOREBOARD TRACK: ' + track + '\] Final results' )
        
            track_interval_results_pattern = \
              re.compile( '\[SCOREBOARD TRACK: ' + track + '\] Interval results' )
            track_final_results_interval_name_pattern = \
              re.compile( '\[SCOREBOARD TRACK: ' + track + '\] Interval name' + name_tail_pattern )
            track_offered_load_ops_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                       'Offered load \(ops/sec\)' + result_line_tail_pattern )
            track_effective_load_ops_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                      'Effective load \(ops/sec\)' + result_line_tail_pattern )
            track_effective_load_reqs_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                'Effective load \(requests/sec\)' + result_line_tail_pattern )
            track_ops_successful_pattern = \
               re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
               'Operations successfully completed' + result_line_tail_pattern )
            track_ops_failed_pattern = \
               re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                           'Operations failed' + result_line_tail_pattern )
            track_avg_op_response_time_pattern = \
             re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                         'Average operation response time \(s\)' + \
                             result_line_tail_pattern )
            track_avg_users_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                      'Active users' + result_line_tail_pattern )

            # Patterns for operations - doesn't group like we'd like
            # but matches the right lines
            track_operation_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\]' \
                       '\|(.+)\|'\
                       '\s*(' + number_pattern + ')%\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|')

            # Find the "Final results" marker for this track so we know where
            # all the track intervals end
            final_results_section_start = None    
            match = track_final_results_pattern.search( output )
            if match:
                final_results_section_start = match.start()
            
            # Find the "Interval results" markers for this track and then
            # search for individual metrics
            match = track_interval_results_pattern.search( output )
            if match:    
                interval_name_start = None                

                interval_pattern = \
                    re.compile( '\[SCOREBOARD TRACK: ' + track + '\] Interval name' + name_tail_pattern )

                intervals = \
                    interval_pattern.findall( output )

                intervals2 = interval_pattern.finditer( output )

                #print intervals2
                interval_endpoints = []
                
                for interval in intervals2:
                    interval_endpoints.append( interval.start() )
                
                #for interval in intervals:
                for i in range(0, len(intervals) ):
                    interval = intervals[i]
                    interval_end = None
                    #print( "{0} start: {1}".format( interval, interval_endpoints[i] ) )
                    if i+1 < len(intervals):
                        interval_end = interval_endpoints[i+1]
                        #print( "{0} end: {1}".format( interval, interval_endpoints[i+1] ) )
                    else:
                        #print( "{0} end: {1}".format( interval, final_results_section_start ) )
                        interval_end = final_results_section_start # use the final results marker start
                    
                    #print interval
                    summary = TrackSummary( "{0}-{1}".format(track, interval) )

                    interval_results_start_pattern = \
                        re.compile( '\[SCOREBOARD TRACK: ' + track + '\] Interval name\s*:\s+(' + interval + ')' )
                    interval_results_section_start = interval_results_start_pattern.search( output ).start()
                    
                    summary.offered_load_ops_per_sec = \
                    float( track_offered_load_ops_pattern.search\
                             ( output, interval_results_section_start ).group(1) )
                    
                    summary.effective_load_ops_per_sec = \
                    float( track_effective_load_ops_pattern.search\
                             ( output, interval_results_section_start ).group(1) )
                    summary.effective_load_reqs_per_sec = \
                        float( track_effective_load_reqs_pattern.search\
                                 ( output, interval_results_section_start ).group(1) )
                    summary.operations_successful = \
                        long( track_ops_successful_pattern.search\
                                 ( output, interval_results_section_start ).group(1) )
                    summary.operations_failed = \
                        long( track_ops_failed_pattern.search\
                                 ( output, interval_results_section_start ).group(1) )
                    summary.average_op_response_time_sec = \
                        float( track_avg_op_response_time_pattern.search\
                                 ( output, interval_results_section_start ).group(1) )
                    summary.average_users = \
                        float( track_avg_users_pattern.search\
                                 ( output, interval_results_section_start ).group(1) )
                    
                    # Need to make sure that we only include the operation results for
                    # the current interval. If intervals don't contain
                    # the same operations, then operation results could get mis-aligned
                    # with the intervals
                    
                     
                    # find all the operations and print out the 90th and 99th
                    # percentiles
                    for opMatch in track_operation_pattern.\
                        finditer( output, interval_results_section_start ):
                    
                        if opMatch.start() < interval_end:
                            #print opMatch
                            #print opMatch.group(0)
                            # Split group(0) on |
                            opCounters = opMatch.group(0).split( "|" )
                            #for s in opCounters:
                            #    print s
                            #print opCounters[1].strip(), opCounters[8], opCounters[9]
                            # Store the op name with a tuple (90th pct,99th pct, avg, min, max)
                            
                            #if not summary.op_response_times.has_key(opCounters[1].strip()):
                            summary.op_response_times[opCounters[1].strip()]=\
                                    (float(opCounters[8].strip()), \
                                     float(opCounters[9].strip()), \
                                     float(opCounters[5].strip()), \
                                     float(opCounters[6].strip()), \
                                     float(opCounters[7].strip())) 
                            #if not summary.op_proportions.has_key(opCounters[1].strip()):
                            summary.op_proportions[opCounters[1].strip()]=float(opCounters[2].replace('%', ''))/100.0
                            #print summary.op_proportions[opCounters[1].strip()]
                                                        

                    # save the summary to the list we have so far
                    #print summary.name, summary.op_response_times
                    track_results.append(summary)
        # return the list of track results we found
        return track_results

    @staticmethod
    def parse_output( output ):
        '''
        Parse the output for each track and return a list with summary results
        for each track
        '''
        #print output
        # Find all the track names
        track_results = []
        track_pattern = re.compile( '\[TRACK: (.*)\] starting load scheduler' )
        tracks = track_pattern.findall( output )
       
        # for each track go get some numbers from the final results section
        for track in tracks:
            #print track
            # sub-pattern of the end of a result line from rain
            # <metric><space*>:<space+><decimal value>
            number_pattern = '([-+]?(\d+(\.\d*)?|\.\d+)([eE][-+]?\d+)?)'
            result_line_tail_pattern = \
                '\s*:\s+([-+]?(\d+(\.\d*)?|\.\d+)([eE][-+]?\d+)?)'
            track_final_results_pattern = \
              re.compile( '\[SCOREBOARD TRACK: ' + track + '\] Final results' )
            track_offered_load_ops_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                       'Offered load \(ops/sec\)' + result_line_tail_pattern )
            track_effective_load_ops_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                      'Effective load \(ops/sec\)' + result_line_tail_pattern )
            track_littles_estimate_ops_pattern = \
              re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
              'Little\'s Law Estimate \(ops/sec\)' + result_line_tail_pattern )
            track_effective_load_reqs_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                'Effective load \(requests/sec\)' + result_line_tail_pattern )
            track_ops_successful_pattern = \
               re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
               'Operations successfully completed' + result_line_tail_pattern )
            track_ops_failed_pattern = \
               re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                           'Operations failed' + result_line_tail_pattern )
            track_avg_op_response_time_pattern = \
             re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                         'Average operation response time \(s\)' + \
                             result_line_tail_pattern )
            track_avg_users_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\] ' \
                      'Average number of users' + result_line_tail_pattern )

            # Patterns for operations - doesn't group like we'd like
            # but matches the right lines
            track_operation_pattern = \
                re.compile( '\[SCOREBOARD TRACK: ' + track + '\]' \
                       '\|(.+)\|'\
                       '\s*(' + number_pattern + ')%\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|'\
                       '\s*(' + number_pattern + ')\s*\|')

            # Create a new track summary instance to fill in
            summary = TrackSummary(track)
            # Find the "Final results" marker for this track and then
            # search for individual metrics
            match = track_final_results_pattern.search( output )
            if match:
                # search the substring from the final results marker 
                # for more specific stuff after this point
                final_results_section_start = match.start()
                summary.offered_load_ops_per_sec = \
                    float( track_offered_load_ops_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                summary.effective_load_ops_per_sec = \
                    float( track_effective_load_ops_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                summary.littles_estimate_ops_per_sec = \
                    float( track_littles_estimate_ops_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                summary.effective_load_reqs_per_sec = \
                    float( track_effective_load_reqs_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                summary.operations_successful = \
                    long( track_ops_successful_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                summary.operations_failed = \
                    long( track_ops_failed_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                summary.average_op_response_time_sec = \
                    float( track_avg_op_response_time_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                summary.average_users = \
                    float( track_avg_users_pattern.search\
                             ( output, final_results_section_start ).group(1) )
                
                # find all the operations and print out the 90th and 99th
                # percentiles
                for opMatch in track_operation_pattern.\
                    finditer( output, final_results_section_start ):
                
                    #print opMatch
                    #print opMatch.group(0)
                    # Split group(0) on |
                    opCounters = opMatch.group(0).split( "|" )
                    #for s in opCounters:
                    #    print s
                    #print opCounters[1].strip(), opCounters[8], opCounters[9]
                    # Store the op name with a tuple (90th pct,99th pct, avg, min, max)
                    summary.op_response_times[opCounters[1].strip()]=\
                        (float(opCounters[8].strip()), \
                             float(opCounters[9].strip()), \
                             float(opCounters[5].strip()), \
                             float(opCounters[6].strip()), \
                             float(opCounters[7].strip())) 
                #else:
                #   print "no operation match" 

                # save the summary to the list we have so far
                #print summary.name, summary.op_response_times
                track_results.append(summary)

                
        
        # return the list of track results we found
        return track_results

    @staticmethod
    def print_results( results, output_stream=sys.stdout ):
        output_stream.write( RainOutputParser.RESULTS_HEADER + '\n' )
        for result in results:
            output_stream.write( str(result) + '\n' )
        # Flush the output stream
        output_stream.flush()


class RunManager():
    """
    A class for launching runs and validating results.
    """
    @staticmethod
    def run_rain( config_file="config/rain.config.ac.json", \
                  classpath=".:rain.jar:workloads/httptest.jar", \
                  min_heapsize="-Xms256m", \
                  max_heapsize="-Xmx1g", \
                  gc_policy="-XX:+DisableExplicitGC" ):
        """
        Launch process for Rain, get pid and then start querying
        sched stats
        """
        args = ["java", min_heapsize, max_heapsize, gc_policy, \
                "-XX:-UseGCOverheadLimit", \
                "-cp", classpath, "radlab.rain.Benchmark", config_file  ]
        
        rain_process = None
        rain_process = subprocess.Popen(args, bufsize=-1, \
                                stdin=subprocess.PIPE, stdout=subprocess.PIPE)
        rain_pid = rain_process.pid
        print( "[Run manager] rain pid: %d" % rain_pid )
        
        # Sit and wait until Rain exits then parse the output
        # Return the raw output, clients can parse it themselves 
        # or use the RainOutputParser to make sense
        # of it
        #output = rain_process.stdout.read()
        output, err = rain_process.communicate()
        return output

'''
Example command line
java -Xms256m -Xmx1g -XX:+DisableExplicitGC -cp .:rain.jar:workloads/httptest.jar radlab.rain.Benchmark config/rain.config.ac.json
'''

def run2():
    try:
        #results_file = open( "./rain.out", 'r' )
        #results_file = open( "./rain.predictable.out", 'r' )
        results_file = open( "./nonpop_run_fixed_url_log_nodes.txt", 'r' )
        results = results_file.read()
        track_results = RainOutputParser.parse_output( results )
        for result in track_results:
            # Set some 90th and 99th pctile thresholds
            result.pct_overhead_ops_threshold=10.0
            result.pct_failed_ops_threshold=5.0
            # Set the desired 90th and 99th percentile thresholds for
            # the 50ms, 100ms, 200ms operations - set everything to
            # 500 ms = 0.5s. Threshold units = seconds
            result.op_response_time_thresholds['FixedUrl']=\
                    (0.5,0.5)

            # Set some 90th and 99th pctile thresholds
            #result.op_response_time_thresholds['PingHome']=(0.001,0.005)
            #result.op_response_time_thresholds['PingHome']=(0.001,0.005)
            
        RainOutputParser.print_results( track_results )
    #except:
    #    print "something broke"
    finally:
        if not results_file.closed:
            results_file.close()

def run3():
    
    config_file = "config/rain.config.ac.json"
    classpath = ".:rain.jar:workloads/httptest.jar"    
    output = RunManager.run_rain(config_file, classpath)
    track_results = RainOutputParser.parse_output( output )                
    RainOutputParser.print_results( track_results, sys.stdout )           
    #return track_results 

if __name__=='__main__':
      run2()
