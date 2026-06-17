# -*- coding:utf8 -*-
import time
from appium import webdriver
#from appium.webdriver.common.touch_action import TouchAction
import os
import getopt
import sys
import Logger
import TestPrepare
#查看启动包名的方式：
#adb shell
#monkey -p 包名 -v -v -v 1
#appium 配置信息

#java.io.IOException
# python Controller.py -e java.io.IOException -t MaterialisticOutput -T testsuitMaterialistic.py -A materialistic
#python Controller.py -e android.database.SQLException -t MaterialisticOutput1 -T testsuitMaterialistic.py -A materialistic
#python Controller.py -e java.util.zip.DataFormatException -t MaterialisticOutput2 -T testsuitMaterialistic.py -A materialistic
opts, args = getopt.getopt(sys.argv[1:], "p:b:t:")
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


logger.begin_log()
time.sleep(5)

desired_caps = {}
desired_caps['appium-version'] = '1.0'
desired_caps['platformName'] = 'Android'
# desired_caps['platformVersion'] = '5.1.1'
# desired_caps['deviceName'] = 'Nexus 4 API 22'
desired_caps['platformVersion'] = '6.0.1'
desired_caps['deviceName'] = 'Redmi 4A'
# desired_caps['platformVersion'] = '8.0.0'
# desired_caps['deviceName'] = 'Pixel 2 API 26'
desired_caps['newCommandTimeout'] = 8000
# desired_caps['automationName'] = 'UIAutomator2'
desired_caps['noReset'] = False
desired_caps['appPackage'] = 'io.github.hidroh.materialistic'
desired_caps['appActivity'] = '.LauncherActivity'
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
#  "noReset": "True",
#  "appPackage": "io.github.hidroh.materialistic",
#  "appActivity": ".LauncherActivity",
#  "unicodeKeyboard": "True",
#  "resetKeyboard": "True"
# }

