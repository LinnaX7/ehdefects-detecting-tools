package fixeh;

import org.apache.commons.lang3.SystemUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import fixeh.util.AndroidUtils;
import fixeh.util.ResourceUtils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Created by Shunjie Ding on 19/12/2017.
 */
public final class Constants {
    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    public final static String[] MESSAGE_KEYWORDS = {"try", "catch", "finally", "throw",
        "exception", "issue", "fix", "bug", "http", "failure", "crash"};

    public final static String[] RESOURCE_RELATED_PACKAGES = {"java.io","java.net", "java.nio",
        "javax.net", "java.crypto","java.util.zip", "android.net", "android.database", "android.location", "android.bluetooth",
        "android.hardware", "android.nfc", "java.security"};

    // private static int EXCEPTIONS_NO = 143;
    public final static String[] RUNTIME_EXCEPTION_RELATED_METHODS = {
        "android.hardware.Camera: void autoFocus(android.hardware.Camera$AutoFocusCallback)"};
    public final static Map<String, List<String>> TOTAL_RUNTIME_EXCEPTIONS_RELATED_METHODS =
        new HashMap<>();

    private final static void loadRuntimeExceptions() {
        Workbook wb = null;
        InputStream is = null;
        try {
            is = new FileInputStream(ResourceUtils.getRuntimeExceptionXls());
            wb = new HSSFWorkbook(is);
            Sheet sheet = wb.getSheetAt(0);
            Row exceptionRow = sheet.getRow(0);
            for (int index = 0; index < exceptionRow.getPhysicalNumberOfCells(); index++) {
                TOTAL_RUNTIME_EXCEPTIONS_RELATED_METHODS.put(
                    exceptionRow.getCell(index).getStringCellValue(), new ArrayList<>());
            }
            Iterator rowIterator = sheet.rowIterator();
            int count = 0;
            while (rowIterator.hasNext()) {
                if (count++ == 0)
                    continue;
                Row row = (Row) rowIterator.next();
                Iterator cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = (Cell) cellIterator.next();
                    if (!cell.getStringCellValue().contains("%%"))
                        continue;
                    TOTAL_RUNTIME_EXCEPTIONS_RELATED_METHODS
                        .get(exceptionRow.getCell(cell.getAddress().getColumn())
                                 .getStringCellValue())
                        .add(cell.getStringCellValue().split("%% ")[1]);
                }
            }
            wb.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        loadRuntimeExceptions();
    }

    public static final String TMPDIR = System.getProperty("java.io.tmpdir") + "/fixeh";

    static {
        try {
            Files.createDirectory(Paths.get(TMPDIR));
        } catch (IOException e) {
            // ignore
        }
    }

    public static final String JAVA_HOME = System.getProperty("java.home");

    public static final String USER_HOME = System.getProperty("user.home");

    public static final String ANDROID_HOME = getAndroidHome();

    public static final int LATEST_ANDROID_SDK_VERSION = AndroidUtils.getLatestSdkVersion();

    public static final String LATEST_ANDROID_BUILD_TOOLS_VERSION =
        AndroidUtils.getLatestBuildToolsVersion();
    private static boolean debugMode = false;
    private static boolean compilerVerbose = false;

    private static String getAndroidHome() {
        if (SystemUtils.IS_OS_WINDOWS) {
            // FIXME Support get android home on windows
            throw new RuntimeException("Windows not supported yet!");
        } else if (SystemUtils.IS_OS_MAC) {
            return Paths.get(USER_HOME, "Library/Android/Sdk").toString();
        } else if (SystemUtils.IS_OS_LINUX) {
            return Paths.get(USER_HOME, "Android/Sdk").toString();
        }
        throw new RuntimeException("Not supported OS " + SystemUtils.OS_NAME);
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    static void setDebugMode(boolean d) {
        debugMode = d;
    }

    public static boolean isCompilerVerbose() {
        return compilerVerbose;
    }

    static void setCompilerVerbose(boolean v) {
        compilerVerbose = v;
    }
}
