/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rainbow.scheduler.application;

import java.util.Scanner;

/**
 *
 * @author WesleyLuk
 */
public class SchedulerRunner {

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
