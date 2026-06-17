package fixeh.instrument.woventools;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by Shunjie Ding on 22/01/2018.
 */
public class TestLogProxy {
    private static Log log;

    @Before
    public void initLog() throws NoSuchMethodException, ClassNotFoundException {
        if (log == null) {
            log = new LogProxy(LogMock.class.getName());
        }
    }

    @Test
    public void testLogProxy() {
        Throwable tr = new Exception("exception");
        log.d("a", "b", tr);
        log.e("a", "b", tr);
        log.i("a", "b", tr);
        log.v("a", "b", tr);
        log.w("a", "b", tr);
        log.wtf("a", "b", tr);
    }
}
