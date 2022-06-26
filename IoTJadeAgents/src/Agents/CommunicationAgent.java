package Agents;

import Mqtt.*;
import Util.ConsoleColors;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class CommunicationAgent extends AgentMQTT {
    private String publishTopic;
    private String requestPublishTopic;
    private String requestSubscribeTopic;

    private ACLMessage request = null;
    private DriverHiveMQTT driver;

    @Override
    public void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.publishTopic = (String) args[0];
            this.requestPublishTopic = (String) args[1];
            this.requestSubscribeTopic = (String) args[2];
        }

        try {
            driver = new DriverHiveMQTT(this);
            driver.SubscribeTopic(requestSubscribeTopic, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        registerYellowPage("communication");

        System.out.println("Communication Agent Started ::: " + getLocalName());
        addBehaviour(new CommunicationInformListener());
        addBehaviour(new CommunicationRequestListener());
    }

    private class CommunicationInformListener extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();

            if (msg == null || msg.getPerformative() != ACLMessage.INFORM){
                return;
            }

            String content = msg.getContent();

            System.out.println(ConsoleColors.GREEN + "Communication Agent: " + ConsoleColors.RESET + content);

            driver.PublishMessage(publishTopic, content);
        }
    }

    private class CommunicationRequestListener extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();

            if (msg == null || msg.getPerformative() != ACLMessage.REQUEST & request != null){
                return;
            }

            request = msg;

            String content = msg.getContent();

            System.out.println(ConsoleColors.GREEN + "Communication Agent: " + ConsoleColors.RESET + content);

            driver.PublishMessage(requestPublishTopic, content, 1);
        }
    }

    @Override
    public void ProcessMessageArrived(String topic, String payload){
        if (!topic.equalsIgnoreCase(requestSubscribeTopic) | request == null) {
            return;
        }

        System.out.println(ConsoleColors.GREEN + "Communication Agent Request: " + ConsoleColors.RESET + payload);

        ACLMessage reply = request.createReply();
        if(payload.equalsIgnoreCase("True")){
            reply.setPerformative(ACLMessage.CONFIRM);
        }else{
            reply.setPerformative(ACLMessage.REFUSE);
        }
        reply.setContent(payload);
        send(reply);
        request = null;
    }
}
