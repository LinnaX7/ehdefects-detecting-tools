package fixeh.scanner.feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shunjie Ding on 07/01/2018.
 */
public final class MethodChange implements Serializable {
    private final String signature;
    private final boolean isPublic;
    private final boolean isNew;
    private final List<String> exceptionsDeclared;
    private final List<HandlerChange> handlerChanges;
    // Modifications
    private int numberTryBlocks;
    private int numberCatchBlocks;
    private int numberFinallyBlocks;
    private int numberTryBlocksChanged;
    private int numberCatchBlocksChanged;
    private int numberFinallyBlocksChanged;

    protected MethodChange(String signature, boolean isPublic, boolean isNew,
        List<String> exceptionsDeclared, List<HandlerChange> handlerChanges) {
        this.signature = signature;
        this.isPublic = isPublic;
        this.isNew = isNew;
        this.exceptionsDeclared = exceptionsDeclared;
        this.handlerChanges = handlerChanges == null ? new ArrayList<>(0) : handlerChanges;
    }

    public static MethodChange newMethodChange(String signature, boolean isPublic, boolean isNew,
        List<String> exceptionsDeclared, List<HandlerChange> handlerChanges) {
        return new MethodChange(signature, isPublic, isNew, exceptionsDeclared, handlerChanges);
    }

    public List<HandlerChange> getHandlerChanges() {
        return handlerChanges;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isNew() {
        return isNew;
    }

    public List<String> getExceptionsDeclared() {
        return exceptionsDeclared;
    }

    public int getNumberTryBlocks() {
        return numberTryBlocks;
    }

    public void setNumberTryBlocks(int numberTryBlocks) {
        this.numberTryBlocks = numberTryBlocks;
    }

    public int getNumberCatchBlocks() {
        return numberCatchBlocks;
    }

    public void setNumberCatchBlocks(int numberCatchBlocks) {
        this.numberCatchBlocks = numberCatchBlocks;
    }

    public int getNumberFinallyBlocks() {
        return numberFinallyBlocks;
    }

    public void setNumberFinallyBlocks(int numberFinallyBlocks) {
        this.numberFinallyBlocks = numberFinallyBlocks;
    }

    public int getNumberTryBlocksChanged() {
        return numberTryBlocksChanged;
    }

    public void setNumberTryBlocksChanged(int numberTryBlocksChanged) {
        this.numberTryBlocksChanged = numberTryBlocksChanged;
    }

    public int getNumberCatchBlocksChanged() {
        return numberCatchBlocksChanged;
    }

    public void setNumberCatchBlocksChanged(int numberCatchBlocksChanged) {
        this.numberCatchBlocksChanged = numberCatchBlocksChanged;
    }

    public int getNumberFinallyBlocksChanged() {
        return numberFinallyBlocksChanged;
    }

    public void setNumberFinallyBlocksChanged(int numberFinallyBlocksChanged) {
        this.numberFinallyBlocksChanged = numberFinallyBlocksChanged;
    }

    public String getSignature() {
        return signature;
    }
}
