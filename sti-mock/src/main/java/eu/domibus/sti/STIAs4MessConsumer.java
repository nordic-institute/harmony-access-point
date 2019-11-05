package eu.domibus.sti;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

import javax.jms.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class STIAs4MessConsumer implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(STIAs4MessConsumer.class);

    public final static String JNDI_FACTORY = "weblogic.jndi.WLInitialContextFactory";

    public final static String JMS_FACTORY = "jms/ConnectionFactory";

    public final static String OUT_QUEUE = "jms/domibus.backend.jms.outQueue";

    public final static String IN_QUEUE = "jms/domibus.backend.jms.inQueue";

    private final static String HAPPY_FLOW_MESSAGE_TEMPLATE = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<response_to_message_id>\n" +
            " $messId\n" +
            "</response_to_message_id>\n" +
            "<dataset>\n" +
            "<record><id>1</id><first_name>Belita</first_name><last_name>MacMeanma</last_name><email>bmacmeanma0@alexa.com</email><gender>Female</gender><ip_address>211.210.105.141</ip_address></record><record><id>2</id><first_name>Delainey</first_name><last_name>Sarll</last_name><email>dsarll1@xinhuanet.com</email><gender>Male</gender><ip_address>172.215.113.41</ip_address></record><record><id>3</id><first_name>Rafaela</first_name><last_name>Jandel</last_name><email>rjandel2@usda.gov</email><gender>Female</gender><ip_address>176.76.130.69</ip_address></record><record><id>4</id><first_name>Fredrika</first_name><last_name>Dunbabin</last_name><email>fdunbabin3@google.com.br</email><gender>Female</gender><ip_address>28.5.174.234</ip_address></record><record><id>5</id><first_name>Othilie</first_name><last_name>Braniff</last_name><email>obraniff4@redcross.org</email><gender>Female</gender><ip_address>135.122.142.137</ip_address></record><record><id>6</id><first_name>Filmer</first_name><last_name>Wands</last_name><email>fwands5@newsvine.com</email><gender>Male</gender><ip_address>245.77.82.100</ip_address></record><record><id>7</id><first_name>Bernie</first_name><last_name>Le feaver</last_name><email>blefeaver6@usda.gov</email><gender>Male</gender><ip_address>142.226.208.76</ip_address></record><record><id>8</id><first_name>Dacy</first_name><last_name>Di Antonio</last_name><email>ddiantonio7@bbb.org</email><gender>Female</gender><ip_address>235.214.118.96</ip_address></record><record><id>9</id><first_name>Hobey</first_name><last_name>Di Pietro</last_name><email>hdipietro8@nps.gov</email><gender>Male</gender><ip_address>166.30.27.83</ip_address></record><record><id>10</id><first_name>Catha</first_name><last_name>Denkel</last_name><email>cdenkel9@princeton.edu</email><gender>Female</gender><ip_address>102.60.69.38</ip_address></record><record><id>11</id><first_name>Jeralee</first_name><last_name>Gorling</last_name><email>jgorlinga@google.ca</email><gender>Female</gender><ip_address>217.169.183.180</ip_address></record><record><id>12</id><first_name>Henrietta</first_name><last_name>Aloshechkin</last_name><email>haloshechkinb@umich.edu</email><gender>Female</gender><ip_address>128.2.221.166</ip_address></record><record><id>13</id><first_name>Georges</first_name><last_name>Veregan</last_name><email>gvereganc@seattletimes.com</email><gender>Male</gender><ip_address>117.64.187.183</ip_address></record><record><id>14</id><first_name>Dara</first_name><last_name>Shottin</last_name><email>dshottind@weather.com</email><gender>Female</gender><ip_address>167.185.3.185</ip_address></record><record><id>15</id><first_name>Jerry</first_name><last_name>Attrill</last_name><email>jattrille@nps.gov</email><gender>Male</gender><ip_address>144.46.79.18</ip_address></record><record><id>16</id><first_name>Worth</first_name><last_name>Louche</last_name><email>wlouchef@vkontakte.ru</email><gender>Male</gender><ip_address>17.117.2.116</ip_address></record><record><id>17</id><first_name>Gabie</first_name><last_name>Fontel</last_name><email>gfontelg@nbcnews.com</email><gender>Female</gender><ip_address>94.216.217.36</ip_address></record><record><id>18</id><first_name>Stanton</first_name><last_name>Millott</last_name><email>smillotth@google.nl</email><gender>Male</gender><ip_address>6.194.119.179</ip_address></record><record><id>19</id><first_name>Hedi</first_name><last_name>Pele</last_name><email>hpelei@jiathis.com</email><gender>Female</gender><ip_address>198.140.7.33</ip_address></record><record><id>20</id><first_name>Nils</first_name><last_name>Klesl</last_name><email>nkleslj@woothemes.com</email><gender>Male</gender><ip_address>106.74.129.90</ip_address></record><record><id>21</id><first_name>Bucky</first_name><last_name>Hobbema</last_name><email>bhobbemak@livejournal.com</email><gender>Male</gender><ip_address>173.139.210.39</ip_address></record><record><id>22</id><first_name>Araldo</first_name><last_name>Claye</last_name><email>aclayel@elpais.com</email><gender>Male</gender><ip_address>116.15.8.224</ip_address></record><record><id>23</id><first_name>Jules</first_name><last_name>Heninghem</last_name><email>jheninghemm@biblegateway.com</email><gender>Male</gender><ip_address>196.24.132.34</ip_address></record><record><id>24</id><first_name>Trista</first_name><last_name>Kiloh</last_name><email>tkilohn@npr.org</email><gender>Female</gender><ip_address>108.148.209.172</ip_address></record><record><id>25</id><first_name>Clevie</first_name><last_name>Drinkall</last_name><email>cdrinkallo@blogtalkradio.com</email><gender>Male</gender><ip_address>63.122.167.93</ip_address></record><record><id>26</id><first_name>Monte</first_name><last_name>Deary</last_name><email>mdearyp@fc2.com</email><gender>Male</gender><ip_address>170.13.123.223</ip_address></record><record><id>27</id><first_name>Teresina</first_name><last_name>Keuning</last_name><email>tkeuningq@ask.com</email><gender>Female</gender><ip_address>29.193.166.64</ip_address></record><record><id>28</id><first_name>Noam</first_name><last_name>Muckley</last_name><email>nmuckleyr@cbc.ca</email><gender>Male</gender><ip_address>246.237.66.187</ip_address></record><record><id>29</id><first_name>Cordelia</first_name><last_name>Bussens</last_name><email>cbussenss@artisteer.com</email><gender>Female</gender><ip_address>102.234.75.160</ip_address></record><record><id>30</id><first_name>Henrik</first_name><last_name>Paffley</last_name><email>hpaffleyt@upenn.edu</email><gender>Male</gender><ip_address>246.79.215.136</ip_address></record><record><id>31</id><first_name>Branden</first_name><last_name>Stannett</last_name><email>bstannettu@yahoo.com</email><gender>Male</gender><ip_address>161.122.87.149</ip_address></record><record><id>32</id><first_name>Madelle</first_name><last_name>Drayton</last_name><email>mdraytonv@tmall.com</email><gender>Female</gender><ip_address>69.170.17.15</ip_address></record><record><id>33</id><first_name>Flemming</first_name><last_name>Hastie</last_name><email>fhastiew@statcounter.com</email><gender>Male</gender><ip_address>194.30.236.45</ip_address></record><record><id>34</id><first_name>Torrance</first_name><last_name>Mielnik</last_name><email>tmielnikx@home.pl</email><gender>Male</gender><ip_address>130.163.101.62</ip_address></record><record><id>35</id><first_name>Cinnamon</first_name><last_name>Trevor</last_name><email>ctrevory@boston.com</email><gender>Female</gender><ip_address>132.206.141.48</ip_address></record><record><id>36</id><first_name>Deanne</first_name><last_name>Gullen</last_name><email>dgullenz@rambler.ru</email><gender>Female</gender><ip_address>134.61.119.145</ip_address></record><record><id>37</id><first_name>Wyatan</first_name><last_name>Rudgard</last_name><email>wrudgard10@addthis.com</email><gender>Male</gender><ip_address>119.131.19.119</ip_address></record><record><id>38</id><first_name>Thomasa</first_name><last_name>Keme</last_name><email>tkeme11@storify.com</email><gender>Female</gender><ip_address>29.51.65.34</ip_address></record><record><id>39</id><first_name>Mead</first_name><last_name>Cobain</last_name><email>mcobain12@youtu.be</email><gender>Female</gender><ip_address>177.138.6.69</ip_address></record><record><id>40</id><first_name>Baillie</first_name><last_name>Sommerlie</last_name><email>bsommerlie13@home.pl</email><gender>Male</gender><ip_address>46.91.193.197</ip_address></record><record><id>41</id><first_name>Cindi</first_name><last_name>Waldocke</last_name><email>cwaldocke14@nature.com</email><gender>Female</gender><ip_address>211.123.179.43</ip_address></record><record><id>42</id><first_name>Sophie</first_name><last_name>Weddell</last_name><email>sweddell15@tiny.cc</email><gender>Female</gender><ip_address>92.79.6.93</ip_address></record><record><id>43</id><first_name>Faydra</first_name><last_name>Spata</last_name><email>fspata16@bloomberg.com</email><gender>Female</gender><ip_address>3.85.1.239</ip_address></record><record><id>44</id><first_name>Monte</first_name><last_name>Philipeau</last_name><email>mphilipeau17@examiner.com</email><gender>Male</gender><ip_address>49.233.30.244</ip_address></record><record><id>45</id><first_name>Garrott</first_name><last_name>Creer</last_name><email>gcreer18@webnode.com</email><gender>Male</gender><ip_address>253.166.143.212</ip_address></record><record><id>46</id><first_name>Harp</first_name><last_name>Wherrett</last_name><email>hwherrett19@squarespace.com</email><gender>Male</gender><ip_address>197.232.85.3</ip_address></record><record><id>47</id><first_name>Miller</first_name><last_name>Wilsee</last_name><email>mwilsee1a@wix.com</email><gender>Male</gender><ip_address>242.106.77.87</ip_address></record><record><id>48</id><first_name>Prentiss</first_name><last_name>Tucknott</last_name><email>ptucknott1b@wix.com</email><gender>Male</gender><ip_address>107.41.137.99</ip_address></record><record><id>49</id><first_name>Muffin</first_name><last_name>Mulkerrins</last_name><email>mmulkerrins1c@cisco.com</email><gender>Female</gender><ip_address>219.94.140.169</ip_address></record><record><id>50</id><first_name>Tamera</first_name><last_name>Skade</last_name><email>tskade1d@flavors.me</email><gender>Female</gender><ip_address>140.28.170.139</ip_address></record>\n" +
            "</dataset>";

    @Value("${sti.provider.url}")
    private String providerUrl;

    private QueueConnectionFactory qconFactory;

    private QueueConnection qcon;

    private QueueSession qsession;

    private QueueReceiver qreceiver;

    private Queue queue;

    private QueueSender qsr;

    private boolean connected = false;


    @Scheduled(fixedRateString = "${broker.connection.retry.fixedRate.in.milliseconds}", initialDelayString = "5000")
    public void init() {
        if (connected) {
            LOG.debug("Jms receiver connected");
            return;
        }
        InitialContext ic = null;
        try {
            ic = getInitialContext();
            initJmsReceiver(ic, OUT_QUEUE);
            LOG.info("JMS Ready To Receive Messages on queue:[{}]", OUT_QUEUE);
            connected = true;
        } catch (NamingException | JMSException e) {
            LOG.error("Error connecting to jms queue on:[{}]", providerUrl, e);
            connected = false;
        }
        /*STIAs4MessConsumer qr = new STIAs4MessConsumer();
        qr.init(ic, QUEUE);
        System.out.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message).");
        synchronized (qr) {
            while (!qr.quit) {
                try {
                    qr.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        qr.close();*/

    }

    public void onMessage(Message msg) {
        try {
            if (msg instanceof MapMessage) {
                MapMessage mess = ((MapMessage) msg);
                LOG.info("New incoming message");
                final Enumeration propertyNames = mess.getPropertyNames();
                while (propertyNames.hasMoreElements()) {
                    final Object o = propertyNames.nextElement();
                    LOG.info("Message property name:[{}] value:[{}],[{}]", o, mess.getString(o.toString()), mess.getStringProperty(o.toString()));
                }

                Enumeration enumeration = mess.getMapNames();
                while (enumeration.hasMoreElements()) {
                    LOG.info("Message element:[{}]", enumeration.nextElement());
                }
                LOG.info("Sending response");
                qsr.send(prepareResponse(mess));
                LOG.info("Response sent");
            }
        } catch (JMSException jmse) {
            LOG.error("An exception occurred while consuming message:", jmse);
        }
    }


    public void initJmsReceiver(Context ctx, String queueName)
            throws NamingException, JMSException {
        qconFactory = (QueueConnectionFactory) ctx.lookup(JMS_FACTORY);
        qcon = qconFactory.createQueueConnection();
        qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = (Queue) ctx.lookup(queueName);
        qreceiver = qsession.createReceiver(queue);
        qreceiver.setMessageListener((MessageListener) this);

        Queue queue = (Queue) ctx.lookup(IN_QUEUE);
        //QueueConnection qc = qconFactory.createQueueConnection();
        //QueueSession qs = qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        qsr = qsession.createSender(queue);
        qcon.start();
    }

    private MapMessage prepareResponse(MapMessage received) throws JMSException {
        MapMessage messageMap = qsession.createMapMessage();

        // Declare message as submit
        messageMap.setStringProperty("username", "plugin_admin");
        messageMap.setStringProperty("password", "123456");

        messageMap.setStringProperty("messageType", "submitMessage");
        messageMap.setStringProperty("messageId", UUID.randomUUID().toString());
        // Uncomment to test refToMessageId that is too long, i.e. > 255
        final String messageId = received.getStringProperty("messageId");

        messageMap.setStringProperty("refToMessageId", messageId);

        // Set up the Communication properties for the message
        messageMap.setStringProperty("service", "eu_ics2_c2t");
        //messageMap.setStringProperty("serviceType", "noSecurity");
        //messageMap.setStringProperty("serviceType", "signOnly");
        messageMap.setStringProperty("agreementRef","EU-ICS2-TI-V1.0");


        messageMap.setStringProperty("action", "IE3R01");
        //messageMap.setStringProperty("conversationId", "123");
        //messageMap.setStringProperty("fromPartyId", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-blue");
        //messageMap.setStringProperty("fromPartyType", ""); // Mandatory but empty here because it is in the value of the party ID
        messageMap.setStringProperty("fromPartyId", received.getStringProperty("toPartyId"));
        messageMap.setStringProperty("fromPartyType", received.getStringProperty("toPartyType")); // Mandatory

        messageMap.setStringProperty("fromRole", received.getStringProperty("toRole"));

        //messageMap.setStringProperty("toPartyId", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-red");
        //messageMap.setStringProperty("toPartyType", ""); // Mandatory but empty here because it is in the value of the party ID
        messageMap.setStringProperty("toPartyId", received.getStringProperty("fromPartyId"));
        messageMap.setStringProperty("toPartyType", received.getStringProperty("fromPartyType")); // Mandatory

        messageMap.setStringProperty("toRole", received.getStringProperty("fromRole"));

        messageMap.setStringProperty("originalSender", received.getStringProperty("finalRecipient"));
        messageMap.setStringProperty("finalRecipient", received.getStringProperty("originalSender"));
        messageMap.setStringProperty("protocol", "AS4");

       // messageMap.setJMSCorrelationID("12345");
        //Set up the payload properties
        messageMap.setStringProperty("totalNumberOfPayloads", "1");
        messageMap.setStringProperty("payload_1_description", "message");
        messageMap.setStringProperty("payload_1_mimeContentId", "cid:message");
        messageMap.setStringProperty("payload_1_mimeType", "text/xml");

        String response = HAPPY_FLOW_MESSAGE_TEMPLATE.replace("$messId", messageId);

        //messageMap.setStringProperty("p1InBody", "true"); // If true payload_1 will be sent in the body of the AS4 message. Only XML payloads may be sent in the AS4 message body. Optional

        //send the payload in the JMS message as byte array
        byte[] payload = response.getBytes();
        messageMap.setBytes("payload_1", payload);
        return messageMap;

    }

    public void close() throws JMSException {
        qreceiver.close();
        qsession.close();
        qcon.close();
    }

   /* public static void main(String[] args) throws Exception {
        InitialContext ic = getInitialContext();
        STIAs4MessConsumer qr = new STIAs4MessConsumer();
        qr.init(ic, QUEUE);
        System.out.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message).");
        synchronized (qr) {
            while (!qr.quit) {
                try {
                    qr.wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        qr.close();
    }*/


    private InitialContext getInitialContext()
            throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
        env.put(Context.PROVIDER_URL, providerUrl);
        return new InitialContext(env);
    }
}
