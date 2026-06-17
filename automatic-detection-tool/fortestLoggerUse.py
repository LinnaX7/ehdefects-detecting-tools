import Logger

if __name__  == '__main__':
    crashstack = []
    trigger_stack = False
    trigger_exception = False
    str_line = ""
    triggerException = 'java.net.UnknownHostException'
    stopCrash = False
    temp_output = '/home/lulu/Documents/fixheh_project/GPSLoggerOutput4/1/format-out.out'
    pid = None
    with open(temp_output, 'r', encoding='utf-8') as temp_fps:
        while True and pid == None:
            line = temp_fps.readline()
            if 'fixeh' in line:
                pid = line.split(' ')[2]
                break
            if not line:
                print('fail to get pid!')
                exit(2)
    with open(temp_output,'r',encoding='utf-8') as fps:
        triggerStack = ''
        for line in fps.readlines():
            if "fixeh" in line and pid in line:
                if ' current ' in line and triggerException in line:
                    triggerStack = ''
                    triggerStack += line
                    trigger_stack = True
                elif trigger_stack and 'XMLCONTROLLER::' in line and triggerException in line:
                    triggerStack += line
                    trigger_stack = False
                    trigger_exception = True
                elif trigger_stack and '\tat ' in line:
                    triggerStack += line
                elif trigger_exception and 'triggering' in line:
                    str_line += line
                    str_line += triggerStack
                    trigger_exception = False
                        # trigger_stack = not trigger_stack
            if "beginning of crash" in line:
                if pid in line:
                    crashstack.append(line)
                continue
            if len(crashstack) != 0:
                if "AndroidRuntime" in line and not stopCrash:
                    if pid in line:
                        crashstack.append(line)
                else:
                    stopCrash = True
    print(str_line)