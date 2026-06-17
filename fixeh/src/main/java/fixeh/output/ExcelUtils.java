package fixeh.output;

import com.bing.excel.core.BingExcel;
import com.bing.excel.core.BingExcelBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by Shunjie Ding on 10/01/2018.
 */
public final class ExcelUtils {
    public static <T> BingExcel writeObjectsTo(File file, Iterable<T> data)
        throws FileNotFoundException {
        BingExcel excel = BingExcelBuilder.builderInstance();
        excel.writeExcel(file, data);
        return excel;
    }

    public static <T> BingExcel writeObjectsTo(String filePath, Iterable<T> data) {
        BingExcel excel = BingExcelBuilder.builderInstance();
        excel.writeExcel(filePath, data);
        return excel;
    }

    public static <T> List<T> readObjectsFrom(String filePath, Class<T> clz, int startRowNum)
        throws Exception {
        return readObjectsFrom(new File(filePath), clz, startRowNum);
    }

    public static <T> List<T> readObjectsFrom(File file, Class<T> clz, int startRowNum)
        throws Exception {
        return BingExcelBuilder.builderInstance().readFile(file, clz, startRowNum).getObjectList();
    }
}
