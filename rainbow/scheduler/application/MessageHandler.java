package rainbow.scheduler.application;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rainbow.scheduler.application.message.RequestQueryMessage;
import rainbow.scheduler.partition.Partition;
import rainbowpc.Message;
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
	static Logger logger = Logger.getLogger(MessageHandler.class.getName());

	public static MessageHandler createMessageAction(SchedulerServer server) {
		MessageHandler hander = new MessageHandler(server);
		return hander;
	}
	private SchedulerServer server;
	private HashMap<String, Action> actions;

	private MessageHandler(SchedulerServer server) {
		this.server = server;
		actions = new HashMap<String, Action>();
		constructActionTable();
	}

	private void constructActionTable() {
		actions.put(CacheReady.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				logger.log(Level.INFO, "Got message " + message.getMethod());
			}
		});
		actions.put(CacheRelease.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				logger.log(Level.INFO, "Got message " + message.getMethod());
			}
		});
		actions.put(CacheRequest.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				logger.log(Level.INFO, "Got message " + message.getMethod());
			}
		});
		actions.put(QueryFound.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				System.out.println("Query was successfuly, plaintext found = " + ((QueryFound) message).getPlaintext());
				for (Controller controller : server.controllers) {
					try {
						controller.stopQuery();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				server.currentQuery = null;
				server.stopWatch();
			}
		});
		actions.put(NewControllerMessage.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				NewControllerMessage newControllerMessage = (NewControllerMessage) message;
				Controller controller = new Controller(newControllerMessage.getSchedulerProtocolet());
				server.controllers.add(controller);

				logger.log(Level.INFO, "There is a new controller " + newControllerMessage.getID());
				if (server.currentQuery != null) {
					try {
						controller.sendQuery(server.currentQuery);
						for (int i = 0; i < server.MESSAGES_BUFFERED; i++) {
							Partition newPartition = server.pm.requestPartition(server.WORKSIZE);
							if (newPartition == null) {
								logger.log(Level.INFO, "Plaintext space exaused");
								return;
							}
							controller.assignPartition(newPartition);
						}
					} catch (Exception e) {
						logger.severe("Could not send new controller the current query or work block\n" + e.getMessage());
					}
				}
			}
		});
		actions.put(WorkBlockComplete.LABEL, new Action() {

			@Override
			public void execute(Message message) {
				WorkBlockComplete workBlockCompleteMessage = (WorkBlockComplete) message;
				Controller controller = server.getController(workBlockCompleteMessage.getSchedulerProtocolet());
				Partition partition = controller.findPartition(workBlockCompleteMessage.getStartBlockNumber(), workBlockCompleteMessage.getEndBlockNumber(), workBlockCompleteMessage.getStringLength());
				if (partition != null) {
					server.pm.notifyComplete(partition);
					controller.removePartition(partition);
				}
				if (server.currentQuery == null) {
					return;
				}
				logger.log(Level.INFO, "Work block is complete, sending more work");
				Partition newPartition = server.pm.requestPartition(server.WORKSIZE);
				if (newPartition == null) {
					logger.log(Level.INFO, "Plaintext space exaused");
					return;
				}
				try {
					controller.assignPartition(newPartition);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		actions.put(ControllerDisconnect.LABEL, new Action() {

			@Override
			public void execute(Message m) {
				ControllerDisconnect cdmessage = (ControllerDisconnect) m;
				Controller controller = server.getController(cdmessage.getSchedulerProtocolet());

				for (Partition p : controller.getAssignedPartitions()) {
					System.out.println("Notify failure" + p.toString());
					server.pm.notifyFailure(p);
				}
				server.controllers.remove(controller);
			}
		});
		actions.put(RequestQueryMessage.LABEL, new Action() {

			@Override
			public void execute(Message m) {
				RequestQueryMessage message = (RequestQueryMessage)m;
				server.currentQuery = message.getQuery();
				server.pm.reset();
				server.startTime = System.currentTimeMillis();
				for (Controller controller : server.controllers) {
					try {
						controller.sendQuery(server.currentQuery);
						List<Partition> assigned =
								server.pm.stripedRequestPartitions(server.WORKSIZE, server.MESSAGES_BUFFERED);
						for (Partition p : assigned) {
							controller.assignPartition(p);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void execute(Message m) {
		actions.get(m.getMethod()).execute(m);
	}
}
