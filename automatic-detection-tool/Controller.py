# coding=utf-8

import PolicyGenerator
import getopt
import sys
import os
import subprocess
import shutil
from time import strftime, gmtime


# python Controller.py -e java.io.IOException -t XXXX(output dir)  -T amaze.py(your own test case) -A amaze(your own package keyword)

def usage():
    pass


def conduct_appium_test(current_log, current_policy_file, dest_dir,
                        testcase, apppackage, current_test_dir, tri_exception):
    # current_log = <CURRENT TEST DIR>/LOG.log : log when error happens
    # current_policy_file = <CURRENT TEST DIR>/fixeh-policy.xml : policy file need to be pushed
    # dest_dir = /data/local/tmp

    # push policy
    pusher = subprocess.Popen(['adb', 'push', current_policy_file, dest_dir], stdout=subprocess.PIPE,
                              stderr=subprocess.PIPE)
    (stdout, stderr) = pusher.communicate()
    # if stderr is not None and len(stderr) is not 0:
    #    print('wrong push fixeh-policy.xml')
    #    exit(4) #stands for something is error whild call subprocess

    with open(current_log, 'w') as l_fps:
        appium_conductor = subprocess.Popen(
            ['python', testcase, '-p', apppackage, '-b', current_test_dir, '-t', tri_exception], stdout=l_fps,
            stderr=l_fps)
        appium_conductor.communicate()

    if not os.path.exists(current_log):
        print('end error at %d turn' % test_id)
        current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        print(current_time)

    print('end error at %d turn' % test_id)
    current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
    print(current_time)


