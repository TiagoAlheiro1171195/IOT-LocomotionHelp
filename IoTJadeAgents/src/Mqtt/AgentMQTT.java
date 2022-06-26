package Mqtt;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class AgentMQTT extends Agent {

    public String GetAgentName() {
        return this.getLocalName();
    }
    public void ProcessMessageArrived(String topic, String payload){
        System.out.println(topic + " : " + payload);
    }

    public void registerYellowPage(String type){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type);
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            System.out.println(fe.getMessage());
        }
    }
}
