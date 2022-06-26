package Agents;

import Mqtt.*;
import Util.ConsoleColors;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class DistanceDetectionAgent extends AgentMQTT {
    final float VALUE_RANGE = 10; // cm
    final float MIN_WARNING_VALUE = 100; // cm
    final int MIN_REPEATED = 2;
    private String subscribeTopic;
    private String warningAgentName;

    private float distance = 0;
    private int repeated = 0;
    private DriverHiveMQTT driver;

    @Override
    public void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.subscribeTopic = (String) args[0];
            this.warningAgentName = (String) args[1];
        }

        try {
            driver = new DriverHiveMQTT(this);
            driver.SubscribeTopic(subscribeTopic);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        registerYellowPage("distance-detection");
        System.out.println("Distance Detection Agent Started ::: " + getLocalName());
    }

    @Override
    public void ProcessMessageArrived(String topic, String payload){
        if (!topic.equalsIgnoreCase(subscribeTopic)) {
            return;
        }

        UpdateState(payload);

        // exit if the minimum repeated times or the minimum value are not met
        if(repeated < MIN_REPEATED || distance > MIN_WARNING_VALUE) {
            return;
        }

        // build message to inform warning agent
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID(warningAgentName, AID.ISLOCALNAME));

        double intensity = Math.round(-0.045226 * distance + 10) / 10.0;
        msg.setContent(String.valueOf(intensity));
        msg.setConversationId(topic);

        System.out.println(ConsoleColors.BLUE + "Distance Detection Agent: " + ConsoleColors.RESET + msg.getContent());
        send(msg);
    }

    private void UpdateState(String data) {
        float sensorValue = Float.parseFloat(data);
        if (sensorValue >= distance - VALUE_RANGE & sensorValue <= distance + VALUE_RANGE){
            distance = (sensorValue + distance) / 2;
            repeated += 1;
        } else {
            distance = sensorValue;
            repeated = 0;
        }
    }
}
