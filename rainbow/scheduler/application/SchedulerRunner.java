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
				String command = params[0];
				// Too lazy to make a mapping just live with an if else chain for now
				if (command.equals("quit")) {
					done = true;
				} else if (command.equals("query")) {
					ss.newQuery(params[1]);
				} else if (command.equals("test1")) {
					// md5 of "z"
					ss.newQuery("fbade9e36a3f36d3d676c1b808451dd7");
				} else if (command.equals("test2")) {
					// md5 of "test"
					ss.newQuery("098f6bcd4621d373cade4e832627b4f6");
				} else if (command.equals("test3")) {
					// md5 of "asdfgh"
					ss.newQuery("a152e841783914146e4bcd4f39100686");
				} else if (command.equals("test4")) {
					// md5 of "asdfgh"
					ss.newQuery("c83b2d5bb1fb4d93d9d064593ed6eea2");
				} else {

					System.out.println("Unknown command " + params[0]);
				}
			} while (!done);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ss.callInterrupt();
	}
}
