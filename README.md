# Scala-Shodan

A Shodan API client for Scala. 

This project is still under construction.



## Usage
Creating a client instance: 
```scala 
import com.kylegoodale.shodan.ShodanClient
 
val shodan = new ShodanClient("<Your API Key>")
```

### Searching 
You can make queries using the provided query builder or write them manually as strings

Example: Finding devices in the State of Maine with HTTP servers that returned an error when they were last crawled by Shodan.
```scala
import com.kylegoodale.shodan.Query
 
val query = new Query()
    .withCountry("US")
    .withState("ME")
    .withHTTP.status.INTERNAL_SERVER_ERROR
    .result
    
shodan.hostSearch(query).onComplete {
    case Success(searchResults) => 
      println(s"Found ${searchResults.total} matches. Showing 100")
      println(searchResults.matches.mkString(","))
    case Failure(ex) => println("Request failed for reason: ", ex.getMessage)
}
```

Example: Fetching info on a certain host

```scala
shodan.hostInfo("192.168.1.1").onComplete {
    case Success(hostInfo) => 
      println(hostInfo)
    case Failure(ex) => println("Request failed for reason: ", ex.getMessage)
}
```

###TODO
- More doc examples
- Exploit searching
- Streams
- Facet Builder
- Clientside rate limiting?
 
 


