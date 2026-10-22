package io.github.gbesse.nifi.jev;
import static org.junit.jupiter.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.Test;
class JevEnvelopeTest {
 @Test void ambiguousOrMisroutedEnvelopesNeverProduceProbability() throws Exception {
  for(String body:new String[]{"{\"model\":\"different\",\"answers\":{\"decision\":{\"type\":\"noul\",\"noul\":0.99}}}",
"{\"answers\":{\"decision\":{\"type\":\"noul\",\"noul\":0.99}}}",
"{\"model\":\"jev-1.13.0\",\"answers\":{\"decision\":{\"type\":\"noul\",\"noul\":0.99},\"extra\":{\"type\":\"noul\",\"noul\":0.99}}}",
"{\"model\":\"jev-1.13.0\",\"model\":\"different\",\"answers\":{\"decision\":{\"type\":\"noul\",\"noul\":0.99}}}",
"{\"model\":\"jev-1.13.0\",\"answers\":{\"decision\":{\"type\":\"noul\",\"noul\":0.99}}} {}"}){
   HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
   server.createContext("/v1/systemone",exchange->{byte[] bytes=body.getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(200,bytes.length);try(var out=exchange.getResponseBody()){out.write(bytes);}});server.start();
   try{
    TestRunner runner=TestRunners.newTestRunner(RouteWithJev.class);
    runner.setProperty(RouteWithJev.API_KEY,"synthetic");runner.setProperty(RouteWithJev.ENDPOINT,"http://127.0.0.1:"+server.getAddress().getPort()+"/v1/systemone");
    runner.setProperty(RouteWithJev.MODEL,"jev-1.13.0");runner.setProperty(RouteWithJev.CONDITION,"Synthetic question");runner.setProperty(RouteWithJev.CONFIDENCE,"0.8");runner.setProperty(RouteWithJev.MAX_BYTES,"1000");runner.setProperty(RouteWithJev.TIMEOUT,"3000");runner.setProperty(RouteWithJev.MAX_CALLS,"10");
    runner.enqueue("synthetic text".getBytes(StandardCharsets.UTF_8));runner.run(1);runner.assertTransferCount(RouteWithJev.FAILURE,1);
    var file=runner.getFlowFilesForRelationship(RouteWithJev.FAILURE).get(0);assertNull(file.getAttribute("jev.probability"));assertNull(file.getAttribute("jev.model"));
   }finally{server.stop(0);}
  }
 }
}
