# -*- coding:utf8 -*-
import time
from appium import webdriver
from appium.webdriver.common.touch_action import TouchAction
import os
import getopt
import traceback
import sys
import FileAssert
import Logger
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
test_assert = FileAssert.FileAssert(base_Dir=test_base_dir, tag='K9')

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
desired_caps['noReset'] = False
desired_caps['appPackage'] = 'com.fsck.k9.material.debug'
desired_caps['appActivity'] = 'com.fsck.k9.activity.Accounts'
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
#  "appPackage": "com.paulds.simpleftp",
#  "appActivity": ".presentation.activities.SplashActivity",
#  "unicodeKeyboard": "True",
#  "resetKeyboard": "True"
# }

#tese
#启动webdriver执行测试脚本
#python Controller.py -e java.io.IOException -t K9Output -T testsuitK9.py -A K9
file_1_exist = False
file_2_exist = False

try:
    # tese
    # 启动webdriver执行测试脚本
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)

    time.sleep(4)
    # testcase0:test setup
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/next")[0]
    el.click()

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/account_email")[0]
    el.clear()
    el.send_keys("user7458@163.com")
    time.sleep(2)
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/account_password")[0]
    el.clear()
    el.send_keys("Aa719588417")
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/show_password")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/show_password")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/next")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/pop")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/next")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/next")[0]
    el.click()
    time.sleep(2)

    # test set poll frequency
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/account_check_frequency")[0]
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Every minute\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/next")[0]
    el.click()
    time.sleep(2)

    # test:account_name
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/account_description")[0]
    el.clear()
    el.send_keys("forTest")

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/account_name")[0]
    el.clear()
    el.send_keys("userTest")

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/done")[0]
    el.click()
    time.sleep(2)

    # test click ok
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)
    # testcase1: test fresh
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/check_mail")[0]
    el.click()
    time.sleep(2)

    # testcase2:test send email and draft
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/compose")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/to")[0]
    el.send_keys("user7458@163.com")

    # test click Allow
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/subject")[0]
    el.send_keys("mailForTest1")

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/message_content")[0]
    el.clear()
    el.send_keys("mail without file")
    time.sleep(2)

    driver.back()
    time.sleep(3)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # send email
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/accounts_item_layout")[2]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/folder_list_item_layout")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/subject")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/to")[0]
    el.clear()
    el.send_keys("user7458@163.com")

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/send")[0]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    # fresh again
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/check_mail")[0]
    el.click()
    time.sleep(3)

    # testcase3: send email with file
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/compose")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/to")[0]
    el.clear()
    el.send_keys("user7458@163.com")

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/subject")[0]
    el.clear()
    el.send_keys("mailForTest2")

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/message_content")[0]
    el.clear()
    el.send_keys("mail with file")

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/add_attachment")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()

    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Photos\")")
    # el.click()
    #
    # el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Camera\")")
    # el.click()
    #
    # el = driver.find_elements_by_class_name("android.view.ViewGroup")[0]
    # el.click()
    # time.sleep(2)
    # el = driver.find_elements_by_id("com.google.android.apps.photos:id/done_button")[0]
    # el.click()
    # time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Gallery\")")
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/button2")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/send")[0]
    el.click()
    time.sleep(10)

    # fresh again
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/check_mail")[0]
    el.click()
    time.sleep(3)

    # testcase: search mail
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/search")[0]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/search_src_text")[0]
    el.send_keys("mailForTest")
    driver.press_keycode(66)
    time.sleep(3)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    # testcase4: test download file
    # /sdcard/Download/IMG_20190912_154245.jpg
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/accounts_item_layout")[2]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/check_mail")[0]
    el.click()
    time.sleep(10)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/subject")[0]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/download")[0]
    el.click()
    time.sleep(2)

    # testcase5: test star
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/flagged")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    # back to folder
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    # testcase: test sort by date
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/folder_list_item_layout")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Sort by…\")")
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Date\")")
    el.click()
    time.sleep(2)

    # test mark as read

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/subject")[0]
    TouchAction(driver).long_press(el).perform()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Mark as read\")")
    el.click()
    time.sleep(2)

    # testcase: test sort by read or unread
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Sort by…\")")
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Read/unread\")")
    el.click()
    time.sleep(2)

    # test sort by star
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Sort by…\")")
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Star\")")
    el.click()
    time.sleep(2)

    # test sort by attachments
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Sort by…\")")
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Attachments\")")
    el.click()
    time.sleep(2)
    # testcase: first set delete file
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Account settings\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Fetching mail\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"When I delete a message\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Delete from server\")")
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/subject")[0]
    TouchAction(driver).long_press(el).perform()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Delete\")")
    el.click()
    time.sleep(2)
    # testcase6: test delete mail
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Select all\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/delete")[0]
    el.click()
    time.sleep(3)

    # back to folder
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    # testcase7:clear trash
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Empty Trash\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    # testcase: clear message
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/accounts_item_layout")[2]
    TouchAction(driver).long_press(el).perform()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Clear messages (danger!)\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # testcase: test about
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"About\")")
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # testcase:test folder settings
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/accounts_item_layout")[2]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Folder settings\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Folder display class\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"1st Class\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Folder sync class\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"1st Class\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Folder notification class\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"1st Class\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[1]
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(3)

    # test Account settings
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Account settings\")")
    el.click()
    time.sleep(2)

    # test General settings
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"General settings\")")
    el.click()
    time.sleep(3)

    # test rename account name
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Account name\")")
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("android:id/edit")[0]
    el.clear()
    el.send_keys("forTestRename")

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Account color\")")
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/okColorButton")[0]
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    # test Read mail
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Reading mail\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Always show images\")")
    el.click()
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"No\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    # test Fetching mail
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Fetching mail\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Local folder size\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"25 messages\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Fetch messages up to\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"32Kb\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Fetch messages up to\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"32Kb\")")
    el.click()
    time.sleep(3)

    driver.back()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    # testcase: test export
    # /sdcard/com.fsck.k9.material.debug/settings.k9s
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings Import & Export\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Export settings and accounts\")")
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(3)

    # testcase: test settings
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Display\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Language\")")
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"System default\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Theme\")")
    el.click()
    time.sleep(3)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Light\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Message view theme\")")
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Use app theme\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Composer theme\")")
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Use app theme\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Font size\")")
    el.click()
    time.sleep(2)
    driver.back()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[1]
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    driver.back()
    time.sleep(2)
    # test interaction
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Interaction\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[1]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Volume key navigation\")")
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[2]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[2]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[3]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[3]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Confirm actions\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    driver.back()
    time.sleep(2)
    # test Notifications
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Notifications\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Show 'Delete' button\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Never\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Lock Screen Notifications\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Unread message count\")")
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    driver.back()
    time.sleep(2)
    # test network
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Network\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Background sync\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Always\")")
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    driver.back()
    time.sleep(2)
    # test Miscellaneous
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Miscellaneous\")")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Save attachments to…\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    driver.back()
    time.sleep(2)

    driver.back()
    time.sleep(2)
    # testcase: remove account
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/accounts_item_layout")[2]
    TouchAction(driver).long_press(el).perform()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Remove account\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # test import
    el = driver.find_elements_by_class_name("android.widget.ImageButton")[0]
    el.click()
    time.sleep(2)
    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Settings Import & Export\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Import settings\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"File Manager\")")
    el.click()
    time.sleep(3)

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"com.fsck.k9.material.debug\")")
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id("android:id/checkbox")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_class_name("android.widget.Button")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/incoming_server_password")[0]
    el.send_keys("Aa719588417")
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)

    # testcase: remove account
    el = driver.find_elements_by_id("com.fsck.k9.material.debug:id/accounts_item_layout")[2]
    TouchAction(driver).long_press(el).perform()

    el = driver.find_element_by_android_uiautomator("new UiSelector().text(\"Remove account\")")
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("android:id/button1")[0]
    el.click()
    time.sleep(2)
    # exit
