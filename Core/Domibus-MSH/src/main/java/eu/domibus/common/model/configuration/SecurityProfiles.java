package eu.domibus.common.model.configuration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "securityProfile")
@XmlRootElement(name = "securityProfiles", namespace = "http://domibus.eu/configuration")
public class SecurityProfiles {

    @XmlElement(required = true)
    protected List<SecurityProfile> securityProfile;

    public List<SecurityProfile> getSecurityProfile() {
        if (this.securityProfile == null) {
            this.securityProfile = new ArrayList<>();
        }
        return this.securityProfile;
    }
}