if __name__ == "__main__":

    options, args = getopt.getopt(sys.argv[1:], 'he:p:c:m:P:C:M:g:t:T:A:',
                                  ['help', 'excepiton=', 'package=', 'class=', 'method=',
                                   'filter_package=', 'filter_class=', 'filter_method=',
                                   'tri_gap=', 'test_dir=', 'testcase=', 'apppackage='])
    policy_file = 'fixeh-policy.xml'
    dest_dir = '/data/local/tmp'
    result_out = 'result.out'
    filter_dir = 'Filter'
    throughout_dir = 'Throughout'
    # crash_log = 'crash-stack.log'
    # appium_log = 'err.out'
    triggering_log = 'trigger.log'
    final_log = 'LOG.log'
    tri_gap = None
    tri_exception = None
    tri_package = None
    tri_class = None
    tri_method = None
    filter_package = None
    filter_class = None
    filter_method = None
    test_dir = None
    testcase = None
    apppackage = None

    try:
        for name, value in options:
            if name in ('-h', '--help'):
                usage()
            if name in ('-e', '--exception'):
                tri_exception = value
            if name in ('-p', '--package'):
                tri_package = value
            if name in ('-c', '--class'):
                tri_class = value
            if name in ('-m', '--method'):
                tri_method = value
            if name in ('-P', '--filter_package'):
                filter_package = value
            if name in ('-C', '--filter_class'):
                filter_class = value
            if name in ('-M', '--filter__method'):
                filter_method = value
            if name in ('-g', '--tri_gap'):
                tri_gap = value
            if name in ('-t', '--test_dir'):
                test_dir = value
            if name in ('-T', '--testcase'):
                testcase = os.path.join(os.getcwd(), value)
            if name in ('-A', '--apppackage'):
                apppackage = value
    except getopt.GetoptError:
        usage()
        exit(30)  # get wrong opt parameter

    flag = True
    # test_dir = os.path.join(os.getcwd(),test_dir)
    if not os.path.exists(test_dir):
        os.makedirs(test_dir)
    assert (os.path.exists(test_dir))
    throughout_dir = os.path.join(test_dir, throughout_dir)
    filter_dir = os.path.join(test_dir, filter_dir)
    last_policy = None
    policygenerator = PolicyGenerator.PolicyGenerator(tri_exception=tri_exception, tri_method=tri_method,
                                                      tri_class=tri_class, tri_package=tri_package, tri_gap=tri_gap)
    policygenerator.set_keyword(apppackage)

    test_id = 0

    while flag:
        # if test_id > 3:
        #     break
        # break
        current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        print(current_time)
        print('start at %d turn' % (test_id))
        current_test_dir = os.path.join(filter_dir, str(test_id))
        if not os.path.exists(current_test_dir):
            os.makedirs(current_test_dir)

        current_log = os.path.join(current_test_dir, final_log)
        break
        if test_id == 0:
            last_policy = policygenerator.generate_none_error_policy()
        elif test_id == 1:
            last_policy = policygenerator.generatord_all_trigger_policy()
        else:
            last_path = os.path.join(filter_dir, str(test_id - 1))
            log_file = os.path.join(last_path, final_log)
            triggering_log_file = os.path.join(filter_dir, str(test_id - 1), triggering_log)
            flag = policygenerator.analyze_appium_error(appium_err_out_file=log_file)
            if not flag:
                result_file = None
                dirs = os.listdir(last_path)
                for item in dirs:
                    if os.path.isdir(os.path.join(last_path, item)):
                        if os.path.exists(os.path.join(last_path, item, result_out)):
                            result_file = os.path.join(last_path, item, result_out)
                if result_file != None:
                    flag = policygenerator.analyze_result_error(result_file=result_file)

            if not flag:
                print('end error at %d turn' % (test_id - 1))
                break
            # otherwise if flag
            policygenerator.analyze_trigger_log(trigger_file=triggering_log_file)
            last_err_path = os.path.join(test_dir, str(test_id - 1), 'ERROR.out')
            if os.path.exists(last_err_path):
                print('end error at %d turn' % (test_id - 1))
                print("unexpected Error, details in %s" % (last_err_path))
                exit(5)
            # policygenerator.analyze_carsh_log(analyze_file=log_file, outformat_file=format_output)
            if last_policy is None:
                last_dir = os.path.join(filter_dir, str(test_id - 1))
                last_policy_file = os.path.join(last_dir, policy_file)
                with open(last_policy_file, 'r', errors='ignore') as lfps:
                    last_policy = lfps.read()
            last_policy = policygenerator.generator_increasement_methodfilters_policy(last_policy=last_policy)
        # copy to file
        current_policy_file = os.path.join(current_test_dir, policy_file)
        with open(current_policy_file, 'w') as fps:
            fps.writelines(last_policy)
        conduct_appium_test(current_log=current_log, current_policy_file=current_policy_file,
                            dest_dir=dest_dir, testcase=testcase, appackage=apppackage,
                            current_test_dir=current_test_dir, tri_exception=tri_exception)
        test_id += 1

    print("END OF FILTER MODE BEGIN THROUGHOUT MODE\n")

    # policygenerator.set_keyword(apppackage)
    # get Initail
    patterns = policygenerator.get_pattern(2 * int(tri_gap) + 1)
    max = -1
    if not os.path.exists(throughout_dir):
        os.makedirs(throughout_dir)
    if not os.path.exists(filter_dir):
        print("Error:: Filter Dir Not Found!")
        exit(1)
    for name in os.listdir(filter_dir):
        if name.isdigit():
            current = int(name)
            if max < current:
                max = current
    max_file = os.path.join(filter_dir, str(max))
    max_policy = os.path.join(max_file, policy_file)
    if not os.path.exists(max_policy):
        max_policy = os.path.join(filter_dir, str(max - 1), policy_file)
    if not os.path.exists(max_policy):
        print("ERROR GET FILTERMODE POLICY")
        exit(-1)
    initial_dir = os.path.join(filter_dir, '0')
    initial_log_file = os.path.join(initial_dir, 'format-out.out')
    if not os.path.exists(initial_log_file):
        print("ERROR GET FILTERMODE INIT LOG")
        exit(-1)
    pattern_elements = policygenerator.generate_throughout_patterns(running_log_file=initial_log_file,
                                                                    final_policy=max_policy)
    if pattern_elements == None:
        print("END OF THROUGHOUT MODE WITH NO PATTERN\n")
        exit(-2)

    left_invocations_file = os.path.join(throughout_dir, "left_invocation.log")
    left_invocations = policygenerator.get_left_invocations()
    if left_invocations is not None and len(left_invocations) > 0:
        with open(left_invocations_file, 'a+') as ips:
            ips.writelines(policygenerator.get_left_invocations())

    initial_test_dir = initial_dir
    current_initial_dir = os.path.join(throughout_dir, '0')
    if not os.path.exists(current_initial_dir):
        shutil.copytree(initial_test_dir, current_initial_dir)
    test_id = 1
    for pattern_element in pattern_elements:
        for pattern in patterns:
            if test_id == 9:
                current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
                print(current_time)
                print('start at %d turn' % (test_id))
                current_test_dir = os.path.join(throughout_dir, str(test_id))
                if not os.path.exists(current_test_dir):
                    os.makedirs(current_test_dir)
                current_policy_file = os.path.join(current_test_dir, policy_file)
                current_log = os.path.join(current_test_dir, final_log)
                with open(current_policy_file, 'w') as fps:
                    current_policy = policygenerator.compose_througout_method_pattern(elements=pattern_element,
                                                                                      pattern=pattern)
                    fps.write(current_policy)
                conduct_appium_test(current_log=current_log, current_policy_file=current_policy_file,
                                    dest_dir=dest_dir, testcase=testcase, appackage=apppackage,
                                    current_test_dir=current_test_dir, tri_exception=tri_exception)
            test_id += 1

    print("END OF THROUGHOUT MODE WITH PATTERNS\n")
