package eu.domibus.ext.delegate.services.server;

import eu.domibus.api.server.ServerInfoService;
import eu.domibus.ext.services.ServerInfoExtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServerInfoServiceDelegate implements ServerInfoExtService {

    @Autowired
    private ServerInfoService serverInfoService;

    public String getNodeName() {
        return serverInfoService.getServerName();
    }
}
