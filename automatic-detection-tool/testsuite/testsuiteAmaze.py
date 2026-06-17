# coding=utf-8
import time
import FileAssert
import getopt
import sys
import json
import Logger
import os
import TestPrepare
from appium import webdriver
from appium.webdriver.common.touch_action import TouchAction

desired_caps = {}
desired_caps['appium-version'] = '1.0'
desired_caps['platformName'] = 'Android'
desired_caps['platformVersion'] = '6.0.1'
desired_caps['deviceName'] = 'Redmi'
desired_caps['newCommandTimeout'] = 8000
# desired_caps['automationName'] = 'UIAutomator2'
desired_caps['noReset'] = True
desired_caps['appPackage'] = 'com.amaze.filemanager'
desired_caps['appActivity']='.activities.MainActivity'
desired_caps["unicodeKeyboard"] = True
desired_caps["resetKeyboard"] = True

# {
#  "appium-version": "1.0",
#  "platformName": "Android",
#  "platformVersion": "",
#  "deviceName": "",
#  "newCommandTimeout": "8000",
#  "noReset": "True",
#  "appPackage": "com.amaze.filemanager",
#  "appActivity": ".activities.MainActivity",
#  "unicodeKeyboard": "True",
#  "resetKeyboard": "True"
# }
# 查看启动包名的方式：
# adb shell
# monkey -p 包名 -v -v -v 1
# -e java.io.IOException -t AmazeOutput -T testsuite.py -A Amaze
# -e java.security.NoSuchAlgorithmException -t AmazeOutput1 -T testsuite.py -A Amaze
# -e java.io.FileNotFoundException -t AmazeOutput2 -T testsuite.py -A Amaze
#
opts, args = getopt.getopt(sys.argv[1:], "p:b:t:")
for op, value in opts:
    if op == "-p":
        test_package=value
    elif op == "-b":
        test_base_dir = value
    elif op == "-t":
        triggerException = value


output_tag = os.path.basename(test_base_dir)
iscreated = False
if output_tag is '0':
    iscreated = True
output_file = os.path.join(os.path.dirname(test_base_dir), 'finalresult.out')

logger = Logger.Logger(output_dir=test_base_dir, app_package_name=test_package)

pre_conductor = TestPrepare.Preparer(err_log_file=os.path.join(test_base_dir,"err.out"),
                                     base_dir="/storage/emulated/0",initial_dir=test_base_dir)
logger.begin_log()
time.sleep(5)
test_assert = FileAssert.FileAssert(base_Dir=test_base_dir, tag=test_package)

