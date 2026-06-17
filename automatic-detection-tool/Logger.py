import subprocess
import os
#import psutil
import time

class Logger(object):
    def __init__(self, output_dir, app_package_name):
        self.output_dir = output_dir
        self.temp_output = os.path.join(output_dir, "temp.out")
        self.ERROR_output = os.path.join(output_dir,'ERROR.out')
        self.output = os.path.join(output_dir, "format-out.out")
        self.simple_output = os.path.join(output_dir,"simple-out.out")
        self.triggering_out = os.path.join(output_dir,"trigger.log")
        self.crash_out = os.path.join(output_dir,"crash-stack.log")
        self.logger = None
        self.app_package_name = app_package_name
        self.tag = os.path.split(output_dir)[1]

    def begin_log(self):
        log_cleaner = subprocess.Popen(['adb', 'logcat', '-c'], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = log_cleaner.communicate()
        if stderr is not None and len(stderr) != 0:
            print('error clean log with message: %s' % stderr.decode('gb2312'))
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)
        with open(self.temp_output, "w+") as fps:
            self.logger = subprocess.Popen(["adb","shell","logcat"],stdout=fps, stderr=fps)
        #with open(self.temp_output, "w+") as fps:
        #    self.logger = subprocess.Popen(["adb","shell","logcat","fixeh:D","*:E"],stdout=fps, stderr=fps)

    def begin_filter_log(self):
        log_cleaner = subprocess.Popen(['adb', 'logcat', '-c'], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = log_cleaner.communicate()
        if stderr is not None and len(stderr) != 0:
            print('error clean log with message: %s', stderr.decode('gb2312'))
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)
        with open(self.temp_output, "w+") as fps:
            # self.logger = subprocess.Popen("adb shell logcat grep -E \"^..fixeh |^..%s\"" % self.app_package_name,stdout=fps, stderr=fps)
            self.logger = subprocess.Popen(["adb", "shell", "logcat", "fixeh:D", "*:E"], stdout=fps, stderr=fps)



    #def close_old(self):
        #parent = psutil.Process(self.logger.pid)
        #children = parent.children(recursive=True)
        #for child in children:
        #    child.kill()
        #gone, still_alive = psutil.wait_procs(children,timeout=5)
        #parent.kill()
        #parent.wait(5)

    def close(self):
        l_pid=self.logger.pid
        self.logger.terminate()
        time.sleep(5)
        #if psutil.pid_exists(l_pid):
            #self.close_old()

    def get_pid(self):
        pid_catcher = subprocess.Popen(['adb','shell', 'ps', '|','grep', self.app_package_name],
                                       stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr)=pid_catcher.communicate()
        if stderr is not None and len(stderr) != 0:
            print("fail to get pid with info: %s" % stderr.decode('gb2312'))
            return None
        pid = stdout.decode('utf-8').split()[1]
        return pid

    def generate_log_file(self,triggerException,pid=None):
        crashstack = []
        with open(self.temp_output,'r',encoding='gb2312',errors='ignore') as temp_fps:
            while True:
                line = temp_fps.readline()
                if 'fixeh' in line:
                    items = line.split(' ')
                    if items is None or len(items) < 2:
                        continue
                    for item in items:
                        if item is None:
                            continue
                        if len(item) == 0:
                            continue
                        if ':' in item:
                            continue
                        if '-' in item:
                            continue
                        pid = item
                        break
                    if pid != None:
                        break
                if not line:
                    print('fail to get pid!')
                    exit(2)
        with open(self.temp_output, 'r', encoding='gb2312',errors='ignore') as fps , open(self.output, 'a+') as out_fps,\
                open(self.simple_output,'a+') as sim_fps, open(self.triggering_out,'a+') as tri_fps:
            # trigger_stack = False
            trigger_exception = False
            str_line = ''
            triggerStack = ''
            for line in fps.readlines():
                if pid in line:
                    out_fps.write(line)
                    if "fixeh" in line and pid in line:
                        sim_fps.write(line)
                        if ' current ' in line and triggerException in line:
                            triggerStack = ''
                            str_line = ''
                        elif 'XMLCONTROLLER::' in line and triggerException in line:
                            str_line += line
                            # trigger_stack = False
                            # trigger_exception = True
                        elif '\tat ' in line:
                            triggerStack += line
                        elif 'triggering' in line:
                            str_line += line
                            str_line += triggerStack
                            tri_fps.write(str_line)
                            # trigger_exception = False
                if "E AndroidRuntime" in line:
                    if pid in line:
                        crashstack.append(line)
                        continue
                if 'unexpected EOF!' in line:
                    with open(self.ERROR_output, 'a+') as err_fps:
                        err_fps.write("ERROR:: EOF!")
            if len(crashstack) > 0:
                with open(self.crash_out,'a+') as cra_fps:
                    cra_fps.writelines(crashstack)
                crashstack.clear()
        os.remove(self.temp_output)