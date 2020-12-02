package com.server;

import javax.servlet.ServletException;
import com.undertow.standalone.UndertowServer;
import static com.util.cloud.DeploymentConfiguration.getProperty;

import java.util.concurrent.locks.Condition;


public class Server {

	public static void main(String[] args)
			throws ServletException {

		final String  host = getProperty("undertow.host", "0.0.0.0");
		final Integer port = getProperty("undertow.port", 8082);

		final UndertowServer server = new UndertowServer(host, port, "essentialProgramming.jar");
		
		final Condition newCondition = server.LOCK.newCondition();
		
		server.start();
		try {
			while( true )
				newCondition.awaitNanos(100000000);
		} catch ( InterruptedException cause ) {
			server.stop();
		}
	}

}
