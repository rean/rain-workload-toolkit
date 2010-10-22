import sys
import subprocess
import time
import re
import os
import simplejson as json
import getopt
from run_manager import RunManager, RainOutputParser

'''Example config
{
    "profilesCreatorClass": "radlab.rain.workload.httptest.PredictableAppProfileCreator",
    "profilesCreatorClassParams": {
         "baseHostIp": "127.0.0.1",
         "hostPort": 80,
         "popularHostFraction": 0.2,
         "popularHostLoadFraction": 0.5,
         "numHostTargets": 10,
         "userPopulation": 100,
         "meanThinkTime": 5,
         "generatorParameters": {
             "operationWorkDone": [50, 100, 200],
             "operationMix": [30, 30, 40],
             "operationBusyPct" : [50, 75, 75],
             "memorySizes": ["small", "med", "large"],
             "memoryMix": [10,20,70]
             }
    },
    "timing": {
        "rampUp": 10,
        "duration": 60,
        "rampDown": 10
    }
}
'''

class PredictableAppGeneratorParameters:
    '''
    Rain configuration object for generator parameters for
    the PredtictableAppGenerator
    '''
    def __init__(self):
        self.operationWorkDone = []
        self.operationMix = []
        self.operationBusyPct = []
        self.memorySizes = []
        self.memorymix = []

    def to_json( self ):
        dict = {}
        dict['operationWorkDone'] = self.operationWorkDone
        dict['operationMix'] = self.operationMix
        dict['operationBusyPct'] = self.operationBusyPct
        dict['memorySizes'] = self.memorySizes
        dict['memoryMix'] = self.memoryMix
        return dict

class PreditableAppTestConfig:
    '''
    Rain configuration object for PredictableApp tests
    '''

    def __init__(self):
        self.generatorParameters = PredictableAppGeneratorParameters()
        # Profile creator class to use
        self.profilesCreatorClass = \
            "radlab.rain.workload.httptest.PredictableAppProfileCreator"
        # Profile creator params
        self.baseHostIp = "127.0.0.1" # base host ip
        self.numHostTargets = 10 # total number of hosts
        self.hostPort = 8080 # server port
        self.popularHostFraction = 0.2 # Fraction of popular hosts
        self.popularHostLoadFraction = 0.5 # % aggregate traffic for popular
        self.userPopulation = 100 # aggregate number of users to emulate
        self.meanThinkTime = 5 # seconds
        # Timing info
        self.rampUp = 10 # seconds
        self.duration = 60 # seconds
        self.rampDown = 10 # seconds

    def to_json( self ):
        dict = {}
        dict['profilesCreatorClass'] = self.profilesCreatorClass
        # sub-map with profile creator parameters
        creatorParams = {}
        # set all the creator params
        creatorParams['baseHostIp'] = self.baseHostIp
        creatorParams['numHostTargets'] = self.numHostTargets
        creatorParams['hostPort'] = self.hostPort
        creatorParams['popularHostFraction'] = self.popularHostFraction
        creatorParams['popularHostLoadFraction'] = self.popularHostLoadFraction
        creatorParams['userPopulation'] = self.userPopulation
        creatorParams['meanThinkTime'] = self.meanThinkTime

        # add in the generator parameters to the creator parameters
        creatorParams['generatorParameters'] = \
            self.generatorParameters.to_json()

        # Add profile creator params to top-level dictionary
        dict['profilesCreatorClassParams'] = creatorParams
        # sub map with timing info
        timing = {}
        timing['rampUp'] = self.rampUp
        timing['duration'] = self.duration
        timing['rampDown'] = self.rampDown
        # Add timing info to top-level dictionary
        dict['timing'] = timing
        return dict

