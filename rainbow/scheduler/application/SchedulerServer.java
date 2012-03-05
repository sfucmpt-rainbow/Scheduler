package rainbow.scheduler.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import rainbow.scheduler.partition.AlphabetGenerator;
import rainbow.scheduler.partition.Partition;
import rainbow.scheduler.partition.PartitionManager;
import rainbowpc.Message;
import rainbowpc.controller.messages.NewQuery;
import rainbowpc.controller.messages.WorkBlockSetup;
import rainbowpc.scheduler.SchedulerProtocol;
import rainbowpc.scheduler.SchedulerProtocolet;
import rainbowpc.scheduler.messages.*;

public class SchedulerServer extends Thread {

	Executor executor;
	SchedulerProtocol protocol;
	String query = null;
	PartitionManager pm;
	String alphabet;
	ArrayList<SchedulerProtocolet> controllers = new ArrayList<>();

	public SchedulerServer() {
		alphabet = AlphabetGenerator.generateAlphabet(AlphabetGenerator.Types.LOWER_CASE);
		pm = new PartitionManager(alphabet, 4);
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
		for (SchedulerProtocolet prot : controllers) {
			try {
				prot.sendMessage(message);
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
		this.query = query;
		broadcast(new NewQuery(query, "md5"));
		Partition p = pm.requestPartition(10);
		broadcast(new WorkBlockSetup(p.stringLength, p.startBlockNumber, p.endBlockNumber));
	}

	public void start() {
		super.start();
		executor.execute(protocol);
	}
	// simulates a scheduler server

	public void run() {
		while (true) {
			try {
				SchedulerMessage message = (SchedulerMessage) protocol.getMessage();
				switch (message.getMethod()) {
					case CacheReady.LABEL:
					case CacheRelease.LABEL:
					case CacheRequest.LABEL:
					case QueryFound.LABEL:
						System.out.println("Got message " + message.getMethod());
						break;
					case NewControllerMessage.LABEL:
						System.out.println("There is a new controller " + message.getID());
						controllers.add(message.getSchedulerProtocolet());
						break;
					case WorkBlockComplete.LABEL:
						System.out.println("Work block is complete, telling remote to shutdown");
						break;
					default:
						System.out.println("Unexpected message " + message);
						break;
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				System.out.println("Thread was interrupted, exiting");
				break;
			}
		}
		System.exit(0);
	}

	public static void main(String[] args) {
		SchedulerServer ss = new SchedulerServer();
		ss.start();
		Scanner s = new Scanner(System.in);
		boolean done = false;
		try {
			do {
				String line = s.nextLine();
				String[] params = line.split(" ");
				switch (params[0]) {
					case "quit":
						done = true;
						break;
					case "query":
						ss.newQuery(params[1]);
						break;
					case "test1":
						// md5 of "z"
						ss.newQuery("fbade9e36a3f36d3d676c1b808451dd7");
						break;
					case "test2":
						// md5 of "test"
						ss.newQuery("098f6bcd4621d373cade4e832627b4f6");
						break;
					default:
						System.out.println("Unknown command " + params[0]);
						break;
				}
			} while (!done);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ss.callInterrupt();
	}
}
