package Agents;

import Mqtt.*;
import Util.ConsoleColors;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PedestrianSignalAgent extends AgentMQTT {
    public enum Signal {
        red,
        green
    }
    final int TICKER_TIMER = 100000; // ms
    public String publishTopic;
    public Signal lightState = Signal.red;
    DriverHiveMQTT driver;
    public TickerBehaviour signalTicker;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.publishTopic = (String) args[0];
        }

        driver = new DriverHiveMQTT(this);

        registerYellowPage("semaphore");
        System.out.println("Pedestrian Signal Client Agent Started ::: " + getLocalName());

        signalTicker = new SignalTicker(this, TICKER_TIMER);
        addBehaviour(signalTicker);
        addBehaviour(new CrossingRequestListener());
    }

    private class CrossingRequestListener extends CyclicBehaviour{
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);

            if (msg == null){
                return;
            }

            if (lightState == Signal.red) {
                SwitchState();
            }

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.CONFIRM);
            reply.setContent(lightState.toString());
            send(reply);
        }
    }

    private class SignalTicker extends TickerBehaviour {
        public SignalTicker(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            SwitchState();
        }
    }

    private void SwitchState() {
        lightState = lightState == Signal.green ? Signal.red: Signal.green;
        System.out.println(ConsoleColors.YELLOW + "Pedestrian Signal Agent change state to: " + ConsoleColors.RESET + lightState);

        driver.PublishMessage(publishTopic, lightState.toString(), 0, true);
        signalTicker.reset();
    }
}
