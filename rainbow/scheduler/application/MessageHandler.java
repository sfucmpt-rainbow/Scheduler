package rainbow.scheduler.application;

import java.util.Hashtable;
import rainbow.scheduler.partition.Partition;
import rainbowpc.Message;
import rainbowpc.controller.messages.StopQuery;
import rainbowpc.controller.messages.WorkBlockSetup;
import rainbowpc.scheduler.messages.*;
import rainbowpc.scheduler.messages.CacheReady;

/**
 *
 * @author WesleyLuk
 */
public class MessageHandler {

	abstract class Action {

		public abstract void execute(Message m);
	}

	public static MessageHandler createMessageAction(SchedulerServer server) {
		MessageHandler hander = new MessageHandler(server);
		return hander;
	}
	private SchedulerServer server;
	private Hashtable<String, Action> actions;

	private MessageHandler(SchedulerServer server) {
		this.server = server;
		actions = new Hashtable<String, Action>();
		constructActionTable();
	}

	private void constructActionTable() {
		actions.put(CacheReady.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				System.out.println("Got message " + message.getMethod());
			}
		});
		actions.put(CacheRelease.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				System.out.println("Got message " + message.getMethod());
			}
		});
		actions.put(CacheRequest.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				System.out.println("Got message " + message.getMethod());
			}
		});
		actions.put(QueryFound.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				System.out.println("Query was successfuly, plaintext found = " + ((QueryFound) message).getPlaintext());
				server.query = null;
				server.broadcast(new StopQuery(server.query, "md5"));
			}
		});
		actions.put(NewControllerMessage.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				NewControllerMessage newControllerMessage = (NewControllerMessage) message;
				System.out.println("There is a new controller " + newControllerMessage.getID());
				server.controllers.add(newControllerMessage.getSchedulerProtocolet());
			}
		});
		actions.put(WorkBlockComplete.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				WorkBlockComplete workBlockCompleteMessage = (WorkBlockComplete) message;
				if (server.query == null) {
					return;
				}
				System.out.println("Work block is complete, sending more work");
				Partition p = server.pm.requestPartition(server.WORKSIZE);
				if (p == null) {
					System.out.println("Plaintext space exaused");
					return;
				}
				try {
					workBlockCompleteMessage.getSchedulerProtocolet().sendMessage(
							new WorkBlockSetup(p.stringLength, p.startBlockNumber, p.endBlockNumber));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void execute(Message m) {
		actions.get(m.getMethod()).execute(m);
	}
}
