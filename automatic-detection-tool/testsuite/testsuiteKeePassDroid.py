# -*- coding:utf8 -*-
import time
from appium import webdriver
from appium.webdriver.common.touch_action import TouchAction
import os
import getopt
import TestPrepare
import FileAssert
import sys
import Logger

opts, args = getopt.getopt(sys.argv[1:], "p:b:t:")
for op, value in opts:
    if op == "-p":
        test_package=value
    elif op == "-b":
        test_base_dir = value
    elif op == '-t':
        triggerException = value

output_tag = os.path.basename(test_base_dir)
'''
iscreated = False
if output_tag is '0':
    iscreated = True
'''
output_file = os.path.join(os.path.dirname(test_base_dir), 'finalresult.out')

logger = Logger.Logger(output_dir=test_base_dir, app_package_name=test_package)

pre_conductor = TestPrepare.Preparer(err_log_file=os.path.join(test_base_dir,"err.out"),
                                     base_dir="/storage/emulated/0/keepass/", initial_dir=test_base_dir)
logger.begin_log()
time.sleep(5)
test_assert = FileAssert.FileAssert(base_Dir=test_base_dir, tag='keepassdroid')

#appium 配置信息
desired_caps = {}
desired_caps['appium-version'] = '1.0'
desired_caps['platformName'] = 'Android'
# desired_caps['platformVersion'] = '5.1.1'
# desired_caps['deviceName'] = 'Nexus 4 API 22'
desired_caps['platformVersion'] = '6.0.1'
desired_caps['deviceName'] = 'Redmi 4A'
desired_caps['newCommandTimeout'] = 8000
# desired_caps['automationName'] = 'UIAutomator2'
desired_caps['noReset'] = True
desired_caps['appPackage'] = 'com.android.keepass'
desired_caps['appActivity']='.KeePass'
desired_caps["unicodeKeyboard"] = True
desired_caps["resetKeyboard"] = True
desired_caps["noReset"] = False
#  "platformVersion": "5.1.1",
#  "deviceName": "Nexus 4 API 22",
# {
#  "appium-version": "1.0",
#  "platformName": "Android",
#  "platformVersion": "6.0.1",
#  "deviceName": "Redmi 4A",
#  "newCommandTimeout": "8000",
#  "noReset": "True",
#  "appPackage": "com.android.keepass",
#  "appActivity": ".KeePass",
#  "unicodeKeyboard": "True",
#  "resetKeyboard": "True"
# }
#python Controller.py -e android.database.SQLException -t KeePassDroidOutput1 -T testsuitKeePassDroid.py -A keePass
#tese
#启动webdriver执行测试脚本
database1_exist = False
database2_exist = False
final_result = True

