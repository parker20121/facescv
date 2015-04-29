# facescv

### Overview

This Java application wraps the OpenCV libraries, providing a facial recognition
capability. The user can train the system through a command line interface
and submit images to query the knowledgebase for matches. If matches 
are recognized, the system will return the associated identifiers.

The following commands are supported:

   * create [facial recognition model] [path to store model]
   * train [training image directory]
   * load [model location]
   * save [model location]
   * find [image path]

Compiling the application with Apache Maven

    mvn clean package assemlby:single -Dplatform.dependencies=true

Running the application with Apache Maven

    mvn exec:java -Dplatform.dependencies=true -Dexec.mainClass=com.l3nss.faces.App

Running the application from the command line

    java -Xms4g -Xmx6g -jar target/facescv-1.0-jar-with-dependencies.jar

### Usage Example

After starting the application, the user is free to build a facial recognition
model.

At the command line, create a new database with the Eigen facial recognition class. Store
the database at the path /opt/test/database

    Enter command: create EIGEN /opt/test/database

> EigenFaceRecognizer loaded. 

Train the system with a collection of png and jpg files. The system will look for 
image files starting at the parent directory /opt/images

    Enter command: train /opt/images

> 

