package io.github.gbesse.nifi.jev;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.Test;

class RouteReceiptTest {
  private static final Map<String, String> STALE = Map.of(
      "jev.probability", "0.99", "jev.model", "old-model", "jev.state.sha256", "old-state",
      "jev.reason", "budget", "jev.error", "remote_failure", "jev.route", "yes",
      "jev.custom", "caller-owned", "customer.id", "synthetic-customer");

  private TestRunner runner(HttpServer server, int maxCalls) {
    TestRunner runner = TestRunners.newTestRunner(RouteWithJev.class);
    runner.setProperty(RouteWithJev.API_KEY, "synthetic-key");
    runner.setProperty(RouteWithJev.ENDPOINT,
        "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/systemone");
    runner.setProperty(RouteWithJev.MODEL, "jev-1.13.0");
    runner.setProperty(RouteWithJev.CONDITION, "Synthetic yes/no condition");
    runner.setProperty(RouteWithJev.CONFIDENCE, "0.8");
    runner.setProperty(RouteWithJev.MAX_BYTES, "1000");
    runner.setProperty(RouteWithJev.TIMEOUT, "3000");
    runner.setProperty(RouteWithJev.MAX_CALLS, Integer.toString(maxCalls));
    return runner;
  }

  private HttpServer server(String reply, int status, AtomicInteger calls) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.createContext("/v1/systemone", exchange -> {
      calls.incrementAndGet();
      byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(status, bytes.length);
      try (var output = exchange.getResponseBody()) { output.write(bytes); }
    });
    server.start();
    return server;
  }

  @Test
  void successfulAndUncertainRoutesReplacePreviousReceipts() throws Exception {
    for (double probability : new double[] {0.92, 0.1, 0.5}) {
      AtomicInteger calls = new AtomicInteger();
      String reply = "{\"answers\":{\"decision\":{\"type\":\"noul\",\"noul\":" + probability + "}}}";
      HttpServer server = server(reply, 200, calls);
      try {
        TestRunner runner = runner(server, 10);
        runner.enqueue("synthetic content".getBytes(StandardCharsets.UTF_8), STALE);
        runner.run(1);
        Relationship route = probability > 0.8 ? RouteWithJev.YES : probability < 0.2 ? RouteWithJev.NO : RouteWithJev.REVIEW;
        runner.assertTransferCount(route, 1);
        var file = runner.getFlowFilesForRelationship(route).get(0);
        assertEquals(route.getName(), file.getAttribute("jev.route"));
        assertEquals(Double.toString(probability), file.getAttribute("jev.probability"));
        assertEquals("jev-1.13.0", file.getAttribute("jev.model"));
        assertTrue(file.getAttribute("jev.state.sha256").matches("[a-f0-9]{64}"));
        assertNull(file.getAttribute("jev.error"));
        assertEquals(route == RouteWithJev.REVIEW ? "uncertain" : null, file.getAttribute("jev.reason"));
        assertEquals("caller-owned", file.getAttribute("jev.custom"));
        assertEquals(1, calls.get());
      } finally { server.stop(0); }
    }
  }

  @Test
  void emptyAndOversizedReviewNeverRetainOldProbabilities() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    HttpServer server = server("{}", 200, calls);
    try {
      TestRunner runner = runner(server, 10);
      runner.enqueue(new byte[0], STALE);
      runner.enqueue(new byte[1001], STALE);
      runner.run(2);
      runner.assertTransferCount(RouteWithJev.REVIEW, 2);
      var files = runner.getFlowFilesForRelationship(RouteWithJev.REVIEW);
      assertEquals("empty", files.get(0).getAttribute("jev.reason"));
      assertEquals("oversized", files.get(1).getAttribute("jev.reason"));
      for (var file : files) {
        assertEquals("review", file.getAttribute("jev.route"));
        assertNull(file.getAttribute("jev.probability"));
        assertNull(file.getAttribute("jev.model"));
        assertNull(file.getAttribute("jev.state.sha256"));
        assertNull(file.getAttribute("jev.error"));
        assertEquals("synthetic-customer", file.getAttribute("customer.id"));
      }
      assertEquals(0, calls.get());
    } finally { server.stop(0); }
  }

  @Test
  void exhaustedBudgetAndRemoteFailureCannotLookLikeAnOldYes() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    HttpServer server = server("{}", 503, calls);
    try {
      TestRunner runner = runner(server, 1);
      runner.enqueue("first".getBytes(StandardCharsets.UTF_8), STALE);
      runner.enqueue("second".getBytes(StandardCharsets.UTF_8), STALE);
      runner.run(2);
      runner.assertTransferCount(RouteWithJev.FAILURE, 1);
      runner.assertTransferCount(RouteWithJev.REVIEW, 1);
      var failed = runner.getFlowFilesForRelationship(RouteWithJev.FAILURE).get(0);
      assertEquals("failure", failed.getAttribute("jev.route"));
      assertEquals("remote_failure", failed.getAttribute("jev.error"));
      assertNull(failed.getAttribute("jev.reason"));
      assertNull(failed.getAttribute("jev.probability"));
      var budget = runner.getFlowFilesForRelationship(RouteWithJev.REVIEW).get(0);
      assertEquals("review", budget.getAttribute("jev.route"));
      assertEquals("budget", budget.getAttribute("jev.reason"));
      assertNull(budget.getAttribute("jev.error"));
      assertNull(budget.getAttribute("jev.probability"));
      assertEquals(1, calls.get());
    } finally { server.stop(0); }
  }
  @Test
  void invalidUtf8GoesToReviewWithoutCallingProvider() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    HttpServer server = server("{}", 200, calls);
    try {
      TestRunner runner = runner(server, 10);
      runner.enqueue(new byte[] {(byte) 0xc3, 0x28}, STALE);
      runner.run(1);
      runner.assertTransferCount(RouteWithJev.REVIEW, 1);
      var file = runner.getFlowFilesForRelationship(RouteWithJev.REVIEW).get(0);
      assertEquals("invalid_utf8", file.getAttribute("jev.reason"));
      assertNull(file.getAttribute("jev.probability"));
      assertEquals(0, calls.get());
    } finally { server.stop(0); }
  }

  @Test
  void oversizedResponseFailsWithoutRetainingOldVerdict() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    HttpServer server = server("x".repeat(100001), 200, calls);
    try {
      TestRunner runner = runner(server, 10);
      runner.enqueue("synthetic text".getBytes(StandardCharsets.UTF_8), STALE);
      runner.run(1);
      runner.assertTransferCount(RouteWithJev.FAILURE, 1);
      var file = runner.getFlowFilesForRelationship(RouteWithJev.FAILURE).get(0);
      assertEquals("remote_failure", file.getAttribute("jev.error"));
      assertNull(file.getAttribute("jev.probability"));
      assertEquals(1, calls.get());
    } finally { server.stop(0); }
  }
}
