## What are provided?

5 files are originally provided: 

+ fixeh-0.0.2-all.jar 
+ fixeh-woventools-0.0.2.jar 
+ k9mail-debug.apk 
+ k9mail-debug-androidTest.apk
+ fixeh-policy.xml

## How to instrument?

Execute the following command: 

```bash
java -jar fixeh-0.0.2-all.jar instrument --woven-jars fixeh-woventools-0.0.2.jar -t k9mail-debug.apk
```

An apk file will be generated under ./instrumented. Note it haven't been aligned or signed.

## How to run all tests?

Execute the following command:

```bash
java -jar fixeh-0.0.2-all.jar test -a instrument/k9mail-debug.apk -b k9mail-debug-androidTest.apk --force-sign --policy-xml fixeh-policy.xml
```

To specify test class, add `--class xxx.xxx.xxxTest` before executing. 

## How to read logs genereted by fixeh on android device?

Execute the following command:

```bash
adb logcat fixeh:* *:S -d | less
```

To clear log buffer:

```bash
adb logcat -c
```

Logs of example test run are saved in file `fixeh.k-9.test.log`.

## What to do if apks can not be installed?

Read the fucking log! Generally problems are caused by inconsistent apk signatures so you have to uninstall apps (along with test packages) manually.

## What to do if fixeh crashes?

Check if you have the most recent android development tools and if the android vm you used have the newest android version (old androids will complain about the apk install options and then fixeh crashes, so just upgrade your android!).

## What to do if app/test app crashes?

Read logs in logcat and find the problem!

But there are apps crashes (after fixeh's instrumentation) without any message logged, mostly created for early android (e.g. android 2.x). Give up on those apps then!
