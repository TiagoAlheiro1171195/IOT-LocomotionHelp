package Agents;

import Mqtt.*;
import Util.ConsoleColors;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class CrosswalkPassageAgent extends AgentMQTT {

    public enum ModelClass {
        gray,
        green,
        red
    }

    final int MIN_FRAMES = 3;
    final int TIMEOUT = 120; // seconds
    private String subscribeTopic;
    private String communicationAgentName;
    private String pedestrianAgentName;
    private DriverHiveMQTT driver;
    private Map<String, Integer> modelClassCounter = new HashMap<String, Integer>() {{
        put(ModelClass.gray.toString(), 0);
        put(ModelClass.green.toString(), 0);
        put(ModelClass.red.toString(), 0);
    }};

    private Map<String, LocalTime> modelClassTimeoutTime = new HashMap<String, LocalTime>() {{
        put(ModelClass.gray.toString(), LocalTime.MIN);
        put(ModelClass.green.toString(), LocalTime.MIN);
        put(ModelClass.red.toString(), LocalTime.MIN);
    }};


    @Override
    public void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.subscribeTopic = (String) args[0];
            this.communicationAgentName = (String) args[1];
            this.pedestrianAgentName = (String) args[2];
        }

        try {
            driver = new DriverHiveMQTT(this);
            driver.SubscribeTopic(subscribeTopic);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        registerYellowPage("crosswalk-passage");

        System.out.println("Crosswalk Passage Agent Started ::: " + getLocalName());
    }

    @Override
    public void ProcessMessageArrived(String topic, String payload) {
        if (!topic.equalsIgnoreCase(subscribeTopic)) {
            return;
        }

        modelClassCounter.put(payload, modelClassCounter.get(payload) + 1);
        LocalTime current = LocalTime.now();

        // continue if min frame counter is achieved and is not in timeout
        if (modelClassCounter.get(payload) < MIN_FRAMES | modelClassTimeoutTime.get(payload).isAfter(current)) {
            return;
        }

        // build message to inform warning agent
        ACLMessage infMsg = new ACLMessage(ACLMessage.INFORM);

        infMsg.addReceiver(new AID(communicationAgentName, AID.ISLOCALNAME));
        infMsg.setConversationId(topic);

        String content = "";
        if (payload.equalsIgnoreCase(ModelClass.gray.toString())) {
            content = "There is a crosswalk ahead";
        } else if (payload.equalsIgnoreCase(ModelClass.green.toString())) {
            content = "Green pedestrian signal ahead";
        } else if (payload.equalsIgnoreCase(ModelClass.red.toString())) {
            content = "Red pedestrian signal ahead";
        }


        modelClassCounter.put(payload, 0);
        modelClassTimeoutTime.put(payload, current.plusSeconds(TIMEOUT));

        infMsg.setContent(content);
        send(infMsg);

        // if red is detected, ask the user if it wants passage
        if (!payload.equalsIgnoreCase(ModelClass.red.toString())){
            return;
        }

        ACLMessage reqMsg = new ACLMessage(ACLMessage.REQUEST);

        reqMsg.addReceiver(new AID(communicationAgentName, AID.ISLOCALNAME));
        reqMsg.setConversationId(topic);
        reqMsg.setContent("Press button if you wish to cross");
        reqMsg.setReplyWith("request" + System.currentTimeMillis());

        System.out.println(ConsoleColors.GREEN + "Crosswalk Passage Agent communication request sent" + ConsoleColors.RESET);
        send(reqMsg);

        // wait for response
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(topic),
                MessageTemplate.MatchInReplyTo(reqMsg.getReplyWith()));
        ACLMessage resMsg = blockingReceive(mt);
        if (resMsg.getPerformative() == ACLMessage.REFUSE) {
            System.out.println(ConsoleColors.GREEN + "Crosswalk Passage Agent communication request refused" + ConsoleColors.RESET);
        } else if (resMsg.getPerformative() == ACLMessage.CONFIRM) {
            System.out.println(ConsoleColors.GREEN + "Crosswalk Passage Agent communication request accepted" + ConsoleColors.RESET);

            ACLMessage reqMsg2 = new ACLMessage(ACLMessage.REQUEST);

            reqMsg2.addReceiver(new AID(pedestrianAgentName, AID.ISLOCALNAME));
            reqMsg2.setConversationId(topic);
            reqMsg2.setContent(ModelClass.green.toString());
            reqMsg2.setReplyWith("request" + System.currentTimeMillis());
            System.out.println(ConsoleColors.YELLOW + "Crosswalk Passage Agent pedestrian signal request sent" + ConsoleColors.RESET);
            send(reqMsg2);

            mt = MessageTemplate.and(MessageTemplate.MatchConversationId(topic),
                    MessageTemplate.MatchInReplyTo(reqMsg2.getReplyWith()));
            ACLMessage resMsg2 = blockingReceive(mt);
            if (resMsg2.getPerformative() == ACLMessage.REFUSE) {
                System.out.println(ConsoleColors.YELLOW + "Crosswalk Passage Agent pedestrian signal request refused" + ConsoleColors.RESET);
            } else if (resMsg2.getPerformative() == ACLMessage.CONFIRM) {
                System.out.println(ConsoleColors.YELLOW + "Crosswalk Passage Agent pedestrian signal request accepted" + ConsoleColors.RESET);
            }
        }
    }
}
