package eu.domibus.core.earchive;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public class BatchEArchiveDTO {
    String version;
    String batch_id;
    String request_type;
    String status;
    String error_code;
    String error_description;
    String timestamp;
    String message_start_date;
    String message_end_date;
    String manifest_checksum;
    List<String> messages;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBatch_id() {
        return batch_id;
    }

    public void setBatch_id(String batch_id) {
        this.batch_id = batch_id;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getError_description() {
        return error_description;
    }

    public void setError_description(String error_description) {
        this.error_description = error_description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage_start_date() {
        return message_start_date;
    }

    public void setMessage_start_date(String message_start_date) {
        this.message_start_date = message_start_date;
    }

    public String getMessage_end_date() {
        return message_end_date;
    }

    public void setMessage_end_date(String message_end_date) {
        this.message_end_date = message_end_date;
    }

    public String getManifest_checksum() {
        return manifest_checksum;
    }

    public void setManifest_checksum(String manifest_checksum) {
        this.manifest_checksum = manifest_checksum;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
