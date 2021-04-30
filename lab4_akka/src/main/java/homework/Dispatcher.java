package homework;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Random;

public class Dispatcher extends AbstractBehavior<CustomCommand> {

    public Dispatcher(ActorContext<CustomCommand> context) {
        super(context);
    }

    @Override
    public Receive<CustomCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(MonitorStation.DataRequest.class, this::onDataRequest)
                .build();
    }

    public static Behavior<CustomCommand> spawn() {
        return Behaviors.setup(Dispatcher::new);
    }

    private Behavior<CustomCommand> onDataRequest(MonitorStation.DataRequest dataRequest){
        getContext()
                .spawn(Behaviors.supervise(SatelliteStation.spawn())
                        .onFailure(Exception.class, SupervisorStrategy.resume()), "SatelliteStation" + dataRequest.queryId, DispatcherSelector.fromConfig("my-dispatcher"))
                .tell(dataRequest);
        return this;
    }
}
