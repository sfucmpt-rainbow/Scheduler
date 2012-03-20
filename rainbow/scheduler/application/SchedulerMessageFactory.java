/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rainbow.scheduler.application;

import rainbow.scheduler.partition.Partition;
import rainbowpc.controller.messages.NewQuery;
import rainbowpc.controller.messages.StopQuery;
import rainbowpc.controller.messages.WorkBlockSetup;
import rainbowpc.scheduler.messages.WorkBlockComplete;

/**
 *
 * @author WesleyLuk
 */
public class SchedulerMessageFactory {

	public static StopQuery createStopQuery(HashQuery query) {
		return new StopQuery(query.getQuery(), query.getQueryID(), query.getMethod());
	}

	public static WorkBlockSetup createWorkBlock(Partition p, HashQuery query) {
		return new WorkBlockSetup(p.stringLength, p.startBlockNumber, p.endBlockNumber, query.getQueryID());
	}

	public static Partition createPartition(WorkBlockComplete block) {
		return new Partition(block.getStringLength(), block.getStartBlockNumber(), block.getEndBlockNumber());
	}

	public static NewQuery createNewQuery(HashQuery query) {
		return new NewQuery(query.getQuery(), query.getQueryID(), query.getMethod());
	}
}
