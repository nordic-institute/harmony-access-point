package eu.domibus.web.rest.ro;

/**
 * @author Soumya Chandran
 * @since 5.0
 */
public class MessageDownloadRO {

    boolean canDownload;
    String response;

    public MessageDownloadRO() {

    }

    public boolean getCanDownload() {
        return canDownload;
    }

    public void setCanDownload(boolean canDownload) {
        this.canDownload = canDownload;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
