package rainbow.scheduler.application;

import rainbowpc.controller.messages.ControllerBootstrapMessage;
import rainbowpc.Message;
import rainbowpc.controller.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rainbowpc.controller.messages.*;
import rainbowpc.scheduler.messages.WorkBlockComplete;

public class BruteForceController extends Thread {

	ExecutorService executor;
	ControllerProtocol protocol;
	NewQuery query;
	BruteForcer current;
	public BruteForceController() {
		executor = Executors.newSingleThreadExecutor();
		try {
			protocol = new ControllerProtocol("localhost");
		} catch (IOException e) {
			System.out.println("Could not connect to scheduler, has it been started?");
			System.exit(1);
		}
	}

	@Override
	public void start() {
		super.start();
		executor.execute(protocol);
	}

	@Override
	public void run() {
		while (true) {
			Message message;
			try {
				message = protocol.getMessage();
				System.out.println(message);
				switch (message.getMethod()) {
					case CacheRequestResponse.LABEL:
						break;
					case NewQuery.LABEL:
						query = (NewQuery)message;
						System.out.println("A new query has been recieved " + query.getQuery());
						break;
					case StopQuery.LABEL:
						System.out.println("Recieved message " + message.getMethod());
						break;
					case ControllerBootstrapMessage.LABEL:
						System.out.println("Bootstrap message found!");
						ControllerBootstrapMessage bootstrap = (ControllerBootstrapMessage) message;
						System.out.println("Scheduler assigned id: " + bootstrap.id);
						System.out.println("All done, great success!");
						break;
					case WorkBlockSetup.LABEL:
						WorkBlockSetup workBlock = (WorkBlockSetup) message;
						System.out.println("Got work block " + workBlock.getStartBlockNumber());
						bruteForce(workBlock);
						break;
					default:
						System.out.println("Test failed, bootstrap message not received");
						break;
				}
			} catch (InterruptedException ie) {
				interrupt();
				break;
			}
		}
	}
	public void bruteForce(WorkBlockSetup block){
		current = new BruteForcer(query, block);
		current.start();
	}
	@Override
	public void interrupt() {
		super.interrupt();
		protocol.shutdown();
		executor.shutdown();
	}

	public static void main(String[] s) {
		new BruteForceController().start();
	}
}