class PredictableAppTestRunner:
    def create_dir( self, path ):
        if not os.path.exists( path ):
            os.mkdir( path )

    def run( self, start_ip, num_apps_to_load, apps_powered_on,\
             host_port, popular_host_fraction, popular_host_load_fraction,\
             user_population, operation_work_done, operation_mix, \
             operation_busy_pct, memory_sizes, memory_mix, \
             mean_think_time=0,  \
             results_dir="./results", run_duration_secs=60, \
             config_dir="./config" ):
        '''
        Given a starting IP, a step size, e.g.,
        1) run servers on ip addressed 11.0.0.1 - 11.0.0.200
        2) with a step size of 10 run experiments on 11.0.0.1 - 10
           11.0.0.1 - 20, ... 11.0.0.1 - 200   
        '''
        # Some pre-reqs:
        # 1) create the config_dir if it doesn't exist
        # 2) create the results_dir if it doesn't exist
        self.create_dir( config_dir )
        self.create_dir( results_dir )
        
        num_tests = apps_powered_on/num_apps_to_load
        for i in range(num_tests):
            # With a single Rain launch, load an entire block of ip's
            config = PreditableAppTestConfig()
            config.baseHostIp = start_ip
            config.numHostTargets = (i+1)*num_apps_to_load
            config.duration = run_duration_secs
            config.hostPort = host_port
            config.popularHostFraction = popular_host_fraction
            config.popularHostLoadFraction = popular_host_load_fraction
            config.userPopulation = user_population
            config.meanThinkTime = mean_think_time
            # Add in the parameters for the workload generator
            # the operation mixes etc.
            generatorParams = PredictableAppGeneratorParameters()
            generatorParams.operationWorkDone = operation_work_done
            generatorParams.operationMix = operation_mix
            generatorParams.operationBusyPct = operation_busy_pct
            generatorParams.memorySizes = memory_sizes
            generatorParams.memoryMix = memory_mix
            config.generatorParameters = generatorParams
            
            json_data = \
                json.dumps(config, sort_keys='True',\
                               default=PreditableAppTestConfig.to_json)
            # Write this data out to a file, then invoke the run mananger
            # passing in the path to this file
                                  
            print( "[PredictableAppTestRunner] json config: {0}"\
                       .format(json_data) )

            run_classpath=".:rain.jar:workloads/httptest.jar"
            run_config_filename = config_dir + "/" + \
                "run_predictable_config_" + start_ip + "_" + \
                str(config.numHostTargets) + "_nodes.json"
            run_output_filename = results_dir + "/" + \
                "run_predictable_log_" + start_ip + "_" + \
                str(config.numHostTargets) + "_nodes.txt"
            run_results_filename = results_dir + "/" + \
                "run_predictable_result_" + start_ip + "_" + \
                str(config.numHostTargets) + "_nodes.txt"
            
            # write the json data out to the config file
            # invoke the run manager passing the location of the config file
            # collect the results and write them out to the results_dir
         
            print "[PredictableAppTestRunner] Writing config file: {0}"\
                .format( run_config_filename )
            config_file = open( run_config_filename, 'w' )
            config_file.write( json_data )
            config_file.flush()
            config_file.close()
            run_output = RunManager.run_rain( run_config_filename,\
                                               run_classpath )
            #print run_output
            track_results = RainOutputParser.parse_output( run_output )
            # Validate each of the track_results instances
            
            for result in track_results:
                # Set some 90th and 99th pctile thresholds
                result.pct_overhead_ops_threshold=10.0
                result.pct_failed_ops_threshold=5.0
                # Set the desired 90th and 99th percentile thresholds for
                # the 50ms, 100ms, 200ms operations - set everything to
                # 500 ms = 0.5s. Threshold units = seconds
                result.op_response_time_thresholds['PredicatableOp_50']=\
                    (0.5,0.5)
                result.op_response_time_thresholds['PredicatableOp_100']=\
                    (0.5,0.5)
                result.op_response_time_thresholds['PredicatableOp_200']=\
                    (0.5,0.5)

            # Write out the run output
            print "[PredictableAppTestRunner] Writing output: {0}"\
                .format( run_output_filename )
            run_output_file = open( run_output_filename, 'w' )
            run_output_file.write( run_output )
            run_output_file.flush()
            run_output_file.close()

            # Write out the run results
            print "[PredictableAppTestRunner] Writing results: {0}"\
                .format( run_results_filename )
            run_results_file = open( run_results_filename, 'w' )
            RainOutputParser.print_results( track_results, run_results_file )
            
            run_results_file.write( "\n" )
            # After writing out the table for all the tracks
            # Spit out the 90th and 99th percentiles
            for result in track_results:
                for k,v in result.op_response_times.items():
                    run_results_file.write( "{0},{1},{2},{3}\n"\
                               .format(result.name, k, v[0], v[1]) )

            run_results_file.flush()
            run_results_file.close()

