package rainbow.scheduler.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import rainbow.scheduler.partition.AlphabetGenerator;
import rainbow.scheduler.partition.Partition;
import rainbow.scheduler.partition.PartitionManager;
import rainbowpc.Message;
import rainbowpc.scheduler.SchedulerProtocol;

public class SchedulerServer extends Thread {

	Executor executor;
	SchedulerProtocol protocol;
	HashQuery currentQuery;
	PartitionManager pm;
	String alphabet;
	ArrayList<Controller> controllers = new ArrayList<>();
	MessageHandler messageHandler;
	public static int WORKSIZE = 2;

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
		broadcast(SchedulerMessageFactory.createNewQuery(currentQuery));
		pm.reset();
		for (Controller controller : controllers) {
			Partition p = pm.requestPartition(WORKSIZE);
			try {
				controller.getProtocol().sendMessage(
						SchedulerMessageFactory.createWorkBlock(p, currentQuery));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
