### Project Structure

```
packetscope/
├── .gitattributes
├── .gitignore
├── .mvn/
│   └── wrapper/maven-wrapper.properties
├── packetscope-web/
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/mdnafih/PacketScope/
│       │   │       ├── configuration/
│       │   │       ├── controller/
│       │   │       ├── model/
│       │   │       ├── repository/
│       │   │       ├── service/
│       │   │       └── PacketScopeApplication.java
│       │   └── resources/
│       │       ├── static/
│       │       ├── templates/
│       │       ├── application.properties       
│       │       └── application-prod.properties  
│       └── test/java/com/mdnafih/PacketScope/PacketScopeApplicationTests.java
│          
└── packetscope-desktop/
    ├── pom.xml
    └── src/main/
        ├── java/
        │   └── com/packetscope/desktop/
        │        └──(controller, model, service, view, MainApp.java)       
        └── resources/
            └── (styles, images, fxml, fonts)
```