def usage():
    print( "Usage: {0} [--startip <ipv4 address>] [--numapps <#apps to load>]"\
           " [--maxapps <#apps powered on>] [--resultsdir <path>]"\
           " [--duration <seconds to run>] [--configdir <path>]"\
           " [--port <host port>] [--popularhosts <%popular hosts>]"\
           " [--popularload <%load for popular hosts>] [--users <#users>]"\
           " [--thinktime <secs>] [--opwork <comma separated list>]"\
           " [--opmix <command separated list>]"\
           " [--opbusy <comma separated list>]"\
           .format(sys.argv[0]) )

    print "\n"
    print( "defaults: {0} --startip 127.0.0.1 --numapps 10"\
           " --maxapps 10 --resultsdir ./results --duration 60"\
           " --configdir ./config --port 8080 --popularhosts 0.2"\
           " --popularload 0.5 --users 100 --thinktime 5 --opwork 50,100,200"\
           " --opmix 30,30,40 --opbusy 50,75,75"\
           " --memsize 'small','med','large' --memmix 10,20,70"\
           .format(sys.argv[0]) )


def main(argv):
    start_ip = "127.0.0.1"
    num_apps_to_load = 10
    apps_powered_on = 10
    results_dir = "./results"
    run_duration = 60
    config_dir = "./config"
    
    host_port = 8080
    popular_host_fraction = 0.2
    popular_host_load_fraction = 0.5
    user_population = 100
    mean_think_time = 5

    # generator parameters - how should these be specified at the cmd line?
    operation_work_done = [50, 100, 200]
    operation_mix = [30, 30, 40]
    operation_busy_pct = [50, 75, 75]
    memory_sizes = [ "small", "med", "large" ]
    memory_mix = [10, 20, 70]

    # parse arguments and replace the defaults
    try:
        opts, args = getopt.getopt( argv, "h", ["startip=", "numapps=",\
                                         "maxapps=", "resultsdir=",\
                                          "duration=", "configdir=",\
                                          "help", "port=",\
                                           "popularhosts=",\
                                           "popularload=",\
                                           "users=",\
                                           "thinktime=", "opwork=",\
                                           "opmix=", "opbusy=",\
                                           "memsize=", "memmix="] )
    except getopt.GetoptError:
            print sys.exc_info()
            usage()
            sys.exit(2)

    for opt, arg in opts:
        if opt in ( "-h", "--help" ):
            usage()
            sys.exit()
        elif opt == "--startip":
            start_ip = arg
        elif opt == "--numapps":
            num_apps_to_load = int(arg)
        elif opt == "--maxapps":
            apps_powered_on = int(arg)
        elif opt == "--resultsdir":
            results_dir = arg
        elif opt == "--duration":
            run_duration = int(arg)
        elif opt == "--configdir":
            config_dir = arg
        elif opt == "--port":
            host_port = int(arg)
        elif opt == "--popularhostfraction":
            popular_host_fraction = float(arg)
        elif opt == "--popularloadfraction":
            popular_host_load_fraction = float(arg)
        elif opt == "--users":
            user_population = int(arg)
        elif opt == "--thinktime":
            mean_think_time = float(arg)
        elif opt == "--opwork":
            opwork = []
            for work in arg.split( "," ):
                opwork.append( int(work) )
            operation_work_done=opwork
        elif opt == "--opmix":
            opmix = []
            for mix in arg.split( "," ):
                opmix.append( int(mix) )
            operation_mix = opmix
        elif opt == "--opbusy":
            opbusy = []
            for busy in arg.split( "," ):
                opbusy.append( int(busy) )
            operation_busy_pct = opbusy
        elif opt == "--memsize":
            memsize = []
            for size in arg.split( "," ):
                memsize.append( size )
            memory_sizes = memsize
        elif opt == "--memmix":
            memmix = []
            for mix in arg.split( "," ):
                memmix.append( int(mix) )
            memory_mix = memmix
    
    # launch run
    test_runner = PredictableAppTestRunner()
    test_runner.run( start_ip, num_apps_to_load, apps_powered_on,\
             host_port, popular_host_fraction, popular_host_load_fraction,\
             user_population, operation_work_done, operation_mix, \
             operation_busy_pct, memory_sizes, memory_mix, \
             mean_think_time, results_dir, run_duration, \
             config_dir )

if __name__=='__main__':
    # Pass all the arguments we received except the name of the script
    # argv[0]
    main( sys.argv[1:] )