driver = None
ziptest_exist = False
testzip_exist = False
testbase_exist = False
movetest_exist =  False
renametest_exist = False
deletetest_exist = False
try:
    driver = webdriver.Remote("http://127.0.0.1:4723/wd/hub", desired_caps)
    driver.implicitly_wait(20)
    n = 0

    el = driver.find_elements_by_class_name('android.widget.ImageButton')[1]
    el.click()

    el = driver.find_element_by_id('com.amaze.filemanager:id/design_menu_item_text')
    el.click()

    time.sleep(2)




    # testcase1:unzip
    '''
    ta = TouchAction(driver)
    ta.long_press(x=280, y=1200, duration=500).move_to(x=280, y=300).release().perform()
    ta.long_press(x=280, y=1200, duration=500).move_to(x=280, y=300).release().perform()
    '''
    driver.swipe(start_x=280, start_y=1200, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=1200, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=1200, end_x=280, end_y=300, duration=500)
    driver.swipe(start_x=280, start_y=1200, end_x=280, end_y=300, duration=500)
    time.sleep(2)
    length = len(driver.find_elements_by_id("com.amaze.filemanager:id/properties"))
    el = driver.find_elements_by_id("com.amaze.filemanager:id/properties")[length - 1]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/title")[8]
    el.click()
    time.sleep(3)
    print('end of unzip')

    # testcase2: compress
    #ta.long_press(x=280, y=1200, duration=500).move_to(x=280, y=300).release().perform()
    driver.swipe(start_x=280,start_y=300,end_x=280,end_y=1000,duration=500)
    time.sleep(2)
    el = driver.find_elements_by_class_name('android.widget.ImageButton')[1]
    el.click()

    el = driver.find_element_by_id('com.amaze.filemanager:id/design_menu_item_text')
    el.click()

    time.sleep(3)

    # test case5: 当前文件夹，长按第一项，压缩
    el = driver.find_element_by_id("com.amaze.filemanager:id/generictext")
    el.click()
    time.sleep(2)

    el = driver.find_element_by_xpath('//android.widget.ImageView[@content-desc="更多选项"]')
    el.click()

    time.sleep(1)

    el = driver.find_elements_by_id("com.amaze.filemanager:id/title")[1]
    el.click()

    el = driver.find_element_by_id("com.amaze.filemanager:id/md_root")
    el.send_keys("test")

    el = driver.find_element_by_id("com.amaze.filemanager:id/md_buttonDefaultPositive")
    el.click()
    time.sleep(2)
    print('end of compress')

    #testcase3 copy
    el = driver.find_elements_by_class_name('android.widget.ImageButton')[1]
    el.click()

    el = driver.find_element_by_id('com.amaze.filemanager:id/design_menu_item_text')
    el.click()

    time.sleep(2)

    el = driver.find_elements_by_id("com.amaze.filemanager:id/properties")[0]
    el.click()
    time.sleep(1)

    el = driver.find_elements_by_id("android:id/title")[1]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id("com.amaze.filemanager:id/second")[2]
    el.click()
    time.sleep(2)
    el = driver.find_element_by_id("com.amaze.filemanager:id/paste")
    el.click()
    time.sleep(2)
    print('end of copy')

    #testcase4 rename
    el = driver.find_element_by_xpath('//android.widget.ImageButton[@content-desc="转到上一层级"]')
    el.click()
    time.sleep(1)

    el = driver.find_element_by_id("com.amaze.filemanager:id/design_menu_item_text")
    el.click()
    time.sleep(3)

    driver.swipe(start_x=280, start_y=500, end_x=280, end_y=200, duration=800)
    el = driver.find_elements_by_id('com.amaze.filemanager:id/properties')[4]
    el.click()
    time.sleep(2)
    el = driver.find_elements_by_id("android:id/title")[3]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_id("com.amaze.filemanager:id/md_root")
    el.clear()
    el.send_keys("test")

    el = driver.find_element_by_id("com.amaze.filemanager:id/md_buttonDefaultPositive")
    el.click()
    time.sleep(3)

    print('end of rename')

    #testcase5 move
    driver.swipe(start_x=280, start_y=200, end_x=280, end_y=500, duration=800)
    el = driver.find_element_by_xpath('//android.widget.ImageButton[@content-desc="转到上一层级"]')
    el.click()
    time.sleep(1)

    el = driver.find_element_by_id("com.amaze.filemanager:id/design_menu_item_text")
    el.click()
    time.sleep(3)
    el = driver.find_elements_by_id('com.amaze.filemanager:id/second')[4]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id('com.amaze.filemanager:id/properties')[0]
    el.click()
    time.sleep(2)

    el = driver.find_elements_by_id('android:id/title')[0]
    el.click()
    time.sleep(2)

    el = driver.find_element_by_xpath('//android.widget.ImageButton[@content-desc="转到上一层级"]')
    el.click()
    time.sleep(1)

    el = driver.find_element_by_id("com.amaze.filemanager:id/design_menu_item_text")
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id('com.amaze.filemanager:id/second')[5]
    el.click()
    time.sleep(3)

    el = driver.find_element_by_id('com.amaze.filemanager:id/paste')
    el.click()
    time.sleep(3)

    print('end of move')

    #testcase6 delete
    el = driver.find_element_by_xpath('//android.widget.ImageButton[@content-desc="转到上一层级"]')
    el.click()
    time.sleep(1)

    el = driver.find_element_by_id("com.amaze.filemanager:id/design_menu_item_text")
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id('com.amaze.filemanager:id/properties')[2]
    el.click()
    time.sleep(3)

    el = driver.find_elements_by_id('android:id/title')[2]
    el.click()
    time.sleep(3)

    el = driver.find_element_by_id('com.amaze.filemanager:id/md_buttonDefaultPositive')
    el.click()
    time.sleep(3)

    print('end of delete')

    driver.quit()

