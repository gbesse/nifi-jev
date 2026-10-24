package io.github.gbesse.nifi.jev;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class EndpointIdentityTest {
 @Test void rejectsAmbiguousIdentitiesWithoutEchoingInput(){
  for(String raw:new String[]{"https:///route","https://user:PRIVATE@example.org/api","https://example.org/api#PRIVATE","https://example.org:0/api","https://example.org:99999/api","http://example.org/api","https://[PRIVATE"}){
   var e=assertThrows(IllegalArgumentException.class,()->RouteWithJev.checkedEndpoint(raw));assertFalse(e.getMessage().contains("PRIVATE"));assertNull(e.getCause());assertTrue(e.getMessage().contains("Invalid HTTP endpoint identity"));
  }
 }
 @Test void keepsHttpsAndLoopbackWithPathAndQuery(){for(String raw:new String[]{"https://example.org:443/api?q=1","http://127.0.0.1:1234/api","http://localhost/api"})assertEquals(raw,RouteWithJev.checkedEndpoint(raw).toString());}
}
