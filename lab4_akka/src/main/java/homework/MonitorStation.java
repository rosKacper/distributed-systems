package homework;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Date;
import java.util.Hashtable;


public class MonitorStation extends AbstractBehavior<CustomCommand> {

    Hashtable<Integer, Long> queryStart = new Hashtable<>();
    public MonitorStation(ActorContext<CustomCommand> context) {
        super(context);
    }

    @Override
    public Receive<CustomCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MonitorStation.DataRequest.class, this::onDataRequest)
                .onMessage(SatelliteStation.SatelliteDataReply.class, this::onSatelliteDataReply)
                .build();
    }

    public static Behavior<CustomCommand> spawn() {
        return Behaviors.setup(MonitorStation::new);
    }



    private Behavior<CustomCommand> onDataRequest(MonitorStation.DataRequest dataRequest){
        int queryID = dataRequest.queryId;
        queryStart.put(queryID, new Date().getTime());
        MonitorStation.DataRequest dataRequest1 = new DataRequest(
                this.getContext().getSelf(),queryID, dataRequest.firstSatId, dataRequest.range, dataRequest.timeout);
        dataRequest.replyTo.tell(dataRequest1);
        return this;
    }

    public static class DataRequest implements CustomCommand{
        public final ActorRef<CustomCommand> replyTo;
        public final int queryId;
        public final int firstSatId;
        public final int range;
        public final int timeout;

        public DataRequest(ActorRef<CustomCommand> replyTo, int queryId, int firstSatId, int range, int timeout) {
            this.replyTo = replyTo;
            this.queryId = queryId;
            this.firstSatId = firstSatId;
            this.range = range;
            this.timeout = timeout;
        }
    }


    private Behavior<CustomCommand> onSatelliteDataReply(SatelliteStation.SatelliteDataReply satelliteDataReply){
        StringBuilder result = new StringBuilder();
        result.append("\n QuerryID: ");
        result.append(satelliteDataReply.querryID);
        result.append("\n Reply time: ");
        result.append(new Date().getTime() - queryStart.get(satelliteDataReply.querryID));
        result.append("ms");
        result.append("\n Responding satellites: ");
        result.append(satelliteDataReply.workingPercent);
        result.append("%");
        result.append("\n Error count: ");
        result.append(satelliteDataReply.satellites.size());
        result.append("\n Satellites:");
        for(Integer ID: satelliteDataReply.satellites.keySet()){
            result.append("\n ID: ");
            result.append(ID);
            result.append("  error: ");
            result.append(satelliteDataReply.satellites.get(ID));
        }
        System.out.println(result);
        return this;
    }
}
