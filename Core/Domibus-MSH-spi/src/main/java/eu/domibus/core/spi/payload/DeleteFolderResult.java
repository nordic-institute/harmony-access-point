package eu.domibus.core.spi.payload;

import java.util.ArrayList;
import java.util.List;

public class DeleteFolderResult {

    public enum Result {
        OK,
        PARTIAL,
        ERROR
    }

    public DeleteFolderResult() {
        this.failed = new ArrayList<>();
    }

    int total = 0;

    Result result;

    List<String> failed;

    public void addFailed(List<String> errors) {
        this.failed.addAll(errors);
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    public List<String> getFailed() {
        return failed;
    }
}