finally:
    time.sleep(10)
    logger.close()
    logger.generate_log_file(triggerException=triggerException)
    time.sleep(5)
    # assert phase
    condition = 0
    condition1 = True
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/ziptest']):
        ziptest_exist = True
        if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/ziptest/123_test_base/1.png']):
            condition = condition + 1
        if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/ziptest/123_test_base/1.jpg']):
            condition = condition + 2
        if condition == 1:
            if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/ziptest/123_test_base/1.png']):
                condition1 = False
            if not test_assert.assert_owner_equality(detected_file_paths=['/storage/emulated/0/ziptest//123_test_base/1.png']):
                condition1 = False
            if not test_assert.assert_md5_equality(detected_paths=['/storage/emulated/0/ziptest//123_test_base/1.png']):
                condition1 = False
        if condition == 2:
            if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/ziptest//123_test_base/1.jpg']):
                condition1 = False
            if not test_assert.assert_owner_equality(detected_file_paths=['/storage/emulated/0/ziptest//123_test_base/1.jpg']):
                condition1 = False
            if not test_assert.assert_md5_equality(detected_paths=['/storage/emulated/0/ziptest/123_test_base/1.jpg']):
                condition1 = False
        if condition == 3:
            if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/ziptest/123_test_base/1.png',
                                                                         '/storage/emulated/0/ziptest/123_test_base/1.jpg']):
                condition1 = False
            if not test_assert.assert_owner_equality(detected_file_paths=['/storage/emulated/0/ziptest/123_test_base/1.png',
                                                                          '/storage/emulated/0/ziptest/123_test_base/1.jpg']):
                condition1 = False
            if not test_assert.assert_md5_equality(detected_paths=['/storage/emulated/0/ziptest/123_test_base/1.png',
                                                                   '/storage/emulated/0/ziptest/123_test_base/1.jpg']):
                condition1 = False


    if condition1 is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'unzip'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'unzip'))
    print('end of unzip assert')

    condition = True
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/test.zip']):
        testzip_exist = True
        if not test_assert.assert_md5_equality(detected_paths=['/storage/emulated/0/test.zip'],
                                               is_zip=True, pulling_path=test_base_dir,
                                               dir_name="123_test_base", file_names=["1.jpg", "1.png"]):
            condition = False
        time.sleep(1)
        if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/test.zip']):
            condition = False
        if not test_assert.assert_owner_equality(detected_file_paths=['/storage/emulated/0/test.zip']):
            condition = False
    if condition is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'compress'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'compress'))

    print('end of compress assert')
    condition = 0
    condition1 = True

    if test_assert.assert_file_existence(
            expected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base"]):
        testbase_exist = True
        if test_assert.assert_file_existence(
                expected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.png"]):
            condition = 1
        if test_assert.assert_file_existence(
                expected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.jpg"]):
            condition = condition + 2
        if condition == 1:
            if not test_assert.assert_size_equality(
                    detected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.png"]):
                condition1 = False
            if not test_assert.assert_owner_equality(
                    detected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.png"]):
                condition1 = False
            if not test_assert.assert_md5_equality(
                    detected_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.png"]):
                condition1 = False
        if condition == 2:
            if not test_assert.assert_size_equality(
                    detected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.jpg"]):
                condition1 = False
            if not test_assert.assert_owner_equality(
                    detected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.jpg"]):
                condition1 = False
            if not test_assert.assert_md5_equality(
                    detected_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.jpg"]):
                condition1 = False
        if condition == 3:
            if not test_assert.assert_size_equality(
                    detected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.jpg",
                                         "/storage/emulated/0/123_test_copy/123_test_base/1.png"]):
                condition1 = False
            if not test_assert.assert_owner_equality(
                    detected_file_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.jpg",
                                         "/storage/emulated/0/123_test_copy/123_test_base/1.png"]):
                condition1 = False
            if not test_assert.assert_md5_equality(
                    detected_paths=["/storage/emulated/0/123_test_copy/123_test_base/1.jpg",
                                    "/storage/emulated/0/123_test_copy/123_test_base/1.png"]):
                condition1 = False

    if condition1 is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'copy2'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'copy2'))
    print('end of copy assert')

    condition = True
    count = 0
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/test']):
        renametest_exist = True
        if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/test/1.png']):
            count = 1
        if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/test/1.jpg']):
            count = count + 2
        if count == 1:
            if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/test/1.png']):
                condition = False
            if not test_assert.assert_owner_equality(detected_file_paths=['/storage/emulated/0/test/1.png']):
                condition = False
            if not test_assert.assert_md5_equality(detected_paths=['/storage/emulated/0/test/1.png']):
                condition = False
        if count == 2:
            if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/test/1.jpg']):
                condition = False
            if not test_assert.assert_owner_equality(detected_file_paths=['/storage/emulated/0/test/1.jpg']):
                condition = False
            if not test_assert.assert_md5_equality(detected_paths=['/storage/emulated/0/test/1.jpg']):
                condition = False
        if count == 3:
            if not test_assert.assert_size_equality(detected_file_paths=['/storage/emulated/0/test/1.png',
                                                                         '/storage/emulated/0/test/1.jpg']):
                condition = False
            if not test_assert.assert_owner_equality(detected_file_paths=['/storage/emulated/0/test/1.png',
                                                                          '/storage/emulated/0/test/1.jpg']):
                condition = False
            if not test_assert.assert_md5_equality(detected_paths=['/storage/emulated/0/test/1.png',
                                                                   '/storage/emulated/0/test/1.jpg']):
                condition = False
    if condition is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'rename'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'rename'))
    print('end of rename assert')

    condition = False
    condition1 = True
    condition2 = 0

    file1 = "/storage/emulated/0/123_test_move2/test/1.png"
    file2 = "/storage/emulated/0/123_test_move2/test/1.jpg"
    file3 = "/storage/emulated/0/123_test_move1/test/1.png"
    file4 = "/storage/emulated/0/123_test_move1/test/1.jpg"
    test_base = os.path.dirname(test_base_dir)
    expect_paras_file = os.path.join(test_base, '0',test_package, 'expected_file_paras_path.out')
    expect_md5_file = os.path.join(test_base, '0',test_package, 'expected_md5_property_path.out')
    if test_assert.assert_file_existence(expected_file_paths=["/storage/emulated/0/123_test_move1/test"],isdelete=True):

        condition1 = False
        files_paras = None
        files_md5s = None
        if os.path.exists(expect_paras_file):
            with open(expect_paras_file, 'r') as reader:
                for line in reader.readlines():
                    if file1 in line:
                        files_paras =json.loads(line)
                # files_paras = json.load(reader)
        if os.path.exists(expect_md5_file):
            with open(expect_md5_file, 'r') as reader:
                for line in reader.readlines():
                    if file2 in line:
                        files_md5s = json.loads(line)
        expect_md5s = files_md5s
        if(len(files_md5s) > 0):
            expect_md5s[file3] = files_md5s[file1]
            expect_md5s[file4] = files_md5s[file2]

        expect_paras = files_paras
        if(len(files_paras) > 0):
            expect_paras[file3] = files_paras[file1]
            expect_paras[file4] = files_paras[file2]
        if not test_assert.assert_size_equality(detected_file_paths=[file3, file4], expected_size=expect_paras):
            condition = True
        if not test_assert.assert_create_time_equality(detected_file_paths=[file3, file4],expected_para=expect_paras):
            condition = True
        if not test_assert.assert_md5_equality(detected_paths=[file3, file4], expected_md5s=expect_md5s):
            condition = True
    elif test_assert.assert_file_existence(expected_file_paths=[os.path.dirname(file1)]):
        movetest_exist = True
        if test_assert.assert_file_existence(expected_file_paths=[file1]):
            condition2 = 1
        if test_assert.assert_file_existence(expected_file_paths=[file2]):
            condition2 = condition2 + 2
        if condition2 == 1:
            if not test_assert.assert_size_equality(detected_file_paths=[file1]):
                condition = True
            if not test_assert.assert_owner_equality(detected_file_paths=[file1]):
                condition = True
            if not test_assert.assert_md5_equality(detected_paths=[file1]):
                condition = True
        if condition2 == 2:
            if not test_assert.assert_size_equality(detected_file_paths=[file2]):
                condition = True
            if not test_assert.assert_owner_equality(detected_file_paths=[file2]):
                condition = True
            if not test_assert.assert_md5_equality(detected_paths=[file2]):
                condition = True
        if condition2 == 3:
            if not test_assert.assert_size_equality(detected_file_paths=[file1, file2]):
                condition = True
            if not test_assert.assert_owner_equality(detected_file_paths=[file1, file2]):
                condition = True
            if not test_assert.assert_md5_equality(detected_paths=[file1, file2]):
                condition = True

    if condition is True:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'move'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'move'))

    print('end of move assert')

    condition = True
    condition1 = 0
    delete_file1 = '/storage/emulated/0/123_test_delete/123_test_base/1.png'
    delete_file2 = '/storage/emulated/0/123_test_delete/123_test_base/1.jpg'
    if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/123_test_delete'], isdelete=True):
        if test_assert.assert_file_existence(expected_file_paths=['/storage/emulated/0/123_test_delete/123_test_base'],
                                             isdelete=True):
            deletetest_exist = False
        else:
            deletetest_exist = True
            condition = False
            if test_assert.assert_file_existence(expected_file_paths=[delete_file1]):
                condition1 = 1
            if test_assert.assert_file_existence(expected_file_paths=[delete_file2]):
                conditon1 = condition1 + 2
            if condition1 == 1:
                if not test_assert.assert_size_equality(detected_file_paths=[delete_file1]):
                    condition = False
                if not test_assert.assert_owner_equality(detected_file_paths=[delete_file1]):
                    condition = False
                if not test_assert.assert_md5_equality(detected_paths=[delete_file1]):
                    condition = False
            if condition1 == 2:
                if not test_assert.assert_size_equality(detected_file_paths=[delete_file2]):
                    condition = False
                if not test_assert.assert_owner_equality(detected_file_paths=[delete_file2]):
                    condition = False
                if not test_assert.assert_md5_equality(detected_paths=[delete_file2]):
                    condition = False
            if condition1 == 3:
                if not test_assert.assert_size_equality(detected_file_paths=[delete_file1, delete_file2]):
                    condition = False
                if not test_assert.assert_owner_equality(detected_file_paths=[delete_file1, delete_file2], ):
                    condition = False
                if not test_assert.assert_md5_equality(detected_paths=[delete_file1, delete_file2]):
                    condition = False

    if condition is False:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Wrong\n' % (output_tag, 'delete'))
    else:
        with open(output_file, 'a+') as fps:
            fps.write('Test %s Tag %s Result: Right\n' % (output_tag, 'delete'))

    print('end of delete assert')

    if ziptest_exist and testbase_exist:
        pre_conductor.clear(test_files=['ziptest','123_test_copy/123_test_base'])
    elif ziptest_exist:
        pre_conductor.clear(test_files=['ziptest'])
    elif testbase_exist:
        pre_conductor.clear(test_files=['123_test_copy/123_test_base'])

    if testzip_exist:
        pre_conductor.clear(test_files=['test.zip'],ifdir=False)
    if movetest_exist:
        pre_conductor.move(source_dir='123_test_move2/test/',dest_dir='123_test_move1/')
    if renametest_exist:
        pre_conductor.move(source_dir='test',dest_dir='123_test_rename')
    if not deletetest_exist:
        pre_conductor.makedir(test_files=['123_test_delete'])
        pre_conductor.copy(source_name='123_test_base',dest_dir='123_test_delete/')
