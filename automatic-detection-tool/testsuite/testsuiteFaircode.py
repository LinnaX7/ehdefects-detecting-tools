# -*- coding:utf8 -*-
import time
from appium import webdriver
from appium.webdriver.common.touch_action import TouchAction
import os
import getopt
import FileAssert
import Logger
import sys
#查看启动包名的方式：
#adb shell
#monkey -p 包名 -v -v -v 1

#appium 配置信息
opts, args = getopt.getopt(sys.argv[1:], "p:b:")
for op, value in opts:
    if op == "-p":
        test_package=value
    elif op == "-b":
        test_base_dir = value
    elif op == '-t':
        triggerException = value

output_tag = os.path.basename(test_base_dir)
output_file = os.path.join(os.path.dirname(test_base_dir), 'finalresult.out')
logger = Logger.Logger(output_dir=test_base_dir, app_package_name=test_package)
test_assert = FileAssert.FileAssert(base_Dir=test_base_dir, tag='fairmail')

logger.begin_log()
time.sleep(5)
desired_caps = {}
desired_caps['appium-version'] = '1.0'
desired_caps['platformName'] = 'Android'
# desired_caps['platformVersion'] = '8.0.0'
# desired_caps['deviceName'] = 'Pixel 2 API 26'
desired_caps['platformVersion'] = '6.0.1'
desired_caps['deviceName'] = 'Redmi 4A'
desired_caps['newCommandTimeout'] = 8000
# desired_caps['automationName'] = 'UIAutomator2'
desired_caps['fastReset'] = True
desired_caps['appPackage'] = 'eu.faircode.email'
desired_caps['appActivity'] = '.ActivityMain'
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
#  "appPackage": "eu.faircode.email",
#  "appActivity": ".ActivityMain",
#  "unicodeKeyboard": "True",
#  "resetKeyboard": "True"
# }

#tese
#启动webdriver执行测试脚本
#python Controller.py -e java.io.IOException -t FairEmailOutput -T testsuitFaircode.py -A fairmail
try:
    logger.begin_log()
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)
    time.sleep(4)

    #testcase0:create account
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)

    el = driver.find_elements_by_id("eu.faircode.email:id/btnOk")[0]
    el.click()
    time.sleep(4)

    el = driver.find_elements_by_id("eu.faircode.email:id/btnQuick")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("eu.faircode.email:id/etName")[0]
    el.send_keys("User")
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/etEmail")[0]
    el.send_keys("user7458@163.com")
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.EditText")[2]
    el.send_keys("Aa719588417")
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/btnCheck")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/btnSave")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)


    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)

    el = driver.find_elements_by_id("eu.faircode.email:id/btnInbox")[0]
    el.click()
    time.sleep(2)


    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    #testcase1:test write email and send email
    el = driver.find_elements_by_id("eu.faircode.email:id/fabCompose")[0]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_id("eu.faircode.email:id/menu_zoom")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/etTo")[0]
    el.send_keys("user7458@163.com")
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/etSubject")[0]
    el.send_keys("sendMailWithFile")
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/etBody")[0]
    el.send_keys("send mail with file")
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/menu_image")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Images\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Camera\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.documentsui:id/icon_thumb")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/action_send")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)
    time.sleep(20)

    #fresh
    driver.swipe(start_x=280, start_y=300, end_x=280, end_y=800, duration=500)
    time.sleep(15)

    driver.swipe(start_x=280, start_y=300, end_x=280, end_y=800, duration=500)
    time.sleep(15)

    driver.swipe(start_x=280, start_y=300, end_x=280, end_y=800, duration=500)
    time.sleep(15)

    #testcase2: test download file
    el = driver.find_elements_by_id("eu.faircode.email:id/clItem")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/cbInline")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/ibSave")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Downloads\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(3)

    #testcase3:test delete email
    el = driver.find_elements_by_id("eu.faircode.email:id/action_delete")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    #testcase4:test delete emil already delete
    el = driver.find_elements_by_id("eu.faircode.email:id/menu_folders")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/clItem")[3]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageView")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Select all\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("eu.faircode.email:id/fabMore")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Delete\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
finally:
    logger.close()
    time.sleep(10)
    logger.generate_log_file(triggerException=triggerException)
    pic_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/sdcard/Download/IMG_20190912_154245.jpg']):
        pic_exist = True
        if not test_assert.assert_size_equality(
                detected_file_paths=['/sdcard/Download/IMG_20190912_154245.jpg']):
            test_result = False
        if not test_assert.assert_md5_equality(detected_paths=['/sdcard/Download/IMG_20190912_154245.jpg']):
            test_result = False
    if pic_exist:
        cmd = "adb shell rm /sdcard/Download/IMG_20190912_154245.jpg"
        os.system(cmd)
    driver.quit()





