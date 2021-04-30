package homework;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Hashtable;

public class SatelliteStation extends AbstractBehavior<CustomCommand> {

    //default values
    private int timeout = 0;
    private int satelliteNumber;
    private int satelliteCounter;
    private int queryID;
    private int success = 0;
    public ActorRef<CustomCommand> replyTo;
    private Hashtable<Integer, SatelliteAPI.Status> satellites = new Hashtable<>();

    public SatelliteStation(ActorContext<CustomCommand> context) {
        super(context);
    }

    public static Behavior<CustomCommand> spawn() {
        return Behaviors.setup(SatelliteStation::new);
    }

    @Override
    public Receive<CustomCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MonitorStation.DataRequest.class, this::onDataRequest)
                .onMessage(SatelliteActor.SatelliteReply.class, this::onSatelliteReply)
                .build();
    }

    private Behavior<CustomCommand> onDataRequest(MonitorStation.DataRequest dataRequest){

        int firstSatelliteId = dataRequest.firstSatId;
        int lastSatelliteId = dataRequest.firstSatId + dataRequest.range;
        this.timeout = dataRequest.timeout;
        this.satelliteNumber = dataRequest.range;
        this.satelliteCounter = satelliteNumber;

        this.replyTo = dataRequest.replyTo;
        this.queryID = dataRequest.queryId;
        //creating Actor for each satellite
        for(int i = firstSatelliteId; i<lastSatelliteId; i++){
            SatelliteDataRequest satelliteDataRequest = new SatelliteDataRequest(this.getContext().getSelf(), i);
            getContext().spawn(Behaviors.supervise(SatelliteActor.spawn())
                    .onFailure(Exception.class, SupervisorStrategy.restart()), "Satellite" + i, DispatcherSelector.fromConfig("my-dispatcher")).tell(satelliteDataRequest);
        }

        return this;
    }

    private Behavior<CustomCommand> onSatelliteReply(SatelliteActor.SatelliteReply satelliteReply){
        this.satelliteCounter--;
        if(satelliteReply.replyTime <= this.timeout){
            success++;
            if(!satelliteReply.status.equals(SatelliteAPI.Status.OK)){
                satellites.put(satelliteReply.satID, satelliteReply.status);
            }
        }
        if(satelliteCounter ==0){
            this.replyTo.tell(new SatelliteDataReply(satellites, queryID, (success*100)/satelliteNumber));
            return Behaviors.stopped();
        }

        return this;
    }

    public static class SatelliteDataRequest implements CustomCommand{
        public final ActorRef<CustomCommand> replyTo;
        public final int satID;


        public SatelliteDataRequest(ActorRef<CustomCommand> replyTo, int satID) {
            this.replyTo = replyTo;
            this.satID = satID;
        }
    }

    public static class SatelliteDataReply implements CustomCommand{

        public final Hashtable<Integer, SatelliteAPI.Status> satellites;
        public final int querryID;
        public final int workingPercent;

        public SatelliteDataReply(Hashtable<Integer, SatelliteAPI.Status> satellites, int querryID, int workingPercent) {
            this.satellites = satellites;
            this.querryID = querryID;
            this.workingPercent = workingPercent;
        }
    }




}