except BaseException as e:
    traceback.print_tb()
finally:
    try:
        driver.quit()
    except BaseException as e:
        traceback.print_tb()
    finally:
        try:
            logger.close()
            logger.generate_log_file(triggerException=triggerException)
            if test_assert.assert_file_existence(expected_file_paths=['/sdcard/Download/IMG_20190912_154245.jpg']):
                file_1_exist = True
                if not test_assert.assert_size_equality(detected_file_paths=['/sdcard/Download/IMG_20190912_154245.jpg']):
                    test_result = False
                if not test_assert.assert_md5_equality(detected_paths=['/sdcard/Download/IMG_20190912_154245.jpg']):
                    test_result = False
            if test_result is False:
                with open(output_file, 'a+') as fps:
                    fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'download'))
            else:
                with open(output_file, 'a+') as fps:
                    fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'download'))

            test_result = True
            if test_assert.assert_file_existence(expected_file_paths=['/sdcard/com.fsck.k9.material.debug/settings.k9s']):
                file_2_exist = True
                if not test_assert.assert_size_equality(
                        detected_file_paths=['/sdcard/com.fsck.k9.material.debug/settings.k9s']):
                    test_result = False
                if not test_assert.assert_md5_equality(detected_paths=['/sdcard/com.fsck.k9.material.debug/settings.k9s']):
                    test_result = False
            if test_result is False:
                with open(output_file, 'a+') as fps:
                    fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'settings'))
            else:
                with open(output_file, 'a+') as fps:
                    fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'settings'))
        except BaseException as e:
            traceback.print_tb()
        finally:
            cmd = "adb shell rm /sdcard/Download/IMG_20190912_154245.jpg"
            os.system(cmd)
            cmd = "adb shell rm /sdcard/com.fsck.k9.material.debug/settings.k9s"
            os.system(cmd)

