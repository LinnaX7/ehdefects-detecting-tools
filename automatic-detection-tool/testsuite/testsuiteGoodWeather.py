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

# -e java.io.IOException -t GoodWeatherOutput -T testsuitGoodWeather.py -A GoodWeather
# -e java.net.MalformedURLException -t GoodWeatherOutput1 -T testsuitGoodWeather.py -A GoodWeather
#appium 配置信息
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
desired_caps['appPackage'] = 'org.asdtm.goodweather'
desired_caps['appActivity']='.MainActivity'
desired_caps["unicodeKeyboard"] = True
desired_caps["resetKeyboard"] = True
#  "platformVersion": "5.1.1",
#  "deviceName": "Nexus 4 API 22",
# {
#  "appium-version": "1.0",
#  "platformName": "Android",
#  "platformVersion": "6.0.1",
#  "deviceName": "Redmi 4A",
#  "newCommandTimeout": "8000",
#  "noReset": "True",
#  "appPackage": "org.asdtm.goodweather",
#  "appActivity": ".MainActivity",
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

try:
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)

    time.sleep(2)

    #testcase0:refresh the weather
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/main_menu_refresh")[0]
    el.click()
    time.sleep(4)

    #testlocate:
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/main_menu_detect_location")[0]
    el.click()
    time.sleep(2)

    # el = driver.find_elements_by_id("com.android.packageinstaller:id/permission_allow_button")[0]
    # el.click()
    # time.sleep(2)
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    #testcase2: share the weather
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/fab")[0]
    el.click()
    time.sleep(2)
    # el=driver.find_elements_by_id("android:id/text1")[0]
    # el.click()
    el = driver.find_elements_by_id("android:id/icon")[2]
    el.click()
    time.sleep(2)

    driver.press_keycode(4)
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    #testcase3:current weather
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[0]
    el.click()
    time.sleep(2)

    #testcase4:graph
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[1]
    el.click()
    time.sleep(2)

    #refresh the graph
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/action_refresh")[0]
    el.click()
    time.sleep(2)

    #test: show the values
    el = driver.find_elements_by_class_name("android.widget.ImageView")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/title")[0]
    el.click()
    time.sleep(2)

    #test: hide the values
    el = driver.find_elements_by_class_name("android.widget.ImageView")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/title")[0]
    el.click()
    time.sleep(2)

    #hide the y-axis
    el = driver.find_elements_by_class_name("android.widget.ImageView")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/title")[1]
    el.click()
    time.sleep(2)

    #show the y-axis
    el = driver.find_elements_by_class_name("android.widget.ImageView")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/title")[1]
    el.click()
    time.sleep(2)

    driver.press_keycode(4)
    time.sleep(2)

    #testcase5: show Daily Forest
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[2]
    el.click()
    time.sleep(2)

    #test:refresh
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/menu_forecast_refresh")[0]
    el.click()
    time.sleep(2)

    driver.press_keycode(4)
    time.sleep(2)

    #testcase6: test donate
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[5]
    el.click()
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)

    #testcase7:test feedback
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[4]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/icon")[1]
    el.click()
    time.sleep(2)
    driver.press_keycode(4)
    time.sleep(2)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[0]
    el.click()
    time.sleep(2)
    # testcase1: test search
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/main_menu_search_city")[0]
    el.click()
    time.sleep(3)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/search_src_text")[0]
    el.send_keys("Nanjing")
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/city_name")[0]
    el.click()
    time.sleep(2)

    #testcase8: test settings
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[3]
    el.click()
    time.sleep(2)
    #test: about
    el = driver.find_elements_by_id("android:id/title")[2]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    # test settings
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[3]
    el.click()
    time.sleep(2)

    # test:deneral settings
    el = driver.find_elements_by_id("android:id/title")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/title")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/text1")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/title")[0]
    el.click()
    el = driver.find_elements_by_id("android:id/text1")[0]
    el.click()
    time.sleep(2)

    #test set hide weather description
    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()

    #test set language
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Language\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"English\")")
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    #test theme
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Theme\")")
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/text1")[0]
    el.click()
    time.sleep(2)

    # test switch
    # el = driver.find_elements_by_id("android:id/switchWidget")[0]
    # el.click()
    # time.sleep(2)
    # el = driver.find_elements_by_id("android:id/switchWidget")[0]
    # el.click()
    # time.sleep(2)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    # test settings
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("org.asdtm.goodweather:id/design_menu_item_text")[3]
    el.click()
    time.sleep(2)

    #test Widge settings
    el = driver.find_elements_by_id("android:id/title")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/title")[0]
    el.click()
    time.sleep(1)
    el = driver.find_elements_by_id("android:id/title")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("android:id/title")[1]
    el.click()
    time.sleep(1)
    el = driver.find_elements_by_id("android:id/title")[1]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("android:id/title")[2]
    el.click()
    time.sleep(1)
    el = driver.find_elements_by_id("android:id/text1")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("android:id/title")[3]
    el.click()
    time.sleep(1)
    el = driver.find_elements_by_id("android:id/text1")[2]
    el.click()
    time.sleep(1)

    # cmd = "adb shell am broadcast -a org.asdtm.goodweather.pkg.END_EMMA"
    # os.system(cmd)
    #经过检测可以删除
    driver.back()

except BaseException as e:
    traceback.print_tb()
finally:
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








