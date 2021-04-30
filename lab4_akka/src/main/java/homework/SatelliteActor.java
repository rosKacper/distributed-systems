package homework;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Date;

public class SatelliteActor extends AbstractBehavior<CustomCommand> {

    public SatelliteActor(ActorContext<CustomCommand> context) {
        super(context);
    }

    public static Behavior<CustomCommand> spawn() {
        return Behaviors.setup(SatelliteActor::new);
    }

    @Override
    public Receive<CustomCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(SatelliteStation.SatelliteDataRequest.class, this::onSatelliteDataRequest)
                .build();
    }

    private Behavior<CustomCommand> onSatelliteDataRequest(SatelliteStation.SatelliteDataRequest satelliteDataRequest){
        long start = new Date().getTime();
        SatelliteAPI.Status status = SatelliteAPI.getStatus(satelliteDataRequest.satID);
        long end = new Date().getTime();
        satelliteDataRequest.replyTo.tell(new SatelliteReply(satelliteDataRequest.satID, status, end-start));
        return Behaviors.stopped();
    }

    public static class SatelliteReply implements CustomCommand{
        public final int satID;
        public final SatelliteAPI.Status status;
        public final long replyTime;

        public SatelliteReply(int satID, SatelliteAPI.Status status, long replyTime) {
            this.status = status;
            this.satID = satID;
            this.replyTime = replyTime;
        }
    }




}
