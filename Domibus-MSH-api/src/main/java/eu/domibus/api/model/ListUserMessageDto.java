package eu.domibus.api.model;

import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
public class ListUserMessageDto {

    List<UserMessageDTO> userMessageDtos;

    public ListUserMessageDto(List<UserMessageDTO> resultList) {
        this.userMessageDtos = resultList;
    }

    public List<UserMessageDTO> getUserMessageDtos() {
        return userMessageDtos;
    }

    public void setUserMessageDtos(List<UserMessageDTO> userMessageDtos) {
        this.userMessageDtos = userMessageDtos;
    }
}
