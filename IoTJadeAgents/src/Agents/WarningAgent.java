package Agents;

import Mqtt.*;
import Util.ConsoleColors;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WarningAgent extends AgentMQTT {
    private String publishTopic;
    private DriverHiveMQTT driver;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.publishTopic = (String) args[0];
        }

        driver = new DriverHiveMQTT(this);

        registerYellowPage("warning");
        addBehaviour(new WarningInformListener());

        System.out.println("Warning Agent Started ::: " + getLocalName());
    }

    private class WarningInformListener extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = blockingReceive(mt);

            if (msg == null){
                return;
            }

            String content = msg.getContent();

            System.out.println(ConsoleColors.BLUE + "Warning Agent: " + ConsoleColors.RESET + content);

            driver.PublishMessage(publishTopic, content);
        }
    }
}