try:
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)

    time.sleep(2)


    # el = driver.find_elements_by_id("com.android.keepass:id/file_filename")[0]
    # el.click()
    # el = driver.find_elements_by_id("com.android.keepass:id/password")[0]
    # el.send_keys("userChange")
    # el = driver.find_elements_by_id("com.android.keepass:id/pass_ok")[0]
    # el.click()
    # time.sleep(2)
    # driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    # driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    # driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)

    #create database without keyfile
    el = driver.find_elements_by_id("com.android.keepass:id/file_filename")[0]
    el.clear()

    el.send_keys("/storage/emulated/0/keepass/keepass.kdbx")
    el = driver.find_elements_by_id("com.android.keepass:id/create")[0]
    el.click()
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/pass_password")[0]
    el.send_keys("user")
    el = driver.find_elements_by_id("com.android.keepass:id/pass_conf_password")[0]
    el.send_keys("user")
    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)
    el = driver.find_elements_by_id("com.android.keepass:id/file_filename")[0]
    TouchAction(driver).long_press(el).perform()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Remove\")")
    el.click()

    keepass_kdbx = True
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepass.kdbx']):
        database1_exist = True
        #print("database1_exist")
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepass.kdbx']):
            keepass_kdbx = False
    final_result = keepass_kdbx and final_result
    if keepass_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database1 0'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database1 0'))
    #test:test about

    el = driver.find_elements_by_id("com.android.keepass:id/menu_about")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/about_button")[0]
    el.click()
    time.sleep(1)
    #testcase:set settings
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    # el = driver.find_elements_by_id("com.android.keepass:id/menu_app_settings")[0]
    # el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Application\")")
    el.click()

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Clipboard timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"30 seconds\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Clipboard timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"1 minute\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Clipboard timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"5 minutes\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Clipboard timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Never\")")
    el.click()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Application timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"30 seconds\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Application timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"1 minute\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Application timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"5 minutes\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Application timeout\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Never\")")
    el.click()

    el = driver.find_elements_by_id("android:id/checkbox")[2]
    el.click()
    el = driver.find_elements_by_id("android:id/checkbox")[2]
    el.click()
    el = driver.find_elements_by_id("android:id/checkbox")[3]
    el.click()
    el = driver.find_elements_by_id("android:id/checkbox")[3]
    el.click()
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Group list size\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Small\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Group list size\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Medium\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Group list size\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Large\")")
    el.click()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Key encryption rounds before corruption\")")
    el.click()
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Key encryption rounds before corruption\")")
    el.click()
    el = driver.find_elements_by_id("android:id/edit")[0]
    el.clear()
    el.send_keys("2")
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()

    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)

    #testcase0:create database
    el = driver.find_elements_by_id("com.android.keepass:id/create")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/file_filename")[0]
    el.clear()
    el.send_keys("/storage/emulated/0/keepass/keepassTest.kdbx")
    el = driver.find_elements_by_id("com.android.keepass:id/open")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/create")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()

    keepassTest_kdbx = True
    database2_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
        database2_exist = True
        #print("datebas2_exist")
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
            keepassTest_kdbx = False
    final_result = keepassTest_kdbx and final_result
    if keepassTest_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database2 0'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database2 0'))

    el = driver.find_elements_by_id("com.android.keepass:id/pass_password")[0]
    el.send_keys("user")
    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/pass_conf_password")[0]
    el.send_keys("user1")
    el = driver.find_elements_by_id("com.android.keepass:id/pass_keyfile")[0]
    el.send_keys("/storage/emulated/0/keepass/keepass1.kdbx")
    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("com.android.keepass:id/pass_conf_password")[0]
    el.send_keys("user")
    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/pass_keyfile")[0]
    el.clear()
    el.send_keys("/storage/emulated/0/keepass/keepass.kdbx")
    time.sleep(1)

    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)
    #testcase1:open database
    el = driver.find_elements_by_id("com.android.keepass:id/browse_button")[0]
    el.click()
    time.sleep(2)
    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Internal storage\")")
    # el.click()
    # time.sleep(2)
    # driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    # driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"keepass\")")
    # el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"keepassTest.kdbx\")")
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/open")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/default_database")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/default_database")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/pass_ok")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("com.android.keepass:id/password")[0]
    el.send_keys("user1")
    el = driver.find_elements_by_id("com.android.keepass:id/pass_keyfile")[0]
    el.send_keys("/storage/emulated/0/keepass/keepass1.kdbx")
    el = driver.find_elements_by_id("com.android.keepass:id/pass_ok")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/password")[0]
    el.send_keys("user")
    el = driver.find_elements_by_id("com.android.keepass:id/pass_ok")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/pass_keyfile")[0]
    el.clear()
    el.send_keys("/storage/emulated/0/keepass/keepass.kdbx")
    el = driver.find_elements_by_id("com.android.keepass:id/pass_ok")[0]
    el.click()
    time.sleep(2)
    # testcase2:add group then cancel
    el = driver.find_elements_by_id("com.android.keepass:id/add_group")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/group_name")[0]
    el.send_keys("WebGroup")
    el.send_keys("WebGroup")

    el = driver.find_elements_by_id("com.android.keepass:id/cancel")[0]
    el.click()
    time.sleep(2)
    #testcase3:add group then ok
    el = driver.find_elements_by_id("com.android.keepass:id/add_group")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/group_name")[0]
    el.send_keys("WebGroup")
    el = driver.find_element_by_id("com.android.keepass:id/icon_button")
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/icon_image")[1]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    time.sleep(2)
    keepassTest_kdbx = True
    database2_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
        database2_exist = True
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx'],
                                                update_time=1):
            keepassTest_kdbx = False
    final_result = keepassTest_kdbx and final_result
    if keepassTest_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database2 1'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database2 1'))

    #testcase4: add entry
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"WebGroup\")")
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/add_entry")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/entry_cancel")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/add_entry")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/entry_save")[0]
    el.click()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"name\")")
    el.send_keys("Twitter")
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"username\")")
    el.send_keys("TherobotONO")
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"url\")")
    el.send_keys("http://twitter.com")
    el = driver.find_elements_by_id("com.android.keepass:id/entry_password")[0]
    el.send_keys("Aa719588417")
    el = driver.find_elements_by_id("com.android.keepass:id/entry_confpassword")[0]
    el.send_keys("Aa71958841")
    el = driver.find_elements_by_id("com.android.keepass:id/menu_toggle_pass")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/menu_toggle_pass")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/entry_save")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/entry_confpassword")[0]
    el.clear()
    el.send_keys("Aa719588417")
    el = driver.find_elements_by_id("com.android.keepass:id/entry_save")[0]
    el.click()
    time.sleep(2)
    keepassTest_kdbx = True
    database2_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
        database2_exist = True
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx'],
                                                update_time=2):
            keepassTest_kdbx = False
    final_result = keepassTest_kdbx and final_result
    if keepassTest_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database2 2'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database2 2'))

    #testcase5: edit entry
    el = driver.find_elements_by_class_name("android.widget.TableRow")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/entry_edit")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/generate_button")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/btn_length6")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/generate_password_button")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/btn_length8")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/generate_password_button")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/btn_length12")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/generate_password_button")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/btn_length16")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/generate_password_button")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/cb_minus")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/cb_underline")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/cb_space")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/cb_specials")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/cb_brackets")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/generate_password_button")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/accept_button")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/add_advanced")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/delete")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/add_advanced")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/entry_save")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Field Name\")")
    el.send_keys("addItem")
    el = driver.find_elements_by_id("com.android.keepass:id/protection")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/protection")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("com.android.keepass:id/value")[0]
    el.send_keys("addstring")

    el = driver.find_elements_by_id("com.android.keepass:id/entry_save")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Copy User\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Copy Password\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Go to URL\")")
    el.click()
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)

    keepassTest_kdbx = True
    database2_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
        database2_exist = True
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx'],
                                                update_time=3):
            keepassTest_kdbx = False
    final_result = keepassTest_kdbx and final_result
    if keepassTest_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database2 3'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database2 3'))

    #testcase:test search

    el = driver.find_elements_by_id("com.android.keepass:id/menu_search")[0]
    el.click()
    el = driver.find_elements_by_id("android:id/search_src_text")[0]
    el.send_keys("Twitter")
    time.sleep(1)

    driver.press_keycode(66)
    time.sleep(1)
    driver.press_keycode(4)
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/menu_search")[0]
    el.click()
    el = driver.find_elements_by_id("android:id/search_src_text")[0]
    el.send_keys("QQ")
    time.sleep(1)

    driver.press_keycode(66)
    time.sleep(1)
    driver.press_keycode(4)
    time.sleep(2)

    #testcase:delete entry
    el = driver.find_elements_by_id("com.android.keepass:id/entry_text")[0]
    TouchAction(driver).long_press(el).perform()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Delete\")")
    el.click()
    time.sleep(2)
    #testcase:test lock
    el = driver.find_elements_by_id("com.android.keepass:id/menu_lock")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/password")[0]
    el.send_keys("user")

    el = driver.find_elements_by_id("com.android.keepass:id/show_password")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/show_password")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/pass_ok")[0]
    el.click()
    time.sleep(2)
    keepassTest_kdbx = True
    database2_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
        database2_exist = True
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx'],
                                                update_time=4):
            keepassTest_kdbx = False
    final_result = keepassTest_kdbx and final_result
    if keepassTest_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database2 4'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database2 4'))

    #testcase: change Master Key
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Change Master Key\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.android.keepass:id/pass_password")[0]
    el.send_keys("userChange")

    el = driver.find_elements_by_id("com.android.keepass:id/pass_conf_password")[0]
    el.send_keys("userChange")
    time.sleep(2)
    el = driver.find_elements_by_id("com.android.keepass:id/pass_keyfile")[0]
    el.send_keys("/storage/emulated/0/keepass/keepass.kdbx")

    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    time.sleep(2)

    keepassTest_kdbx = True
    database2_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
        database2_exist = True
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx'],
                                                update_time=5):
            keepassTest_kdbx = False
    final_result = keepassTest_kdbx and final_result
    if keepassTest_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database2 5'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database2 5'))

    #testcase:sort

    el = driver.find_elements_by_id("com.android.keepass:id/add_group")[0]
    el.click()

    el = driver.find_elements_by_id("com.android.keepass:id/group_name")[0]
    el.send_keys("AppGroup")
    el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"DB sort order\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Sort by name\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"RecycleBin\")")
    el.click()
    time.sleep(1)
    el = driver.find_elements_by_id("com.android.keepass:id/entry_text")[0]
    TouchAction(driver).long_press(el).perform()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Delete\")")
    el.click()
    time.sleep(1)
    driver.press_keycode(4)
    time.sleep(2)
    #testcase:test donate

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Donate\")")
    el.click()
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(1)
    #driver.press_keycode(4)
    driver.press_keycode(4)
    time.sleep(1)
    driver.press_keycode(4)
    time.sleep(1)

    #testcase: remove history
    el = driver.find_elements_by_id("com.android.keepass:id/file_filename")[0]
    TouchAction(driver).long_press(el).perform()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Remove\")")
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/file_filename")[0]
    el.click()
    el = driver.find_elements_by_id("com.android.keepass:id/password")[0]
    el.send_keys("userChange")
    el = driver.find_elements_by_id("com.android.keepass:id/pass_ok")[0]
    el.click()
    time.sleep(2)

    # for i in range(10):
    #     el = driver.find_elements_by_id("com.android.keepass:id/add_group")[0]
    #     el.click()
    #     el = driver.find_elements_by_id("com.android.keepass:id/group_name")[0]
    #     el.send_keys(str(i))
    #     el = driver.find_elements_by_id("com.android.keepass:id/ok")[0]
    #     el.click()
    # time.sleep(2)
    #
    # driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    # driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    # driver.swipe(start_x=280, start_y=800, end_x=280, end_y=300, duration=500)
    # time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)
    #driver.press_keycode(4)
    el = driver.find_elements_by_id("com.android.keepass:id/file_filename")[0]
    TouchAction(driver).long_press(el).perform()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Remove\")")
    el.click()
    time.sleep(2)
    keepassTest_kdbx = True
    database2_exist = False
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx']):
        database2_exist = True
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/keepass/keepassTest.kdbx'],
                                                update_time=6):
            keepassTest_kdbx = False
    final_result = keepassTest_kdbx and final_result
    if keepassTest_kdbx is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'database2 6'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'database2 6'))

