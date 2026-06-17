# -*- coding:utf8 -*-
import time
from appium import webdriver
from appium.webdriver.common.touch_action import TouchAction
import os
import getopt
import subprocess
import sys
import Logger
import EmailConductor
import hashlib
from time import strftime
from time import gmtime
import zipfile

opts, args = getopt.getopt(sys.argv[1:], "p:b:t:")
for op, value in opts:
    if op == "-p":
        test_package=value
    elif op == "-b":
        test_base_dir = value
    elif op == "-t":
        triggerException = value

output_tag = os.path.basename(test_base_dir)

output_file = os.path.join(os.path.dirname(test_base_dir), 'finalresult.out')

logger = Logger.Logger(output_dir=test_base_dir, app_package_name=test_package)
#logger.begin_filter_log()
logger.begin_log()
#appium 配置信息
desired_caps = {}
desired_caps['appium-version'] = '1.0'
desired_caps['platformName'] = 'Android'
# desired_caps['platformVersion'] = '5.1.1'
# desired_caps['deviceName'] = 'Pixel 2 API 22'
# desired_caps['platformVersion'] = '6.0.1'
# desired_caps['deviceName'] = 'Redmi 4A'
desired_caps['platformVersion'] = '8.0.0'
desired_caps['deviceName'] = 'Pixel 2 API 26'
desired_caps['newCommandTimeout'] = 8000
# desired_caps['automationName'] = 'UIAutomator2'
desired_caps['noReset'] = False
desired_caps['appPackage'] = 'com.mendhak.gpslogger'
desired_caps['appActivity'] = '.GpsMainActivity'
desired_caps["unicodeKeyboard"] = True
desired_caps["resetKeyboard"] = True
#  "platformVersion": "5.1.1",
#  "deviceName": "Nexus 4 API 22",
#  "platformVersion": "6.0.1",
#  "deviceName": "Redmi 4A",
# {
#  "appium-version": "1.0",
#  "platformName": "Android",
#  "platformVersion": "8.0.0",
#  "deviceName": "Pixel 2 API 26",
#  "newCommandTimeout": "8000",
#  "noReset": "False",
#  "appPackage": "com.mendhak.gpslogger",
#  "appActivity": ".GpsMainActivity",
#  "unicodeKeyboard": "True",
#  "resetKeyboard": "True"
# }
#python Controller.py -e java.io.IOException -t GPSLoggerOutput -T testsuitGPSLogger.py -A GPSLogger
#python Controller.py -e java.io.FileNotFoundException -t GPSLoggerOutput1 -T testsuitGPSLogger.py -A GPSLogger
#python Controller.py -e java.security.KeyStoreException -t GPSLoggerOutput2 -T testsuitGPSLogger.py -A GPSLogger
#python Controller.py -e java.nio.charset.CharacterCodingException -t GPSLoggerOutput3 -T testsuitGPSLogger.py -A GPSLogger
#python Controller.py -e java.net.UnknownHostException -t GPSLoggerOutput4 -T testsuitGPSLogger.py -A GPSLogger
#tese
#启动webdriver执行测试脚本

email_exist = False
ftp_exist = False
base_log_exist = False

