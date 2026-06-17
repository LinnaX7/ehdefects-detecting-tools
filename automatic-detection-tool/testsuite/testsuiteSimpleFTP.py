# -*- coding:utf8 -*-
import time
from appium import webdriver
from appium.webdriver.common.touch_action import TouchAction
from selenium.webdriver.common.touch_actions import TouchActions
from time import gmtime, strftime
import getopt
import os
import sys
import Logger
import subprocess
import traceback
import shutil
# 查看启动包名的方式：
# adb shell
# monkey -p 包名 -v -v -v 1
# python Controller.py -e android.database.SQLException -t SimpleFTPOutput1 -T testsuitSimpleFTP.py -A SimpleFTP
# python Controller.py -e java.net.SocketException -t SimpleFTPOutput2 -T testsuitSimpleFTP.py -A SimpleFTP
# python Controller.py -e java.io.IOException -t SimpleFTPOutput -T testsuitSimpleFTP.py -A SimpleFTP
# appium 配置信息


def get_screen_info(driver,screen_dir,screen_xml_dir,screenCount):
    screenName = str(screenCount) + ".png"
    driver.save_screenshot(os.path.join(screen_dir, screenName))
    screenXmlName = str(screenCount) + ".xml"
    with open(os.path.join(screen_xml_dir,screenXmlName),'w+') as fps:
        fps.write(driver.page_source)
    # cmd = "adb shell /system/bin/uiautomator dump /data/local/tmp/" + screenXmlName
    # res = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE,
    #                        stderr=subprocess.PIPE)  # 使用管道
    # # result = res.stdout.read()  # 获取输出结果
    # stdout,stderr = res.communicate()
    # print('stdout:%s   stderr:%s' % (stdout,stderr))
    # # time.sleep(5)
    # cmd = "adb pull /data/local/tmp/" + screenXmlName + " " + screen_xml_dir
    # os.system(cmd)
    time.sleep(5)
    return

desired_caps = {}
desired_caps['appium-version'] = '1.0'
desired_caps['platformName'] = 'Android'
# desired_caps['platformVersion'] = '8.0.0'
# desired_caps['deviceName'] = 'Pixel 2 API 26'
desired_caps['platformVersion'] = '6.0.1'
desired_caps['deviceName'] = 'Redmi 4A'
desired_caps['newCommandTimeout'] = 8000
# desired_caps['automationName'] = 'UIAutomator2'
desired_caps['noReset'] = False
desired_caps['appPackage'] = 'com.paulds.simpleftp'
desired_caps['appActivity'] = '.presentation.activities.SplashActivity'
desired_caps["unicodeKeyboard"] = True
desired_caps["resetKeyboard"] = True

#  "platformVersion": "5.1.1",
#  "deviceName": "Nexus 4 API 22",
#  "platformVersion": "6.0.1",
#  "deviceName": "Redmi 4A",
# {
#  "appium-version": "1.0",
#  "platformName": "Android",
#  "platformVersion": "6.0.1",
#  "deviceName": "Redmi 4A",
#  "newCommandTimeout": "8000",
#  "noReset": "True",
#  "appPackage": "com.paulds.simpleftp",
#  "appActivity": ".presentation.activities.SplashActivity",
#  "unicodeKeyboard": "True",
#  "resetKeyboard": "True"
# }

#tese
#启动webdriver执行测试脚本
opts, args = getopt.getopt(sys.argv[1:], "p:b:t:")
for op, value in opts:
    if op == "-p":
        test_package = value
    elif op == "-b":
        test_base_dir = value
    elif op == '-t':
        triggerException = value

output_tag = os.path.basename(test_base_dir)
output_file = os.path.join(os.path.dirname(test_base_dir), 'finalresult.out')
logger = Logger.Logger(output_dir=test_base_dir, app_package_name=test_package)
logger.begin_log()
screen_dir = os.path.join(test_base_dir, 'screenShot')
screen_xml_dir = os.path.join(test_base_dir, 'screenXml')
# os.mkdir(screen_dir)
# os.mkdir(screen_xml_dir)
# screenCount = 1