finally:
    '''
    #删除文件使得恢复到原来的样子
    cmd = "adb shell rm /storage/emulated/0/keepass/keepassTest.kdbx"
    #cmd = "adb shell rm /storage/sdcard/keepass/keepassTest.kdbx"
    os.system(cmd)
    cmd = "adb shell rm /storage/emulated/0/keepass/keepass.kdbx"
    #cmd = "adb shell rm /storage/sdcard/keepass/keepass.kdbx"
    os.system(cmd)
    cmd = "adb shell am broadcast -a com.keepassdroid.pkg.END_EMMA"
    os.system(cmd)
    '''
    time.sleep(5)
    logger.close()
    logger.generate_log_file(triggerException=triggerException)
    if database1_exist:
        pre_conductor.clear(['/storage/emulated/0/keepass/keepass.kdbx'], ifdir=False)
        cmd = "adb shell rm /storage/emulated/0/keepass/keepass.kdbx"
        os.system(cmd)
    if database2_exist:
        pre_conductor.clear(['/storage/emulated/0/keepass/keepassTest.kdbx'], ifdir=False)
        cmd = "adb shell rm /storage/emulated/0/keepass/keepassTest.kdbx"
        os.system(cmd)

    driver.back()
    driver.quit()

    #经过检测可以删除




