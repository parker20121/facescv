# facescv

### Overview

This Java application wraps the OpenCV libraries, providing a facial recognition
capability. The user can train the system through a command line interface
and submit images to query the knowledgebase for matches. If matches 
are recognized, the system will return the associated identifiers.

### Building the Application

Your system should have the latest [OpenCV](http://opencv.org) installed on 
the computer.

Compiling the application with Apache Maven

    mvn package assembly:single -Dplatform.dependencies=true

Running the application with Apache Maven

    mvn exec:java -Dplatform.dependencies=true -Dexec.mainClass=com.l3nss.faces.App

Running the application from the command line

    java -Xms4g -Xmx6g -jar target/facescv-1.0-jar-with-dependencies.jar

### Application Commands

The following commands are supported when the application is running.

Create facial recognition model, stored as specified path

    create [facial recognition model] [path to store model]

Train the model using a directory of images

    train [training image directory]

Load facial recognition model from disk

    load [model location]

Save facial recognition model to disk

    save [model location]

Find image located at a specific path

    find [image path]

### Usage Example

After starting the application, the user is free to build a facial recognition
model. The system will provide the following user prompt:

    Enter command:

At the command line, create a new facial recognition model and store it 
at a specified location. User's can chose from the following models:

   * EIGEN
   * FISHER
   * LBPH

Store the database at the path /opt/test/database

    Enter command: create EIGEN /opt/test/database

Train the system with a collection of png and jpg files. The system will look for 
image files starting at the parent directory /opt/images

    Enter command: train /opt/images

### Copyright

Copyright 2015 Matt Parker
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.