package eu.domibus.core.alerts;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.core.alerts.service.AlertConfigurationService;
import eu.domibus.logging.DomibusLoggerFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class MailSender {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(MailSender.class);

    static final String DOMIBUS_ALERT_MAIL = "domibus.alert.mail";

    private static final String MAIL = ".mail";


    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    private AlertConfigurationService configurationService;

    private boolean mailSenderInitiated;

    protected void initMailSender() {
        final Boolean alertModuleEnabled = configurationService.isAlertModuleEnabled();
        LOG.debug("Alert module enabled:[{}]", alertModuleEnabled);
        final boolean mailActive = configurationService.isSendEmailActive();
        if (BooleanUtils.isTrue(alertModuleEnabled) && mailActive) {
            //static properties.
            final Integer timeout = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_MAIL_SMTP_TIMEOUT);
            final String url = getMandatoryUrl();
            final Integer port = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ALERT_SENDER_SMTP_PORT);
            final String user = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_USER);
            final String password = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_PASSWORD);

            LOG.debug("Configuring mail server.");
            LOG.debug("Smtp url:[{}]", url);
            LOG.debug("Smtp port:[{}]", port);
            LOG.debug("Smtp timeout:[{}]", timeout);
            LOG.debug("Smtp user:[{}]", user);

            javaMailSender.setHost(url);
            javaMailSender.setPort(port);
            javaMailSender.setUsername(user);
            javaMailSender.setPassword(password);

            //Non static properties.
            final Properties javaMailProperties = javaMailSender.getJavaMailProperties();
            final Set<String> mailPropertyNames = domibusPropertyProvider.filterPropertiesName(s -> s.startsWith(DOMIBUS_ALERT_MAIL));
            mailPropertyNames.
                    forEach(domibusPropertyName -> {
                        final String mailPropertyName = domibusPropertyName.substring(domibusPropertyName.indexOf(MAIL) + 1);
                        final String propertyValue = domibusPropertyProvider.getProperty(domibusPropertyName);
                        LOG.debug("mail property:[{}] value:[{}]", mailPropertyName, propertyValue);
                        javaMailProperties.put(mailPropertyName, propertyValue);
                    });
        }
    }

    /**
     *
     * @return domibus.alert.sender.smtp.url value
     * @throws IllegalStateException if url is blank
     */
    protected String getMandatoryUrl() {
        String url = domibusPropertyProvider.getProperty(DOMIBUS_ALERT_SENDER_SMTP_URL);
        if (StringUtils.isBlank(url)) {
            throw new IllegalStateException("Could not initialize mail sender because the properties " +
                    DOMIBUS_ALERT_SENDER_SMTP_URL + " are incorrect: [" + url + "]");
        }
        return url;
    }

    public void reset() {
        mailSenderInitiated = false;
    }

    public <T extends MailModel<Map<String, String>>> void sendMail(final T model, final String from, final String to) {
        if (StringUtils.isBlank(to)) {
            throw new IllegalArgumentException("The 'to' property cannot be null");
        }
        if (StringUtils.isBlank(from)) {
            throw new IllegalArgumentException("The 'from' property cannot be null");
        }

        if (!this.mailSenderInitiated) {
            this.mailSenderInitiated = true;
            try {
                initMailSender();
            } catch (Exception ex) {
                this.mailSenderInitiated = false;
                throw new DomibusPropertyException("Could not initiate mail sender", ex);
            }
        }
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = getMimeMessageHelper(message);

            Template template = freemarkerConfig.getTemplate(model.getTemplatePath());
            final Object model1 = model.getModel();
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model1);

            if (to.contains(";")) {
                helper.setBcc(to.split(";"));
            } else {
                helper.setTo(to);
            }
            helper.setText(html, true);
            helper.setSubject(model.getSubject());
            helper.setFrom(from);
            javaMailSender.send(message);
        } catch (IOException | MessagingException | TemplateException | MailException e) {
            LOG.error("Exception while sending mail from[{}] to[{}]", from, to, e);
            throw new AlertDispatchException(e);
        }
    }

    MimeMessageHelper getMimeMessageHelper(MimeMessage message) throws MessagingException {
        return new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
    }


}