try:
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)

    time.sleep(4)
    #testcase 0:init
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.packageinstaller:id/permission_allow_button")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.packageinstaller:id/permission_allow_button")[0]
    el.click()
    time.sleep(2)

    #testcase1:start logging
    #el = driver.find_elements_by_id("com.mendhak.gpslogger:id/btnActionProcess")[0]
    #el.click()
    TouchAction(driver).tap(x=400, y=700).perform()
    time.sleep(15)

    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Simple View\")")
    # el.click()
    TouchAction(driver).tap(x=300, y=120).perform()
    #el = driver.find_elements_by_id("android:id/text1")[0]
    #el.click()
    time.sleep(2)

    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Detailed View\")")
    # el.click()
    el = driver.find_elements_by_id("android:id/text1")[1]
    el.click()
    time.sleep(2)

    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Detailed View\")")
    # el.click()
    el = driver.find_elements_by_id("android:id/text1")[0]
    el.click()
    time.sleep(2)

    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Big View\")")
    # el.click()
    el = driver.find_elements_by_id("android:id/text1")[2]
    el.click()
    time.sleep(2)

    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Big View\")")
    # el.click()
    el = driver.find_elements_by_id("android:id/text1")[0]
    el.click()
    time.sleep(2)

    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Log View\")")
    # el.click()
    el = driver.find_elements_by_id("android:id/text1")[3]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/logview_chkLocationsOnly")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/logview_startLogging")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/logview_chkLocationsOnly")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/logview_startLogging")[0]
    el.click()
    time.sleep(2)

    #testcase2:test send email
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Email\")")
    el.click()
    time.sleep(2)

    #el = driver.find_elements_by_id("android:id/switchWidget")[0]
    el = driver.find_elements_by_class_name("android.widget.Switch")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Target email addresses\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("user7458@163.com")

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Mail Provider\")")
    el.click()
    time.sleep(2)


    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_control")[3]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Username\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("user7458@163.com")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"From address\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("user7458@163.com")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Password\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("Aa719588417")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Server\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("smtp.163.com")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Port\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.clear()
    el.send_keys("465")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)


    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)

    # el = driver.find_elements_by_id("android:id/switchWidget")[0]
    # el.click()
    el = driver.find_elements_by_class_name("android.widget.Switch")[0]
    el.click()
    time.sleep(2)
    #will get an email after click
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Test email\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    #testcase3: set log send
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Auto send, email and upload\")")
    el.click()
    time.sleep(2)

    # el = driver.find_elements_by_id("android:id/switchWidget")[0]
    # el.click()
    el = driver.find_elements_by_class_name("android.widget.Switch")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"How often?\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    # el = driver.find_elements_by_id("android:id/switchWidget")[1]
    # el.click()
    el = driver.find_elements_by_class_name("android.widget.Switch")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    #testcase4: test ftp
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"FTP\")")
    el.click()
    time.sleep(2)

    # el = driver.find_elements_by_id("android:id/switchWidget")[0]
    # el.click()
    el = driver.find_elements_by_class_name("android.widget.Switch")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Server\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("192.168.16.1")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Username\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("user")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Password\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.send_keys("user")
    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)
    #E:\SummerProject\test\GPSLogger\
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Test upload\")")
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_id("com.mendhak.gpslogger:id/md_buttonDefaultPositive")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    #testcase5: stop logging and send email
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Log View\")")
    el.click()
    time.sleep(8)

    el = driver.find_elements_by_id("android:id/text1")[0]
    el.click()
    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Simple View\")")
    # el.click()
    time.sleep(5)
    TouchAction(driver).tap(x=400, y=710).perform()
    time.sleep(10)
    #click stop,so the email will be sent, and will upload file to ftp
    #E:\SummerProject\test\GPSLogger\20190926.zip
    # el = driver.find_elements_by_id("com.mendhak.gpslogger:id/btnActionProcess")[0]
    # el.click()

    # el = driver.find_elements_by_id("com.mendhak.gpslogger:id/btnActionProcess")[0]
    # el.click()
    # time.sleep(5)

