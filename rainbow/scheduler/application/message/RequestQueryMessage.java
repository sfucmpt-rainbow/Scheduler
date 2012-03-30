/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rainbow.scheduler.application.message;

import rainbow.scheduler.application.HashQuery;
import rainbowpc.scheduler.messages.SchedulerMessage;

/**
 *
 * @author WesleyLuk
 */
public class RequestQueryMessage extends SchedulerMessage {

	public static String LABEL = "requestQueryMessage";
	HashQuery query;

	public RequestQueryMessage(HashQuery q) {
		super(LABEL, "");
		this.query = q;
	}
}
