package fixeh.scanner.feature;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;

/**
 * Created by Shunjie Ding on 16/01/2018.
 */
@OutAlias("Suspicious Revisions")
final class SuspiciousRevisionRecord {
    @CellConfig(index = 0, aliasName = "Revision ID", readRequired = true)
    private String revisionId;

    @CellConfig(index = 1, aliasName = "Message")
    private String message;

    @CellConfig(index = 2, aliasName = "Handler Changes Size")
    private int handlerChangesSize;

    public SuspiciousRevisionRecord() {}

    SuspiciousRevisionRecord(String revisionId, String message, int handlerChangesSize) {
        this.revisionId = revisionId;
        this.message = message;
        this.handlerChangesSize = handlerChangesSize;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public String getMessage() {
        return message;
    }

    public int getHandlerChangesSize() {
        return handlerChangesSize;
    }
}
