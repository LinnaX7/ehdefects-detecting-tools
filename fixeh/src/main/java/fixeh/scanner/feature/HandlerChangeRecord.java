package fixeh.scanner.feature;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;

/**
 * Created by Shunjie Ding on 16/01/2018.
 */
@OutAlias("Handler Changes")
final class HandlerChangeRecord {
    @CellConfig(index = 0, aliasName = "Revision ID", readRequired = true)
    private String revisionId;

    @CellConfig(index = 1, aliasName = "Change Type", readRequired = true)
    private String changeType;

    @CellConfig(index = 2, aliasName = "Is Catch", readRequired = true)
    private boolean isCatch;

    @CellConfig(index = 3, aliasName = "Left Exceptions", readRequired = true)
    private String leftExceptions;

    @CellConfig(index = 4, aliasName = "Right Exceptions", readRequired = true)
    private String rightExceptions;

    @CellConfig(index = 5, aliasName = "Left Actions", readRequired = true)
    private String leftActions;

    @CellConfig(index = 6, aliasName = "Right Actions", readRequired = true)
    private String rightActions;

    @CellConfig(index = 7, aliasName = "Left Codes", readRequired = true)
    private String leftCodes;

    @CellConfig(index = 8, aliasName = "Right Codes", readRequired = true)
    private String rightCodes;

    public HandlerChangeRecord() {}

    HandlerChangeRecord(String revisionId, String changeType, boolean isCatch,
        String leftExceptions, String rightExceptions, String leftActions, String rightActions,
        String leftCodes, String rightCodes) {
        this.revisionId = revisionId;
        this.changeType = changeType;
        this.isCatch = isCatch;
        this.leftExceptions = leftExceptions;
        this.rightExceptions = rightExceptions;
        this.leftActions = leftActions;
        this.rightActions = rightActions;
        this.leftCodes = leftCodes;
        this.rightCodes = rightCodes;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public String getChangeType() {
        return changeType;
    }

    public boolean isCatch() {
        return isCatch;
    }

    public String getLeftExceptions() {
        return leftExceptions;
    }

    public String getRightExceptions() {
        return rightExceptions;
    }

    public String getLeftActions() {
        return leftActions;
    }

    public String getRightActions() {
        return rightActions;
    }

    public String getLeftCodes() {
        return leftCodes;
    }

    public String getRightCodes() {
        return rightCodes;
    }
}
