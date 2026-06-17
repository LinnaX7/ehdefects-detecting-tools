import PolicyGenerator
import Logger
if __name__ == '__main__':
    # Test 1 policygenerator
    # pg = PolicyGenerator.PolicyGenerator(tri_gap=2,tri_exception='java.io.IOException')
    # pg.set_keyword("gpslogger")
    # result = pg.generate_throughout_patterns(running_log_file='/Users/lulu/fixheh_project/GPSLoggerOutput/77/format-out.out',
    #                                 final_policy='/Users/lulu/fixheh_project/GPSLoggerOutput/77/fixeh-policy.xml',
    #                                 )
    # list = pg.get_pattern(5)
    # print(pg.compose_througout_method_pattern(elements=result[0],pattern=list[5]))

    # Test 2 logger
    mylogger = Logger.Logger(output_dir='/Users/lulu/Documents/codecoverage/tools4analyze/test_result',
                             app_package_name='gpslogger')
    mylogger.generate_log_file(triggerException='java.io.IOException')


