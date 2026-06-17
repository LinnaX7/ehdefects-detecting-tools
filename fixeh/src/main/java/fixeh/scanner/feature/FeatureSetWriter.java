package fixeh.scanner.feature;

import com.bing.excel.annotation.CellConfig;
import com.bing.excel.annotation.OutAlias;
import com.bing.excel.core.BingExcel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import fixeh.output.ExcelUtils;
import fixeh.output.SerializeUtils;
import fixeh.project.vcs.Revision;

/**
 * Created by Shunjie Ding on 16/01/2018.
 */
public class FeatureSetWriter {
    private static int countHandlerChanges(ClassChange classChange) {
        return classChange.getMethodChanges()
            .stream()
            .mapToInt(cc -> cc.getHandlerChanges().size())
            .sum();
    }

    private static int countHandlerChanges(RevisionChange revisionChange) {
        return revisionChange.getClassChanges()
            .stream()
            .mapToInt(FeatureSetWriter::countHandlerChanges)
            .sum();
    }

    private static String getExceptionsOfHandler(HandlerChange.Handler handler) {
        if (handler instanceof HandlerChange.CatchHandler) {
            return ((HandlerChange.CatchHandler) handler)
                .getExceptions()
                .stream()
                .collect(Collectors.joining(", "));
        }
        return "";
    }

    private static String getActionsOfHandler(HandlerChange.Handler handler) {
        if (handler != null) {
            return handler.getActions()
                .stream()
                .map(HandlerChange.HandlerAction::name)
                .collect(Collectors.joining(", "));
        }
        return "";
    }

    private static String getCodesOfHandler(HandlerChange.Handler handler) {
        if (handler != null) {
            return handler.getCodes();
        }
        return "";
    }

    private static List<HandlerChangeRecord> getHandlerChangeRecords(FeatureSet featureSet) {
        List<HandlerChangeRecord> records = new ArrayList<>();
        for (RevisionChange revisionChange : featureSet.getRevisionChanges()) {
            for (ClassChange classChange : revisionChange.getClassChanges()) {
                for (MethodChange methodChange : classChange.getMethodChanges()) {
                    records.addAll(
                        methodChange.getHandlerChanges()
                            .stream()
                            .map(handlerChange
                                -> new HandlerChangeRecord(revisionChange.getRevisionId(),
                                    handlerChange.getType().name(),
                                    handlerChange.getLeft() instanceof HandlerChange.CatchHandler
                                        || handlerChange.getRight()
                                                instanceof HandlerChange.CatchHandler,
                                    getExceptionsOfHandler(handlerChange.getLeft()),
                                    getExceptionsOfHandler(handlerChange.getRight()),
                                    getActionsOfHandler(handlerChange.getLeft()),
                                    getActionsOfHandler(handlerChange.getRight()),
                                    getCodesOfHandler(handlerChange.getLeft()),
                                    getCodesOfHandler(handlerChange.getRight())))
                            .collect(Collectors.toList()));
                }
            }
        }
        return records;
    }

    public void writeToExcel(File f, FeatureSet featureSet) throws FileNotFoundException {
        BingExcel excel =
            ExcelUtils.writeObjectsTo(f, Collections.singleton(featureSet.getOverview()));
        excel.writeExcel(f,
            featureSet.getRevisionChanges()
                .stream()
                .map(revisionChange
                    -> new SuspiciousRevisionRecord(revisionChange.getRevisionId(),
                        revisionChange.getRevisionMessage(), countHandlerChanges(revisionChange)))
                .collect(Collectors.toList()));
        // FIXME fix bugs in bingexcel
        // excel.writeExcel(f, getHandlerChangeRecords(featureSet));
    }

    public void serializeToFile(File f, FeatureSet featureSet) throws IOException {
        SerializeUtils.writeObject(f, featureSet);
    }

    public void writeSuspiciousRevisionsToExcel(File f, List<Revision> revisions)
        throws FileNotFoundException {
        ExcelUtils.writeObjectsTo(f,
            revisions.stream()
                .map(revision
                    -> new SuspiciousRevisionRecordWithoutHandlerCounts(
                        revision.getId(), revision.getMessage(), revision.getCommitTime()))
                .collect(Collectors.toList()));
    }

    @OutAlias("Suspicious Revisions")
    static class SuspiciousRevisionRecordWithoutHandlerCounts {
        @CellConfig(index = 0, aliasName = "Revision ID", readRequired = true)
        private String revisionId;

        @CellConfig(index = 1, aliasName = "Message")
        private String message;

        @CellConfig(index = 2, aliasName = "Commit Time")
        private Date commitTime;

        public SuspiciousRevisionRecordWithoutHandlerCounts() {}

        SuspiciousRevisionRecordWithoutHandlerCounts(
            String revisionId, String message, Date commitTime) {
            this.revisionId = revisionId;
            this.message = message;
            this.commitTime = commitTime;
        }
    }
}