try:
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)

    time.sleep(4)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1
    # testcase0:test add Favorites servers
    el = driver.find_elements_by_id("com.paulds.simpleftp:id/Main_ibServers")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/fabAddServer")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_etName")[0]
    el.send_keys("KFC")

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_etHost")[0]
    el.send_keys("192.168.0.107")

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_etLogin")[0]
    el.send_keys("user")

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_etPassword")[0]
    el.send_keys("user")

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_swAnonymous")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_swAnonymous")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_ibCreate")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/AddServer_ibBack")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/Explorer_ivConnect")[0]
    TouchActions(driver).tap(el).perform()
    TouchActions(driver).tap(el).perform()
    # el = driver.find_elements_by_class_name("android.widget.ImageView")[4]
    # el.click()
    # time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1


    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"KFC\")")
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    #testcase1: create folder
    el = driver.find_elements_by_id("com.paulds.simpleftp:id/fabPlus")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_class_name("android.widget.EditText")[0]
    el.send_keys("folderAdd")

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1


    #testcase2: delete folder
    # el = driver.find_elements_by_class_name("android.widget.ImageView")[0]
    # TouchAction(driver).long_press(el).perform()
    el = driver.find_elements_by_id("com.paulds.simpleftp:id/filename")[0]
    TouchAction(driver).long_press(el).perform()
    TouchAction(driver).long_press(el).perform()

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    #el = driver.find_elements_by_class_name("android.widget.ImageView")[2]
    #el.click()
    TouchAction(driver).tap(x=650, y=200).perform()
    TouchAction(driver).tap(x=650, y=200).perform()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    #testcase3:delete file
    # el = driver.find_elements_by_class_name("android.widget.ImageView")[0]
    # TouchAction(driver).long_press(el).perform()
    el = driver.find_elements_by_id("com.paulds.simpleftp:id/filename")[0]
    TouchAction(driver).long_press(el).perform()
    TouchAction(driver).long_press(el).perform()

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    # el = driver.find_elements_by_class_name("android.widget.ImageView")[2]
    # el.click()
    TouchAction(driver).tap(x=650, y=200).perform()
    TouchAction(driver).tap(x=650, y=200).perform()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    #testcase4: delete server
    el = driver.find_elements_by_id("com.paulds.simpleftp:id/Main_ibServers")[0]
    el.click()
    time.sleep(1)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/ServerList_tvName")[0]
    el.click()

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("com.paulds.simpleftp:id/ConsultServer_ibDelete")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # get_screen_info(driver, screen_dir, screen_xml_dir, screenCount)
    # screenCount += 1

    driver.back()
except BaseException as e:
    traceback.print_tb()
finally:
    dir = False
    file1 = False
    try:
        driver.quit()
    except BaseException as e:
        traceback.print_tb()
    try:
        time.sleep(4)
        logger.close()
        logger.generate_log_file(triggerException=triggerException)
    except BaseException as e:
        traceback.print_tb()
    try:
        if not os.path.exists(os.path.join(test_base_dir,test_package)):
            os.makedirs(os.path.join(test_base_dir,test_package))
        log_context_format = '%s:: %s  assert_file_existence  Result is: %s\n'
        current_time  = strftime("%Y-%m-%d-%H-%M-%S", gmtime())
        if os.path.exists('e:/SummerProject/test/test'):
            dir = True
            with open(os.path.join(test_base_dir,test_package,'result.out'),'a') as rof:
                rof.write(log_context_format % (current_time, 'delete1', 'Wrong'))
        else:
            with open(os.path.join(test_base_dir,test_package,'result.out'),'a') as rof:
                rof.write(log_context_format % (current_time, 'delete1', 'Right'))
        if os.path.exists('e:/SummerProject/test/gray.jpg'):
            file1 = True
            with open(os.path.join(test_base_dir,test_package,'result.out'),'a') as rof:
                rof.write(log_context_format % (current_time, 'delete2', 'Wrong'))
        else:
            with open(os.path.join(test_base_dir,test_package,'result.out'),'a') as rof:
                rof.write(log_context_format % (current_time, 'delete2', 'Right'))
        if dir or file1:
            with open(output_file,'a') as frf:
                frf.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'delete'))
        else:
            with open(output_file, 'a') as frf:
                frf.write('Test %s Tag %s Result: Right\n' % (output_tag, 'delete'))

    except BaseException as e:
        traceback.print_tb()
    try:
        # 恢复文件
        if dir is False:
            cmd = "xcopy E:\\SummerProject\\testcopy E:\\SummerProject\\test /t /e"
            os.system(cmd)
        if file1 is False:
            shutil.copy('e:/SummerProject/testcopy/gray.jpg','e:/SummerProject/test/gray.jpg')
    except BaseException as e:
        traceback.print_tb()




