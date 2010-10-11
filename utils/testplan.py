import sys
import subprocess
import time
import re
import os
import simplejson as json
import getopt
from run_manager import RunManager, RainOutputParser

'''
Example config
{
    "profilesCreatorClass": "radlab.rain.workload.httptest.HttpTestProfileCreator",
    "profilesCreatorClassParams": {
        "baseHostIp": "127.0.0.1",
        "numHostTargets": 5
    },
    "timing": {
        "rampUp": 10,
        "duration": 60,
        "rampDown": 10
    }
}
'''

class HttpTestConfig:
    '''
    Rain configuration object for the Http tests
    '''
    def __init__(self):
        # profile creator to use
        self.profilesCreatorClass = \
            "radlab.rain.workload.httptest.HttpTestProfileCreator"
        # profile creator params
        self.baseHostIp = "127.0.0.1"
        self.numHostTargets = 5
        # timing details
        self.rampUp = 10
        self.duration = 60
        self.rampDown = 10
    
    def to_json( self ):
        dict = {}
        dict['profilesCreatorClass'] = self.profilesCreatorClass
        # sub-map with profile creator parameters
        creatorParams = {}
        creatorParams['baseHostIp'] = self.baseHostIp
        creatorParams['numHostTargets'] = self.numHostTargets
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

class HttpTestStepRunner:
    def create_dir( self, path ):
        if not os.path.exists( path ):
            os.mkdir( path )

    def step_run( self, start_ip, num_apps_to_load, apps_powered_on, \
                      results_dir="./results", run_duration_secs=60, \
                      config_dir="./config" ):
        '''
        Given a starting IP, a step size

        e.g.,:
        1) run servers on ip addressed 11.0.0.1 - 11.0.0.200
        2) with a step size of 10 run experiments on 11.0.0.1 - 20
           11.0.0.21 - 40, ... 10.0.0.191 - 200
        '''

        # Some pre-reqs:
        # 1) create the config_dir if it does not exist
        # 2) create the results_dir if it does not exist
        self.create_dir( config_dir )
        self.create_dir( results_dir )

        num_tests = apps_powered_on/num_apps_to_load
        for i in range(num_tests):
            # with one Rain launch we can load an entire block of ip's
            # using the track feature
            ip_address_parts = start_ip.split( "." )
            #print len(ip_address_parts)
            # throw exception if we don't find a numeric ip v4 address
            if len(ip_address_parts) != 4:
                raise Exception( "Expected a numeric IPv4 address"\
                                     + " (format N.N.N.N)" )
            lastOctet = int( ip_address_parts[3] )
            base_ip = "{0}.{1}.{2}.{3}".format( ip_address_parts[0],\
                                    ip_address_parts[1],\
                                    ip_address_parts[2],\
                                    str(lastOctet+(num_apps_to_load*i)))

            # Create config objects to write out as files
            config = HttpTestConfig()
            config.baseHostIp = base_ip
            config.numHostTargets = num_apps_to_load
            config.duration = run_duration_secs
            
            json_data = \
                json.dumps(config, sort_keys='True',\
                               default=HttpTestConfig.to_json)
            # Write this data out to a file, then invoke the run mananger
            # passing in the path to this file
            print( "[HttpTestStepRunner] json config: {0}".format(json_data) )
            
            run_classpath=".:rain.jar:workloads/httptest.jar"
            run_config_filename = config_dir + "/" + \
                "run_config_" + base_ip + "_" + \
                str(config.numHostTargets) + "_nodes.json"
            run_output_filename = results_dir + "/" + \
                "run_log_" + base_ip + "_" + \
                str(config.numHostTargets) + "_nodes.txt"
            run_results_filename = results_dir + "/" + \
                "run_result_" + base_ip + "_" + \
                str(config.numHostTargets) + "_nodes.txt"
            
            # write the json data out to the config file
            # invoke the run manager passing the location of the config file
            # collect the results and write them out to the results_dir
            config_file = open( run_config_filename, 'w' )
            config_file.write( json_data )
            config_file.flush()
            config_file.close()
            run_output = RunManager.run_rain( run_config_filename,\
                                               run_classpath ) 
            #print run_output
            track_results = RainOutputParser.parse_output( run_output )
            
            # Write out the run output
            print "[HttpTestStepRunner] Writing output: {0}"\
                .format( run_output_filename )
            run_output_file = open( run_output_filename, 'w' )
            run_output_file.write( run_output )
            run_output_file.flush()
            run_output_file.close()
            
            # Write out the run results
            print "[HttpTestStepRunner] Writing results: {0}"\
                .format( run_results_filename )
            run_results_file = open( run_results_filename, 'w' )
            RainOutputParser.print_results( track_results, run_results_file )
            run_results_file.flush()
            run_results_file.close()

def usage():
    print( "Usage: {0} [--startip <ipv4 address>] [--numapps <#apps to load>]"\
           " [--maxapps <#apps powered on>] [--resultsdir <path>]"\
           " [--duration <seconds to run>] [--configdir <path>]"\
               .format(sys.argv[0]) )
    print( "defaults: {0} --startip 127.0.0.1 --numapps 10"\
           " --maxapps 100 --resultsdir ./results --duration 60"\
           "--configdir ./config".format(sys.argv[0]) )

def main(argv):
    start_ip = "127.0.0.1"
    num_apps_to_load = 10
    apps_powered_on = 100
    results_dir = "./results"
    run_duration = 60
    config_dir = "./config"

    # parse aguments and replace the defaults
    try:
        opts, args = getopt.getopt( argv, "h", ["startip=", "numapps=", \
                                                "maxapps=", "resultsdir=", \
                                                "duration=", "configdir=" \
                                                "help"] )
    except getopt.GetoptError:
            usage()
            sys.exit(2)
    
    for opt, arg in opts:
        if opt in ( "-h", "--help" ):
            usage()
            sys.exit()
        elif opt == "--startip":
            start_ip = arg
        elif opt == "--numapps":
            num_apts_to_load = int(arg)
        elif opt == "--maxapps":
            apps_powered_on = int(arg)
        elif opt == "--resultsdir":
            results_dir = arg
        elif opt == "--duration":
            run_duration = int(arg)
        elif opt == "--configdir":
            config_dir = arg

    test_runner = HttpTestStepRunner()
    test_runner.step_run( start_ip, num_apps_to_load, \
                              apps_powered_on, results_dir, run_duration, \
                              config_dir )
if __name__=='__main__':
    main( sys.argv[1:] )
