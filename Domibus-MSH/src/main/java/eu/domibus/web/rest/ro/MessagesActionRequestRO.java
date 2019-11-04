package eu.domibus.web.rest.ro;

import eu.domibus.api.validators.CustomWhiteListed;

import java.util.List;

/**
 * Created by musatmi on 15/05/2017.
 */

public class MessagesActionRequestRO {

    @CustomWhiteListed(permitted = ".@!/")
    private String source;
    private String type;
    private String content;
    @CustomWhiteListed(permitted = ".@!/")
    private String destination;
    @CustomWhiteListed(permitted = "<>.:-")
    private List<String> selectedMessages;
    private Action action;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<String> getSelectedMessages() {
        return selectedMessages;
    }

    public void setSelectedMessages(List<String> selectedMessages) {
        this.selectedMessages = selectedMessages;
    }

    public enum Action {
        MOVE("move"),
        REMOVE("remove");

        private String stringRepresentation;

        Action(String stringRepresentation) {
            this.stringRepresentation = stringRepresentation;
        }
    }
}
