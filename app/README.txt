This folder contains:

All server application build modules / maven projects.

The modules are:

client - contains sources of a client library. A client library is needed
         if you want to provide basic server objects to a non-EE client
		 such as an Android client application. Typically the library will
		 contains POJOs (plain Java objects) for the JSON interface used
		 on client and server side.

ear    - contains the files and descriptors need for building an enterprise
         ear file. By default, this will combine the "ejb" module and the
		 "war" module (see below) into the enterprise application.

ejb    - the application's EJB module containing session beans and interfaces,
         entity beans and core server classes.
		 
war    - the application's web module containing the web frontend files and
         web services.
