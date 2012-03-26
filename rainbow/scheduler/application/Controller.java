package rainbow.scheduler.application;

import java.io.IOException;
import java.util.ArrayList;
import rainbow.scheduler.partition.Partition;
import rainbowpc.controller.messages.WorkBlockSetup;
import rainbowpc.scheduler.SchedulerProtocolet;
import rainbowpc.scheduler.messages.WorkBlockComplete;

/**
 *
 * Contains the information relating to a specific connected controller
 */
public class Controller {

	private SchedulerProtocolet protocol;
	private ArrayList<Partition> assignedPartitions;
	private HashQuery currentQuery;

	public Controller(SchedulerProtocolet protocol) {
		this.protocol = protocol;
		assignedPartitions = new ArrayList<Partition>();
	}

	/*
	 * Accessor methods
	 */
	public SchedulerProtocolet getProtocol() {
		return protocol;
	}

	public ArrayList<Partition> getAssignedPartitions() {
		return assignedPartitions;
	}

	public HashQuery getCurrentQuery() {
		return currentQuery;
	}

	public void setCurrentQuery(HashQuery currentQuery) {
		this.currentQuery = currentQuery;
	}

	public void sendQuery(HashQuery query) throws IOException {
		currentQuery = query;
		protocol.sendMessage(SchedulerMessageFactory.createNewQuery(query));
	}

	public void stopQuery() throws IOException {
		protocol.sendMessage(SchedulerMessageFactory.createStopQuery(currentQuery));
		currentQuery = null;
	}

	public void assignPartition(Partition p) throws IOException {
		assignedPartitions.add(p);
		protocol.sendMessage(SchedulerMessageFactory.createWorkBlock(p, currentQuery));
	}

	public void removePartition(WorkBlockComplete message) {
		assignedPartitions.remove(SchedulerMessageFactory.createPartition(message));
	}
}
