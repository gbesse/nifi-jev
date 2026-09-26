package io.github.gbesse.nifi.jev;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.Test;

class RouteWithJevTest {
  @Test
  void routesHighAndUncertainProbabilities() throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v1/systemone", exchange -> {
      byte[] reply = "{\"answers\":{\"decision\":{\"type\":\"noul\",\"noul\":0.92}}}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, reply.length);
      try (var out = exchange.getResponseBody()) { out.write(reply); }
    });
    server.start();
    try {
      TestRunner runner = TestRunners.newTestRunner(RouteWithJev.class);
      runner.setProperty(RouteWithJev.API_KEY, "test-key");
      runner.setProperty(RouteWithJev.ENDPOINT, "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/systemone");
      runner.setProperty(RouteWithJev.MODEL, "jev-1.13.0");
      runner.setProperty(RouteWithJev.CONDITION, "Does this review recommend the film?");
      runner.setProperty(RouteWithJev.CONFIDENCE, "0.8");
      runner.setProperty(RouteWithJev.MAX_BYTES, "1000");
      runner.setProperty(RouteWithJev.TIMEOUT, "3000");
      runner.setProperty(RouteWithJev.MAX_CALLS, "10");
      runner.enqueue("Highly recommended".getBytes(StandardCharsets.UTF_8));
      runner.run(1);
      runner.assertTransferCount(RouteWithJev.YES, 1);
      assertEquals("0.92", runner.getFlowFilesForRelationship(RouteWithJev.YES).get(0).getAttribute("jev.probability"));
    } finally { server.stop(0); }
  }
}