#tese
#启动webdriver执行测试脚本
try:
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)

    time.sleep(20)
    #testcase0:test fresh
    driver.swipe(start_x=650, start_y=300, end_x=650, end_y=900, duration=500)
    driver.swipe(start_x=650, start_y=300, end_x=650, end_y=900, duration=500)
    driver.swipe(start_x=650, start_y=300, end_x=650, end_y=900, duration=500)
    time.sleep(10)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_list")[0]
    el.click()
    time.sleep(8)
    driver.swipe(start_x=650, start_y=300, end_x=650, end_y=900, duration=500)
    driver.swipe(start_x=650, start_y=300, end_x=650, end_y=900, duration=500)
    driver.swipe(start_x=650, start_y=300, end_x=650, end_y=900, duration=500)
    time.sleep(8)

    el = driver.find_elements_by_class_name("android.widget.ImageView")[1]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[0]
    el.click()
    time.sleep(2)
    driver.back()
    time.sleep(2)
    #test search
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/search_button")[0]
    el.click()
    time.sleep(1)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/search_src_text")[0]
    el.clear()
    el.send_keys("Open Source Finance [pdf]")
    driver.press_keycode(66)
    time.sleep(15)
    # driver.swipe(start_x=650, start_y=300, end_x=650, end_y=900, duration=500)
    # time.sleep(10)

    #test
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/title")[0]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/bookmarked")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/bookmarked")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/bookmarked")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_readability")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_external")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[0]
    el.click()
    time.sleep(4)

    # driver.back()
    # time.sleep(3)
    driver.press_keycode(4)
    time.sleep(3)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_external")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[1]
    el.click()
    time.sleep(5)

    driver.press_keycode(4)
    time.sleep(3)

    driver.press_keycode(4)
    time.sleep(3)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_share")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[0]
    el.click()
    time.sleep(2)
    # el = driver.find_elements_by_id("android:id/icon")[0]
    # el.click()
    # time.sleep(2)

    driver.back()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(1)


    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_share")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[1]
    el.click()
    time.sleep(2)
    # el = driver.find_elements_by_id("android:id/icon")[0]
    # el.click()
    # time.sleep(2)
    driver.back()
    time.sleep(5)
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)
    driver.back()
    time.sleep(5)
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/reply_button")[0]
    # el.click()
    # time.sleep(10)
    # #test fresh
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_refresh")[0]
    # el.click()
    # time.sleep(10)
    #
    # #test find
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_find")[0]
    # el.click()
    # time.sleep(1)
    #
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/edittext")[0]
    # el.send_keys("work")
    # driver.press_keycode(66)
    # time.sleep(2)
    #
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_next")[0]
    # el.click()
    # time.sleep(2)
    #
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_clear")[0]
    # el.click()
    # time.sleep(2)
    #
    # #test zoom
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_more")[0]
    # el.click()
    # time.sleep(1)
    #
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[0]
    # el.click()
    # time.sleep(5)
    #
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_more")[0]
    # el.click()
    # time.sleep(1)
    #
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[1]
    # el.click()
    # time.sleep(5)
    #
    # #test exit
    # el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_exit")[0]
    # el.click()
    # time.sleep(5)

    #testcase1:test catch up
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_popular")[0]
    el.click()
    time.sleep(10)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_range")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/title")[0]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_range")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/title")[1]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_range")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/title")[2]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_range")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/title")[3]
    el.click()
    time.sleep(10)

    #testcase2: New stories
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_new")[0]
    el.click()
    time.sleep(10)

    #testcase3:more sections:Best stories
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_more")[0]
    el.click()
    #testcase0:test fresh
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_best")[0]
    el.click()
    time.sleep(10)

    #testcase4:more sections:Show HN
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_more")[0]
    el.click()
    #testcase0:test fresh
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_show")[0]
    el.click()
    time.sleep(10)
    #testcase5:more sections:Ask HN
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_more")[0]
    el.click()
    #testcase0:test fresh
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_ask")[0]
    el.click()
    time.sleep(10)

    #testcase6:more sections:Jobs
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_more")[0]
    el.click()
    #testcase0:test fresh
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_job")[0]
    el.click()
    time.sleep(10)

    #testcase7:Saved Stories
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_favorite")[0]
    el.click()

    #testcase8:Submit to HN
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_submit")[0]
    el.click()
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()

    #testcase9:test feedback
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_feedback")[0]
    el.click()
    time.sleep(3)
    driver.back()

    #testcase10:Settings
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_more")[0]
    el.click()
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_settings")[0]
    el.click()
    time.sleep(3)

    #test Display
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_display")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[0]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[1]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[2]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[3]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[4]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[5]
    el.click()
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/content")[0]
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(1)
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Small\")")
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[1]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Default\")")
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[2]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Top Stories\")")
    el.click()

    driver.swipe(start_x=650, start_y=900, end_x=650, end_y=300, duration=500)
    driver.swipe(start_x=650, start_y=900, end_x=650, end_y=300, duration=500)
    driver.swipe(start_x=650, start_y=900, end_x=650, end_y=300, duration=500)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[1]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[1]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[2]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[2]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[3]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[3]
    el.click()
    time.sleep(1)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"What's this?\")")
    el.click()
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[4]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[4]
    el.click()
    time.sleep(1)
    driver.back()
    time.sleep(2)


    #test List settings
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_list")[0]
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[0]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Article\")")
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[1]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Same\")")
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[1]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[1]
    el.click()
    time.sleep(1)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"What's this?\")")
    el.click()
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[2]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[2]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[2]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Save\")")
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[3]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Vote up\")")
    el.click()
    time.sleep(1)

    driver.back()
    time.sleep(1)

    #test comments settings
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_comments")[0]
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[0]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Normal\")")
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[1]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Single page - auto expand\")")
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[2]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Full content\")")
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[1]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[1]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[2]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[2]
    el.click()
    time.sleep(1)

    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)
    driver.swipe(start_x=200, start_y=900, end_x=200, end_y=300, duration=500)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[3]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[3]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[4]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[4]
    el.click()
    time.sleep(1)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"What's this?\")")
    el.click()
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(1)

    #test Readability
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/menu_readability")[0]
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[0]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Small\")")
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[1]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Default\")")
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/spinner")[2]
    el.click()
    time.sleep(1)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Normal\")")
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(1)

    #test offline
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_offline")[0]
    el.click()

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/switchWidget")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(1)

    #test About
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_about")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(1)

    #test what is New
    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/drawer_release")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("io.github.hidroh.materialistic:id/button_ok")[0]
    el.click()
    time.sleep(1)

    driver.back()
    # cmd = "adb shell rm /storage/sdcard/Download/Open_Source_Finance_v1_0.pdf"
    # os.system(cmd)
    cmd = "adb shell am broadcast -a io.github.hidroh.materialistic.pkg.END_EMMA"
    os.system(cmd)
finally:
    logger.close()
    try:
        logger.generate_log_file(triggerException=triggerException)
    finally:
        driver.quit()