finally:
    try:
        driver.quit()
        #driver.close()
    finally:
        create_context_format = '%s:: %s  create expected md5 property : %s\n'
        current_time = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        time.sleep(2)
        logger.close()
        logger.generate_log_file(triggerException=triggerException)
        result1 = False
        result2 = False

        ec = EmailConductor.EmailUnseenConductor(host='imap.163.com', user='user7458', password='Aa719588417')
        ec.parseEmail(body_dir=test_base_dir)
        ftpsource_path = 'E:\\SummerProject\\test\\GPSLogger'
        ftpdest_path = os.path.join(test_base_dir, 'ftp')
        ftp_files = []
        if not os.path.exists(ftpdest_path):
            os.makedirs(ftpdest_path)
        #temp_result = None

        #get ftp files
        for name in os.listdir(ftpsource_path):
            if '.zip' in name:
                temp = os.path.join(ftpsource_path, name)
                temp_dest = os.path.join(ftpdest_path,os.path.basename(temp))
                ftp_files.append(temp_dest)
                cmd_str = 'move %s %s' % (temp, temp_dest)
                os.system(cmd_str)
            if '.xml' in name:
                temp = os.path.join(ftpsource_path, name)
                temp_dest = os.path.join(ftpdest_path, os.path.basename(temp))
                ftp_files.append(temp_dest)
                cmd_str = 'move %s %s' % (temp, temp_dest)
                os.system(cmd_str)
                    # mover = subprocess.Popen(cmd_str, stdout=subprocess.PIPE,
                    #                          stderr=subprocess.PIPE)
                    # stdout, stderr = mover.communicate()
                    # if stderr is not None and len(stderr) != 0:
                    #     print('err move: ' + stderr.decode('gbk'))
        #if temp_result:
            #ftpsource_path = temp_result
        #if '.zip' in ftpsource_path:

            # mover = subprocess.Popen(['move', ftpsource_path, ftpdest_path],stdout=subprocess.PIPE, stderr=subprocess.PIPE)
            # stdout,stderr = mover.communicate()
            # if stderr is not None and len(stderr) != 0:
            #     print('err move: ' + stderr.decode('gbk'))
        result_dir = os.path.join(test_base_dir,test_package)
        if not os.path.exists(result_dir):
            os.makedirs(result_dir)
        result_file = os.path.join(result_dir,'result.out')
        ftp_xml_results = []
        with open(result_file,'a+') as fps:
            email_attach_dir = os.path.join(test_base_dir,'attach')
            ftp_dir = os.path.join(test_base_dir,'ftp')
            email_results = []
            ftp_results = []
            if os.path.exists(email_attach_dir):
                for name in os.listdir(email_attach_dir):
                    email_attach_file = os.path.join(email_attach_dir,name)
                    if os.path.isfile(email_attach_file):
                        email_results.append(name)
                        email_results.append(os.path.getsize(email_attach_file))
                        zName = None
                        if os.path.exists(email_attach_file):
                            try:
                                zEmail = zipfile.ZipFile(email_attach_file)
                                #for name in zEmail.namelist()
                                zName = zEmail.filelist[0].filename
                                zEmail.extract(zName,email_attach_dir)
                            except zipfile.BadZipFile:
                                email_results.append('badzipFile!')
                                email_results.append('0')
                                continue
                        if zName:
                            zFilePath = os.path.join(email_attach_dir,zName)
                            with open(zFilePath,'rb') as efps:
                                if os.path.getsize(zFilePath) < 920:
                                    email_results.append('-' + hashlib.md5(efps.read()).hexdigest())
                                else:
                                    email_results.append(hashlib.md5(efps.read()).hexdigest())

            if os.path.exists(ftp_dir):
                for name in os.listdir(ftp_dir):
                    ftp_file = os.path.join(ftp_dir,name)
                    zName = None
                    if not os.path.isfile(ftp_file):
                        continue
                    if os.path.exists(ftp_file) and 'zip' in ftp_file:
                        ftp_results.append(name)
                        ftp_results.append(os.path.getsize(ftp_file))
                        try:
                            zFTP = zipfile.ZipFile(ftp_file)
                            zName = zFTP.filelist[0].filename
                            zFTP.extract(zName, ftp_dir)
                        except zipfile.BadZipFile:
                            ftp_results.append('badzipFile!')
                            ftp_results.append('0')
                            continue
                        if zName:
                            zFilePath = os.path.join(ftp_dir, zName)
                            with open(zFilePath, 'rb') as efps:
                                if os.path.getsize(zFilePath) < 920:
                                    ftp_results.append('-' + hashlib.md5(efps.read()).hexdigest())
                                else:
                                    ftp_results.append(hashlib.md5(efps.read()).hexdigest())
                    elif(os.path.exists(ftp_file) and 'xml' in ftp_file):
                        ftp_xml_results.append(name)
                        ftp_xml_results.append(os.path.getsize(ftp_file))
                        ftp_xml_results.append('-1')
                        with open(os.path.join(ftp_file),'rb') as efps:
                            ftp_xml_results[2] = hashlib.md5(efps.read()).hexdigest()
            if len(ftp_xml_results) is 0:
                fps.write('%s:: assert_ftp_test_file_existence  Result is: %s\n' % (current_time, 'False'))
            else:
                fps.write('%s:: assert_ftp_test_file_existence  Result is: %s\n' % (current_time, 'True'))
            if len(email_results) is 0:
                fps.write('%s:: assert_email_file_existence  Result is: %s\n' % (current_time, 'False'))
                if len(ftp_results) > 0:
                    fps.write('%s::%s assert_fpt_file_existence  Result is: %s\n' % (current_time, ftp_results[0], 'True'))
                    fps.write('%s:: fpt_size: %d ; fpt_md5: %s' % (current_time, ftp_results[1], ftp_results[2]))
                else:
                    fps.write('%s:: assert_ftp_file_existence  Result is: %s\n' % (current_time, 'False'))
            elif len(ftp_results) is 0:
                fps.write('%s:: assert_fpt_file_existence  Result is: %s\n' % (current_time, 'False'))
                fps.write('%s::%s assert_email_file_existence  Result is: %s\n' % (current_time,email_results[0], 'True'))
                fps.write('%s:: email_attach_size: %d ; email_attach_md5: %s'% (current_time, email_results[1],email_results[2]))
            else:
                fps.write('%s::%s assert_email_file_existence  Result is: %s\n' % (current_time, email_results[0], 'True'))
                fps.write('%s::%s assert_fpt_file_existence  Result is: %s\n' % (current_time, ftp_results[0], 'True'))
                if email_results[1] == ftp_results[1]:
                    result1 = True
                if (not 'bad' in email_results[2]) and (not 'bad' in ftp_results[2]) and (ftp_results[2] == email_results[2]):
                    if not email_results[2].startswith('-'):
                        result2 = True
                if result1:
                    fps.write(
                        '%s:: email %s size is %d, ftp %s size is %d Result is: True\n' %
                        (current_time, email_results[0], email_results[1],
                         ftp_results[0],ftp_results[1]))
                else:
                    fps.write(
                        '%s:: email %s size is %d, ftp %s size is %d Result is: False\n' %
                        (current_time, email_results[0], email_results[1],
                         ftp_results[0],ftp_results[1]))
                if result2:
                    fps.write(
                        '%s:: email %s md5 is %s, ftp %s md5 is %s Result is: True\n' %
                        (current_time, email_results[0], email_results[2],
                         ftp_results[0],ftp_results[2]))
                else:
                    fps.write(
                        '%s:: email %s md5 is %s, ftp %s md5 is %s Result is: False\n' %
                        (current_time, email_results[0], email_results[2],
                         ftp_results[0],ftp_results[2]))
        output_file = os.path.dirname(test_base_dir)
        output_file = os.path.join(output_file, 'finalresult.out')
        with open(output_file, 'a+') as fps:
            if result1 and result2:
                fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'email and ftp'))
            else:
                fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'email and ftp'))




    #cmd = "adb shell am broadcast -a com.mendhak.gpslogger.pkg.END_EMMA"
        #os.system(cmd)
