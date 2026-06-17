import subprocess
from time import gmtime, strftime
import os
import json
import shutil
import zipfile
from zipfile import BadZipFile
import hashlib

class FileAssert(object):
    def __init__(self, base_Dir,tag):
        self.base_Dir = os.path.join(base_Dir, tag)
        if not os.path.exists(self.base_Dir):
            os.makedirs(self.base_Dir)
        temp_pardir = os.path.dirname(self.base_Dir)
        temp_pardir = os.path.dirname(temp_pardir)
        self.expected_property_dir = os.path.join(temp_pardir, '0', tag)
        if not os.path.exists(self.expected_property_dir):
            os.makedirs(self.expected_property_dir)
        self.expected_md5_property_path = os.path.join(self.expected_property_dir, 'expected_md5_property_path.out')
        self.expected_file_paras_path = os.path.join(self.expected_property_dir, 'expected_file_paras_path.out')
        self.log_file_path = os.path.join(self.base_Dir, 'result.out')
        self.screen_cap_path = os.path.join(self.base_Dir, 'screen_cap')
        #if not os.path.exists(self.screen_cap_path):
        #    os.mkdir(self.screen_cap_path)

    def assert_file_existence(self, expected_file_paths, driver=None, isdelete=False):
        log_context_format = '%s:: %s  assert_file_existence  Result is: %s\n'
        log_delete_format = '%s:: %s  assert_file_delete  Result is: %s\n'
        current_time  = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        image_name = 'FileEx-' + current_time
        result_file_paths = []
        if driver is not None:
            self.get_screen(driver=driver, image_name=image_name)
        for expected_file_path in expected_file_paths:
            file_existence = subprocess.Popen(["adb","shell","ls",expected_file_path],
                                              stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            (stdout, stderr) = file_existence.communicate()
            if stderr is not None and len(stderr) != 0:
                print('wrong adb ls::' + stderr.decode('gbk'))
                continue
            else:
                if 'No such file or directory' in stdout.decode('gbk'):
                    print(stdout)
                    if not isdelete:
                        self.write_log(log_context=log_context_format % (current_time, expected_file_path, 'False'))
                    else:
                        self.write_log(log_context=log_delete_format % (current_time, expected_file_path, 'True'))
                    continue
                else:
                    if not isdelete:
                        self.write_log(log_context=log_context_format % (current_time, expected_file_path, 'True'))
                    else:
                        self.write_log(log_context=log_delete_format % (current_time, expected_file_path,'False'))
                    result_file_paths.append(expected_file_path)
                    continue
        return result_file_paths

    def assert_md5_equality(self, detected_paths, driver=None, expected_md5s=None,
                            is_zip=False,pulling_path=None,dir_name=None,file_names=None, update_time=0):
        log_context_format = '%s:: %s  assert_md5_equality  expected md5: %s, detected md5: %s  Result is %s\n'
        create_context_format = '%s:: %s  create expected md5 property : %s\n'
        current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        image_name = "MD5eq-" + current_time
        expected_paths = []
        # dir_name should be only name of the dir
        if driver is not None:
            self.get_screen(driver=driver, image_name=image_name)
        if is_zip:
            # assert(len(detected_paths) is 1)
            detected_path = detected_paths[0]
            '''
            if detected_path is None:
                print("iszip set true with no zip path")
                return False
            '''
            if pulling_path is None:
                print("pulling path should be set")
                return False
            # pull zip file
            puller = subprocess.Popen(["adb","pull", detected_path, pulling_path], stdout=subprocess.PIPE,
                                      stderr=subprocess.PIPE)
            (stdout, stderr) = puller.communicate()
            if stderr is not None and len(stderr) != 0:
                print('wrong pull file %s :: %s'%(detected_path,stderr.decode('gbk')))
                return False

            # detected_path = extact path of file -> to unzip it
            detected_path = os.path.join(pulling_path, os.path.split(detected_path)[-1])
            print(detected_path)
            if zipfile.is_zipfile(detected_path):
                print("detected_path is " + detected_path)
                try:
                    zip_file = zipfile.ZipFile(detected_path)
                except BadZipFile:
                    self.write_log(log_context="create BadZipFile\n")
                    return False
                zip_file.extractall(path=pulling_path)
                zip_file.close()
                os.remove(detected_path)
                detected_base_path = os.path.join(os.path.dirname(detected_path), dir_name)
                detected_paths = []
                non_zip_paths = []
                # relative path is logged
                for file_name in file_names:
                    detected_path = os.path.join(detected_base_path, file_name)
                    expected_path = os.path.join(os.path.basename(detected_base_path), file_name)
                    #expected_path = os.path.join(dir_name,file_name)
                    # expected_path = os.path.join(os.path.dirname(self.expected_property_dir), dir_name,file_name)
                    if os.path.exists(detected_path):
                        print("detected_path is " + detected_path + ' and expected_path is ' + expected_path)
                        detected_paths.append(detected_path)
                        expected_paths.append(expected_path)
                    else:
                        non_zip_paths.append(os.path.dirname(detected_path))
            else:
                try:
                    _ = zipfile.ZipFile(detected_path)
                except BadZipFile:
                    self.write_log(log_context="create BadZipFile\n")
                    return False
        else:
            for detected_path in detected_paths:
                expected_paths.append(detected_path)
        if expected_md5s is None:
            appear = 0
            if os.path.exists(self.expected_md5_property_path):
                with open(self.expected_md5_property_path, 'r') as fps:
                    if is_zip:
                        temp_path = expected_paths[0]
                    else:
                        temp_path = detected_paths[0]
                    for line in fps.readlines():
                        if temp_path in line:
                            appear += 1
                            if appear - 1 == update_time:
                                expected_md5s = json.loads(line)
                                break

            if not appear - 1 == update_time: # need to load expected attributes to expeceted file
                expected_md5s = {}
                # TODO can not find the attributes
                for i in range(len(detected_paths)):
                    expected_md5s[expected_paths[i]] = self.get_check_sum(detected_paths[i],is_zip)
                    self.write_log(log_context=create_context_format %
                                               (current_time, expected_paths[i],expected_md5s[expected_paths[i]]))
                    print('right get checksum at %s and md5 is %s' %
                          (expected_paths[i], expected_md5s[expected_paths[i]]))
                    # os.remove(detected_path)
                '''
                for detected_path in detected_paths:
                    expected_md5s[detected_path] = self.get_check_sum(detected_path,is_zip)
                    self.write_log(log_context=create_context_format % (current_time, detected_path, expected_md5s[detected_path]))
                    print('right get checksum at %s and md5 is %s' % (detected_path, expected_md5s[detected_path]))
                    #os.remove(detected_path)
                '''
                with open(self.expected_md5_property_path, 'a+') as fps:
                    json_str = json.dumps(expected_md5s)
                    fps.write(json_str + '\n')
                if is_zip:
                    shutil.rmtree(os.path.dirname(detected_paths[i]))
                return True

        md5s = {}
        for detected_path in detected_paths:
            md5s[detected_path] = self.get_check_sum(detected_path,is_zip)
        if is_zip:
            for detected_path in detected_paths:
                os.remove(detected_path)
            shutil.rmtree(os.path.dirname(detected_path))
        flag = True
        for i in range(len(detected_paths)):
            detected_path = detected_paths[i]
            #expected_path = expected_paths[i]
        #for detected_path in detected_paths:
            if is_zip:
                #[temp,temp1] = os.path.split(detected_path)
                #temp =  os.path.split(temp)[1]
                expected_path = expected_paths[i]
                #expected_path = os.path.join(os.path.split(self.expected_property_dir)[0], temp,temp1)
            else :
                expected_path = detected_path
            if md5s[detected_path] == expected_md5s[expected_path]:
                self.write_log(log_context=log_context_format % (current_time, detected_path, md5s[detected_path], expected_md5s[expected_path], 'True'))
                continue
            self.write_log(log_context=log_context_format % (current_time,detected_path, expected_md5s[expected_path], md5s[detected_path], 'False'))
            flag = False
        log_context_non_zip_format = '%s:: %s is not compressed\n'
        if is_zip:
            for non_zip_path in non_zip_paths:
                flag = False
                self.write_log(log_context=log_context_non_zip_format % (current_time,non_zip_path))
        return flag

    '''
    def assert_certain_file_para_equality(self, mode, detected_file_paths, driver, expected_para=None, iscreated=False,
                                          update_time=0): 
        #notice that if update_time = 0, then the file is first coducted.
        create_file_paras_format = '%s:: %s  create expected file paras: %s\n'
        log_context_format = '%s:: %s  assert_%s_equality  expected %s: %s, detected %s: %s  Result is %s\n'
        current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        image_name = 'FileParaeq-' + current_time

        if driver is not None:   
            # get screenprint
            self.get_screen(driver=driver, image_name=image_name)
        if expected_para is None:
            appear = False
            # appear used for log whether the file is create and the according file is logged on the expected file.
            # if appear is True, then expected para has logged in the expected_file.
            if os.path.exists(self.expected_file_paras_path):
                # here we have to find out that weather the 
                with open(self.expected_file_paras_path, 'r') as fps:
                    for line in fps.readlines():
                        if detected_file_paths[0] in line:
                            expected_paras = json.loads(line)
                            appear = True
                            break
            if(iscreated and not appear):
                # in this branch, we should log the certain infomation in the file.
                expected_paras = {}
                for detected_file_path in detected_file_paths:` 
                    expected_para = self.get_file_paras(detected_file_path=detected_file_path)
                    expected_paras[detected_file_path] = expected_para
                    self.write_log(log_context=create_file_paras_format % (current_time, detected_file_path,expected_para))
                with open(self.expected_file_paras_path, 'a+') as fps:
                    josn_str = json.dumps(expected_paras)
                    fps.write(josn_str + '\n')
                return True
        detected_paras = {}
        for detected_file_path in detected_file_paths:
            detected_paras[detected_file_path] = self.get_file_paras(detected_file_path=detected_file_path)
        flag = True
        for detected_file_path in detected_file_paths:
            if expected_paras[detected_file_path][mode] == detected_paras[detected_file_path][mode]:
                self.write_log(log_context=log_context_format % (current_time, detected_file_path, mode, mode, expected_paras[detected_file_path][mode],
                                                             mode, expected_paras[detected_file_path][mode], 'True'))
                continue
            self.write_log(log_context=log_context_format % (current_time, detected_file_path, mode, mode, expected_paras[detected_file_path][mode],
                                                         mode, detected_paras[detected_file_path][mode], 'False'))
        return flag
    '''

    def assert_certain_file_para_equality(self, mode, detected_file_paths, driver, expected_para=None, update_time=0):
        #notice that if update_time = 0, then the file is first coducted.
        create_file_paras_format = '%s:: %s  create expected file paras: %s\n'
        log_context_format = '%s:: %s  assert_%s_equality  expected %s: %s, detected %s: %s  Result is %s\n'
        current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        image_name = 'FileParaeq-' + current_time

        if driver is not None:
            # get screenprint
            self.get_screen(driver=driver, image_name=image_name)
        if expected_para is None:
            appear_time = 0
            if os.path.exists(self.expected_file_paras_path):
                # here we have to find out that weather the
                with open(self.expected_file_paras_path, 'r') as fps:
                    for line in fps.readlines():
                        if detected_file_paths[0] in line:
                            appear_time += 1
                            if appear_time - 1 == update_time:
                                expected_para = json.loads(line)
                                break
            if(not appear_time - 1 == update_time):
                # in this branch, we should log the certain infomation in the file.
                expected_paras = {}
                for detected_file_path in detected_file_paths:
                    expected_para = self.get_file_paras(detected_file_path=detected_file_path)
                    expected_paras[detected_file_path] = expected_para
                    self.write_log(log_context=create_file_paras_format % (current_time, detected_file_path,expected_para))
                with open(self.expected_file_paras_path, 'a+') as fps:
                    josn_str = json.dumps(expected_paras)
                    fps.write(josn_str + '\n')
                return True
        detected_paras = {}
        for detected_file_path in detected_file_paths:
            detected_paras[detected_file_path] = self.get_file_paras(detected_file_path=detected_file_path)
        flag = True
        for detected_file_path in detected_file_paths:
            if expected_para[detected_file_path][mode] == detected_paras[detected_file_path][mode]:
                self.write_log(log_context=log_context_format % (current_time, detected_file_path, mode, mode, expected_para[detected_file_path][mode],
                                                             mode, expected_para[detected_file_path][mode], 'True'))
                continue
            self.write_log(log_context=log_context_format % (current_time, detected_file_path, mode, mode, expected_para[detected_file_path][mode],
                                                         mode, detected_paras[detected_file_path][mode], 'False'))
        return flag

    def assert_size_equality(self, detected_file_paths, driver=None, expected_size=None, update_time=0):
        return self.assert_certain_file_para_equality(mode='size', expected_para=expected_size,
                                                      detected_file_paths=detected_file_paths, driver=driver, update_time=update_time)


    def assert_owner_equality(self, detected_file_paths, driver=None, expected_para=None, update_time=0):
        if expected_para is None:
            flag = self.assert_certain_file_para_equality(mode='owner',
                                                          detected_file_paths=detected_file_paths, driver=driver, update_time=update_time)
            return flag & self.assert_certain_file_para_equality(mode='owner_group',
                                                                 detected_file_paths=detected_file_paths, driver=driver, update_time=update_time)
        flag = self.assert_certain_file_para_equality(mode='owner', expected_para=expected_para[0],
                                                      detected_file_paths=detected_file_paths, driver=driver, update_time=update_time)
        return flag & self.assert_certain_file_para_equality(mode='owner_group', detected_file_paths=detected_file_paths,
                                                             expected_para=expected_para[1], driver=driver, update_time=update_time)

    def assert_create_time_equality(self, detected_file_paths, driver=None, expected_para=None, update_time=0):
        flag = True
        if expected_para is None:
            flag = self.assert_certain_file_para_equality(mode='create_date', driver=driver,
                                                          detected_file_paths=detected_file_paths, update_time=update_time)
            return flag & self.assert_certain_file_para_equality(mode='create_time', driver=driver,
                                                                 detected_file_paths=detected_file_paths, update_time=update_time)
        flag = flag and self.assert_certain_file_para_equality(mode='create_date', expected_para=expected_para, driver=driver,
                                                      detected_file_paths=detected_file_paths, update_time=update_time)
        return flag & self.assert_certain_file_para_equality(mode='create_time', expected_para=expected_para,
                                                             driver=driver, detected_file_paths=detected_file_paths, update_time=update_time)

    def write_log(self, log_context):
        with open(self.log_file_path, 'a+') as fps:
            fps.write(log_context)

    @staticmethod
    def get_file_paras(detected_file_path):
        files_paras_reader = subprocess.Popen(["adb", "shell", "ls", "-l", detected_file_path],
                                              stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        (stdout, stderr) = files_paras_reader.communicate()
        if (stderr is not None) and (len(stderr) != 0):
            print(stderr.decode('gbk'))
            return
        file_paras_list = stdout.decode('gbk').split()
        if len(file_paras_list) < 5:
            print('wrong ls cmd with out put'+stdout.decode('gbk'))
            return
        detected_file_paras = {'privilege': file_paras_list[0], 'owner': file_paras_list[1],
                               'owner_group': file_paras_list[2], 'size': file_paras_list[3],
                               'create_date': file_paras_list[4], 'create_time': file_paras_list[5],
                               'file_name': file_paras_list[6]}
        return detected_file_paras

    @staticmethod
    def get_check_sum(detected_path,is_zip):
        if is_zip:
            with open(detected_path,'rb') as rfps:
                return hashlib.md5(rfps.read()).hexdigest()
        else:
            file_md5 = subprocess.Popen('adb shell md5sum '+detected_path, shell=True,
                                        stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            [stdout, stderr] = file_md5.communicate()
            if stderr is not None and len(stderr) != 0:
                print('wrong adb checksum::' + stderr.decode('utf-8'))
                return None
            else:
                # elements = stdout.decode('utf-8').split()
                elements = stdout.decode('gb2312').split()
                if len(elements) > 3:
                    return elements[4]
                else:
                    return elements[0]

    def get_screen(self, driver, image_name):
        image_name = os.path.join(self.screen_cap_path,(image_name + ".png"))
        img = driver.get_screenshot_as_png()
        with open(image_name,"wb") as fps:
            fps.write(img)
        return

    def path_split(self,path_string):
        path =  path_string
        res = []
        while True:
            temp = os.path.split(path)
            if len(temp) < 1:
                res.append(temp)
                break
            else:
                path = temp[0]
                res.append(temp[1])
                if len(path) <= 2:
                    break
        return res

if __name__ == '__main__':
    ta = FileAssert(base_Dir='/Users/lulu/Downloads/test_assert/0', tag='test')
    #ta.assert_owner_equality(detected_file_path='/storage/emulated/0/.aa/1.png')
    ta.assert_md5_equality(detected_paths=['/Users/lulu/Downloads/test_assert/new_test.zip'],
                                    is_zip=False, pulling_path='/Users/lulu/Downloads/test_assert',
                                    dir_name="new_test", file_names=["initial.log"])