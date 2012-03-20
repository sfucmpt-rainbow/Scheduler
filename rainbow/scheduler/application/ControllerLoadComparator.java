package rainbow.scheduler.application;

import java.util.Comparator;

public class ControllerLoadComparator implements Comparator<Controller> {

	@Override
	public int compare(Controller o1, Controller o2) {
		return Integer.compare(o2.getCores(), o1.getCores());
	}

	
}
