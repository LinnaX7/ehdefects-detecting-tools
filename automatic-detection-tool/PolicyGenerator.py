import copy
import os

class PolicyGenerator(object):
    __stack_keyword = None
    __filter_method = None
    __filter_class = None
    __filter_package = None
    __filter_stackkeyword = None
    __fixeh_head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" \
                      "<fixeh>\n" \
    "   <remote-controller enable=\"false\" address=\"127.0.0.1\" port=\"7675\"/>\n" \
    "   <policy exclude=\"false\" search=\"false\" limit=\"-1\">\n"
    # <policyentry kind = "exception" filtermode = "0" value = "java.io.IOException"/>
    #         <policyentry kind = "method" filtermode = "1" value = "java.io.File&#58; java.io.File getCanonicalFile()"/>
    #         <policyentry kind = "method" filtermode = "1" value = "java.io.File&#58; java.lang.String getCanonicalPath()"
    #         stack = " com.amaze.filemanager.filesystem.FileUtil.getExtSdCardFolder&#64;&#64;
    #         com.amaze.filemanager.filesystem.FileUtil.isOnExtSdCard"/>
    __active_method_format = '      <policyentry kind = \"method\" filtermode = \"%d\" value = \"%s\" %s/>\n'
    __active_class_format = '      <policyentry kind = \"class\" filtermode = \"%d\" value = \"%s\" %s/>\n'
    __active_package_format = '      <policyentry kind = \"package\" filtermode = \"%d\" value = \"%s\" %s/>\n'
    __active_exception_format = '      <policyentry kind = \"exception\" filtermode = \"%d\" value = \"%s\" %s/>\n'
    __active_stacktrace_format = '      <policyentry kind = \"stacktrace\" filtermode = \"%d\" value = \"%s\"/>\n'
    __stack_format = 'stack =\"%s\"'
    __pattern_format = 'pattern=\"%s\"'
    __fixeh_end = "</policy>\n" \
    "</fixeh>"
    __exception = None
    __method = None
    __class = None
    __package = None
    __trigger_gap = -1
    __left_invocations = None

    def __init__(self,tri_exception=None, tri_method=None, tri_class=None,
                 tri_package=None, tri_gap=-1):
        self.__exception = tri_exception
        self.__method = tri_method
        self.__class = tri_class
        self.__package = tri_package
        self.__trigger_gap = int(tri_gap)

    def set_keyword(self,keyword):
        self.__stack_keyword = keyword

    def generate_none_error_policy(self):
        return self.__fixeh_head + (self.__active_exception_format % (0, "NOSUCHException","")) + self.__fixeh_end

    # from crash-stack.log get stackkeywords

    def convert_stacktrace_to_keyword_string(self, lines):
        res_string = lines[0]
        for i in range(len(lines)):
            if i < 1:
                continue
            if i < 2:
                res_string += '&#64;&#64;'
                res_string += lines[i]
            else:
                if self.__stack_keyword in lines[i]:
                    res_string += '&#64;&#64;'
                    res_string += lines[i]
        return res_string

    def convert_stackstring_to_keyword_string(self, stack_string):
        lines = stack_string.split('@@')
        return self.convert_stacktrace_to_keyword_string(lines=lines)

    #just for trigger excepiton mode:
    def generatord_all_trigger_policy(self):
        if self.__exception is None:
            print('triggerde Exception must be given!')
            exit(2) #2 stands for try to generate policy without key information
        #'<policyentry kind = \"exception\" filtermode = \"%d\" value = \"%s\" %s/>\n'
        return self.__fixeh_head + (self.__active_exception_format % (0, self.__exception, "")) + self.__fixeh_end

    #every time only one method can be added
    def generator_increasement_methodfilters_policy(self,last_policy):
        policy = ''
        self.__filter_method = self.__filter_method.replace('<','&lt;').replace('>','&gt;')
        appear = False
        for line in last_policy.split('\n'):
            if 'policyentry' in line and not appear:
                if self.__filter_method is not None:
                    stackkeyword = ''
                    if self.__stackkeyword is not None:
                        # 'stack =\"%s\"'
                        stackkeyword = self.__stack_format % (self.__stackkeyword)
                    # <policyentry kind = \"method\" filtermode = \"%d\" value = \"%s\" %s/>\n
                    # filtermode = 1
                    policy += self.__active_method_format % (1, self.__filter_method, stackkeyword)
                    appear = True
            policy += line + '\n'
        return policy

    def analyze_appium_error(self,appium_err_out_file):
        error = False
        if not os.path.exists(appium_err_out_file):
            return error
        with open(appium_err_out_file, 'r', encoding='gb2312', errors='ignore') as fps:
            if 'Traceback' in fps.read():
                error = True
        return error

    def analyze_result_error(self,result_file):
        error = False
        with open(result_file, 'r', encoding='gb2312', errors='ignore') as fps:
            if ' False' in fps.read():
                error = True
        return error

    def analyze_trigger_log(self,trigger_file):
        lines = []
        method = None
        # stack_appeared = False
        with open(trigger_file, 'r', encoding='gb2312', errors='ignore') as tf_fps:
            while True:
                line = tf_fps.readline()
                if not line:
                    break
                if 'XMLCONTROLLER' in line:
                    lines.clear()
                    continue
                if 'triggering exception on' in line:
                    method = line.split('triggering exception on')[1].strip()
                if '\tat ' in line:
                    current = line.split('\tat ')[1].split('(')[0].strip()
                    lines.append(current)
        if method is None or len(lines) == 0:
            print('wrong get trigger_file key value')
            exit(2)
        self.__stackkeyword = self.convert_stacktrace_to_keyword_string(lines=lines)
        self.__filter_method = method

    def generate_throughout_patterns(self, running_log_file, final_policy):
        final_results = [] #[[methodformat]]
        lists = {}
        with open(final_policy, 'r', encoding='gb2312', errors='ignore') as fps:
            lines = fps.readlines()
            for line in lines:
                if 'policyentry' in line:
                    if'method' in line:
                        #'<policyentry kind = \"method\" filtermode = \"%d\" value = \"%s\" %s/>\n'
                        items = line.split('\"')
                        if len(items) < 8:
                            print("Wrong policyEntry: %s" % line)
                        method = items[5]
                        stacktrace = items[7]
                        if method not in lists.keys():
                            lists[method] = []
                        lists[method].append(stacktrace)
        # first loop
        with open(running_log_file,'r', encoding='gb2312', errors='ignore') as fps:
            final_results = []
            # <method,stackkeyword>
            invocationTrace = []
            exception_appear = False
            current_invocation = []
            while(True):
                line = fps.readline()
                if not line:
                    break
                if 'fixeh' in line:
                    if 'current stack is' in line:
                        if self.__exception in line:
                            exception_appear = True
                        if len(current_invocation) > 1:
                            length = len(invocationTrace)
                            if length > 0:
                                if invocationTrace[length - 1][1] == current_invocation[1] \
                                        and invocationTrace[length - 1][0] == current_invocation[0]:
                                    current_invocation.clear()
                                    continue
                            invocationTrace.append([])
                            invocationTrace[length].append(current_invocation[0])
                            invocationTrace[length].append(current_invocation[1])
                            current_invocation.clear()
                            continue
                    if '\tat ' in line:
                        if exception_appear:
                            if len(current_invocation) == 0:
                                current_invocation.append("")
                                current_invocation.append("")
                            if len(current_invocation[1]) > 2:
                                current_invocation[1] += "@@"
                            current_invocation[1] += line.split('\tat ')[1].split('(')[0].strip()
                        continue
                    if 'XMLCONTROLLER' in line:
                        if exception_appear:
                            line = line.split("entering")[1].strip()
                            line = line.split('is filtered')[0]
                            if line.startswith('('):
                                count = 0
                                while line[count] != ')':
                                    count += 1
                                line = line[count+1:]
                            line = line.split('with exception')[0]
                            current_invocation[0] = line.strip()
                            exception_appear = False
        count = 0
        for invocation in invocationTrace:
            invocation[0] = invocation[0].replace('<','&lt;').replace('>','&gt;')
            if len(lists) == 0:
                break
            if invocation[0] in lists.keys():
                include = True
                for stack in lists.get(invocation[0]):
                    for element in stack.split('&#64;&#64;'):
                        if element not in invocation[1]:
                            include = False
                            break
                    if include:
                        lists.get(invocation[0]).remove(stack)
                        if len(lists.get(invocation[0])) == 0:
                            lists.pop(invocation[0])
                        break
                if include:
                    final_results.append([])
                    length = len(final_results)
                    for i in range(count - self.__trigger_gap,count + self.__trigger_gap + 1):
                        if i < 0:
                            continue
                        if not i < len(invocationTrace):
                            break
                        stack_value = self.convert_stackstring_to_keyword_string(invocationTrace[i][1])
                        final_results[length - 1].append(self.__active_method_format % (0, invocationTrace[i][0],
                                                                                        self.__stack_format % (stack_value)))
            count += 1

        count = 0
        for result in final_results:
            count += 1
            if len(result) == 2 * self.__trigger_gap + 1:
                break
        if len(final_results) == count:
            if(count == 0):
                return None
            return final_results[count - 1:]
        final_results = final_results[count - 1:]
        count = len(final_results)
        while True:
            if len(final_results[count - 1]) == 2 * self.__trigger_gap + 1:
                break
            count -= 1
        final_results = final_results[:count]
        if self.__left_invocations is None:
            self.__left_invocations = []
        if lists is not None and len(lists) > 0:
            for invocation in lists:
                for stack in lists.get(invocation):
                    self.__left_invocations.append('%s :: %s' % (invocation, stack))
        return final_results

    def get_pattern(self, n):
        list = []
        length = 2 ** n
        for i in range(length):
            if i == 0:
                continue
            pattern = []
            while i > 0:
                pattern.append( i % 2)
                i //= 2
            while len(pattern) < n:
                pattern.append(0)
            list.append(pattern)
        return list

    def compose_throughout_method_pattern(self, elements, pattern):
        method_pattern = ''
        exception_format = self.__active_exception_format % (0, self.__exception,"")
        length = len(elements)
        for i in range(length):
            element = elements[i]
            if pattern[i] == 0:
                continue
            elif pattern[i] == 1:
                method_pattern += element
        return self.__fixeh_head + exception_format + method_pattern +self.__fixeh_end

    def get_left_invocations(self):
        return self.__left_invocations

