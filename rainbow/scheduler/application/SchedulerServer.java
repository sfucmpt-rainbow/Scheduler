package rainbow.scheduler.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import rainbow.scheduler.partition.AlphabetGenerator;
import rainbow.scheduler.partition.Partition;
import rainbow.scheduler.partition.PartitionManager;
import rainbowpc.Message;
import rainbowpc.scheduler.SchedulerProtocol;
import rainbowpc.scheduler.SchedulerProtocolet;

public class SchedulerServer extends Thread {

	Executor executor;
	SchedulerProtocol protocol;
	HashQuery currentQuery;
	PartitionManager pm;
	String alphabet;
	ArrayList<Controller> controllers = new ArrayList<Controller>();
	MessageHandler messageHandler;
	long startTime = System.currentTimeMillis();

	public static int WORKSIZE = 2;
	public static int MESSAGES_BUFFERED = 3;

	public SchedulerServer() {
		alphabet = AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE);
		pm = new PartitionManager(alphabet, 8);
		messageHandler = MessageHandler.createMessageAction(this);
		try {
			executor = Executors.newSingleThreadExecutor();
			protocol = new SchedulerProtocol();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		buildHook();
	}

	public void callInterrupt() {
		this.interrupt();
	}

	public void broadcast(Message message) {
		for (Controller prot : controllers) {
			try {
				prot.getProtocol().sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Controller getController(SchedulerProtocolet protocol) {
		for (Controller controller : controllers) {
			if (controller.getProtocol() == protocol) {
				return controller;
			}
		}
		return null;
	}

	public void buildHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				// this.interrupt() would be ambiguous
				callInterrupt();
				protocol.shutdown();
				Thread handle = new Thread(protocol);
				byte retries = 0;
				final byte totalRetries = 5;
				while (retries < totalRetries && !protocol.hasExited()) {
					handle.interrupt();
					System.out.println("Waiting...");
					retries++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
				if (retries < 5) {
					System.out.println("Graceful shutdown completed!");
				} else {
					System.out.println("Graceful shutdown failed, abort, abort!");
				}
			}
		});
	}

	public void newQuery(String query) {
		if (!query.matches("^[0-9a-f]{1,32}$")) {
			System.out.println("Error, invalid query");
			return;
		}
		currentQuery = new HashQuery(query, "md5");
		pm.reset();
		startTime = System.currentTimeMillis();
		for (Controller controller : controllers) {
			try {
				controller.sendQuery(currentQuery);
				List<Partition> assigned = 
					pm.stripedRequestPartitions(WORKSIZE, MESSAGES_BUFFERED);
				for (Partition p : assigned) {
					controller.assignPartition(p);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void stopWatch() {
		double seconds = (double)(System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Query took " + seconds + " seconds to complete");
	}

	public void start() {
		super.start();
		executor.execute(protocol);
	}
	// simulates a scheduler server

	public void run() {
		while (true) {
			try {
				Message message = protocol.getMessage();
				messageHandler.execute(message);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				System.out.println("Thread was interrupted, exiting");
				break;
			}
		}
		System.exit(0);
	}
}
