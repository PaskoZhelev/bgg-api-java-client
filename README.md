# BGG Java API Client

A modern, fast, and simple Java client for accessing the BoardGameGeek (BGG) XML API 2. This library is built using Java 11's native HttpClient and Jackson for XML parsing, featuring built-in retry logic for BGG's rate-limiting (429 Too Many Requests).

### 🚀 Installation (JitPack)

This library is published via JitPack. To use it in your Maven project, follow these two steps.
1. Add the JitPack Repository

Add the following <repository> block to your main pom.xml:
XML

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the Dependency

Add the dependency block to your pom.xml. The GROUP_ID is com.github.YOUR_USERNAME and the ARTIFACT_ID is your repository name.

```xml
<dependencies>
    <dependency>
        <groupId>com.github.PaskoZhelev</groupId>
        <artifactId>bgg-api-java-client</artifactId>
        <version>1.0.0</version> 
    </dependency>
</dependencies>
```

### 🔑 Authentication

The BoardGameGeek XML API 2 generally requires obtaining an Authorization Token.
Follow the official BGG API documentation on the process for obtaining a token. [BGG Request Token](https://boardgamegeek.com/using_the_xml_api)

#### Using the Token in the Client

The token must be provided during the initialization of the BggApi client. It will be automatically sent in every request as an Authorization: Bearer {{token}} header.
```java
import com.pmz.bgg.java.client.BggApi;

// If you have a token
String userToken = "YOUR_SECRET_BGG_TOKEN";
BggApi authenticatedClient = new BggApi(userToken);
```
### 📚 Usage Examples
#### 1. Get Board Game Details

Retrieve extended information for a game using its BGG ID.
```java
import com.omertron.bgg.model.BoardGameExtended;
import java.util.List;

public class Example {
    public static void main(String[] args) {
        
    String userToken = "YOUR_SECRET_BGG_TOKEN";
    BggApi client = new BggApi(userToken);
    
    int boardGameId = 822; // Example: Carcassonne

    try {
            List<BoardGameExtended> games = client.getBoardGameInfo(boardGameId);
            if (!games.isEmpty()) {
                System.out.println("Found: " + games.get(0).getName().getValue());
                System.out.println("Min Players: " + games.get(0).getMinplayers());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### 2. Search for Games

Search BGG for titles matching a query.
```java
import com.omertron.bgg.model.SearchWrapper;

public class Example {
public static void main(String[] args) {
    
    String userToken = "YOUR_SECRET_BGG_TOKEN";
    BggApi client = new BggApi(userToken);
    
    try {
        SearchWrapper results = client.searchBoardGame("Catan", false, true); // query, exact match, include expansions
        System.out.println("Search Results (Total: " + results.getTotal() + "):");
        results.getItems().forEach(item -> System.out.println(" - " + item.getName() + " (" + item.getId() + ")")
        );
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
}
```
### 🌐 API Endpoints Implemented

This client provides wrappers for the following BGG XML API 2 endpoints:

    Thing: getBoardGameInfo(...)

    Family: getFamilyItems(...)

    User: getUserInfo(...)

    Collection: getCollectionInfo(...)

    Hot: getHotItems(...)

    Search: searchBoardGame(...)

    Plays: getPlays(...)

For detailed parameter options, refer to the official API documentation: https://boardgamegeek.com/wiki/page/bgg_xml_api2