# README #

This Java project can be used to generate device service scaffolding using newly developed Device Service SDK.

### Setup ###

The project can be run directly from Spring Tools. It can also be exported as Runnable JAR and run from command line.

Run configuration expects two parameters:

1. Output directory name where files are generated. 

2. Device Service description file. By default the file is expected to be in the project root directory. Examples are included in the tools project root directory.

To generate a new device service:

1. Create a new service configuration file using the included file 'Demo' as a template.

a. Set the Service name field of the new service. Convention is device-<protocol name>

b. Set the service port. Be sure to check the current service ports being used and pick an unused one. Current conventions place device services in the 49000-49999 port range

c. Set the (CSV) service labels. For the service to be recognized as capable of running schedules, leave the scheduler label in place

d. Set the profile attributes that will be generated in the domain.<Protocol name>Attribute.java file. These fields are passed to the driver layer and must also be included in each deviceResource attribute field of every device profile for a service. These are protocol specific metadata objects characteristics of a protocol (i.e. uuid for BLE, oid and community for SNMP, etc.). The first column is the Attribute name, the second is the java type.

2. Run the Generate Device Service command with <path to new device service project parent directory> and <path to service configuration file> as arguments

3. Import your newly generated Device Service project into the IDE of your choice

4. Run a maven install on the project to install the project dependencies

5. Replace the numbered TODOs in the src/main/ directory:

a. TODO 1-7 are in the <Protocol name>.<Protocol name>Driver.java file

b. TODO 8 is in the domain.SimpleWatcher.java file

c. TODO 9 is in the resources/schedule.properties file

d. TODO 10 is in the resources/watcher.properties file

Note: TODOs marked [Optional] may be left as is for some services, see the note with the TODO

6. Generate a device profile for use by the service. Use the included resources/JC.RR-NAE-9.ConfRoom.Padre.Island.profile.yaml as a template. Be sure to replace the deviceResources attributes with the profile attributes in your service configuration file

7. Run and test the service locally until satisfied with functionality

8. Dockerize the service (may require injecting different .properties files as part of the build chain, see the device-bluetooth Jenkins Configuration for an example of this)

9. Deploy to EdgeX
