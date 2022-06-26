import Agents.*;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

public class Main {
    public static void main(String[] args) {

        // setup jade
        Runtime rt = jade.core.Runtime.instance();
        rt.setCloseVM(true);
        Profile pMain = new ProfileImpl();
        pMain.setParameter(Profile.MTPS, null);
        pMain.setParameter(Profile.LOCAL_HOST, "localhost");
        pMain.setParameter(Profile.LOCAL_PORT, "1099");
        AgentContainer ac = rt.createMainContainer(pMain);

        // setup agents
        try {
            for (int id = 1; id < 4; id++) {
                String warningAgentName = "Warning-" + id;
                String warningTopic = "sensor/warning/" + id;
                ac.createNewAgent(warningAgentName, WarningAgent.class.getName(), new Object[]{warningTopic}).start();

                String detectionAgentName = "Distance_Detection-" + id;
                String detectionTopic = "sensor/distance/" + id;
                ac.createNewAgent(detectionAgentName, DistanceDetectionAgent.class.getCanonicalName(), new Object[]{detectionTopic, warningAgentName}).start();
            }

            String pedestrianAgentName = "Pedestrian_Signal";
            String pedestrianTopic = "semaphore";
            ac.createNewAgent(pedestrianAgentName, PedestrianSignalAgent.class.getName(), new Object[]{pedestrianTopic}).start();

            String communicationAgentName = "Communication";
            String communicationTopic = "device/audio";
            String crossingRequestTopic = "cross/request";
            String crossingAnswerTopic = "cross/answer";
            ac.createNewAgent(communicationAgentName, CommunicationAgent.class.getName(), new Object[]{communicationTopic, crossingRequestTopic, crossingAnswerTopic}).start();

            String crosswalkAgentName = "Crosswalk Passage";
            String crosswalkTopic = "camera/detection";
            ac.createNewAgent(crosswalkAgentName, CrosswalkPassageAgent.class.getName(), new Object[]{crosswalkTopic, communicationAgentName, pedestrianAgentName}).start();
        } catch (Exception ex) {
            System.out.println(ex);
        }


    }
}
