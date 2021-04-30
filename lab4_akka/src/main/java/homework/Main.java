package homework;


import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        File configFile = new File(".\\src\\main\\java\\homework\\resources\\dispatcher.conf");
        Config config = ConfigFactory.parseFile(configFile);

        ActorSystem.create(Main.create(), "main", config);
    }

    public static Behavior<Void> create() {

        return Behaviors.setup(
                context -> {
                    ActorRef<CustomCommand> dispatcher = context.spawn(Behaviors.supervise(Dispatcher.spawn())
                            .onFailure(Exception.class, SupervisorStrategy.resume()), "dispatcher", DispatcherSelector.fromConfig("my-dispatcher"));

                    ActorRef<CustomCommand> monitorStation1 = context.spawn(Behaviors.supervise(MonitorStation.spawn())
                            .onFailure(Exception.class, SupervisorStrategy.resume()), "monitorStation1", DispatcherSelector.fromConfig("my-dispatcher"));
                    ActorRef<CustomCommand> monitorStation2 = context.spawn(Behaviors.supervise(MonitorStation.spawn())
                            .onFailure(Exception.class, SupervisorStrategy.resume()), "monitorStation2", DispatcherSelector.fromConfig("my-dispatcher"));
                    ActorRef<CustomCommand> monitorStation3 = context.spawn(Behaviors.supervise(MonitorStation.spawn())
                            .onFailure(Exception.class, SupervisorStrategy.resume()), "monitorStation3", DispatcherSelector.fromConfig("my-dispatcher"));

                    Thread.sleep(2000);

                    monitorStation1.tell(new MonitorStation.DataRequest(dispatcher, Math.abs((new Random().nextInt()))%1000000, 1000 + new Random().nextInt(50), 50, 300));
                    monitorStation1.tell(new MonitorStation.DataRequest(dispatcher, Math.abs((new Random().nextInt()))%1000000,100 + new Random().nextInt(50), 50, 300));
                    monitorStation2.tell(new MonitorStation.DataRequest(dispatcher, Math.abs((new Random().nextInt()))%1000000,100 + new Random().nextInt(50), 50, 300));
                    monitorStation2.tell(new MonitorStation.DataRequest(dispatcher, Math.abs((new Random().nextInt()))%1000000,100 + new Random().nextInt(50), 50, 300));
                    monitorStation3.tell(new MonitorStation.DataRequest(dispatcher, Math.abs((new Random().nextInt()))%1000000,100 + new Random().nextInt(50), 50, 300));
                    monitorStation3.tell(new MonitorStation.DataRequest(dispatcher, Math.abs((new Random().nextInt()))%1000000,100 + new Random().nextInt(50), 50, 300));


                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                }
        );
    }
}