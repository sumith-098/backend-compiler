// package sumith.hinglish;

// import org.springframework.web.bind.annotation.*;

// @CrossOrigin(origins="*")
// @RestController
// public class InterpreterController {

//     HinglishInterpreter interpreter = new HinglishInterpreter();

//     @PostMapping("/run")
//     public String run(@RequestBody String code) {
//         return interpreter.runProgram(code);
//     }
// }
package sumith.hinglish;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@CrossOrigin(origins = "*")
@RestController
public class InterpreterController {

    /*
     * How it works:
     *
     * 1. Frontend calls GET /run-interactive?code=...
     * 2. Backend starts an SSE stream
     * 3. Interpreter runs line by line:
     *    - When it prints  → sends  event: output  data: <text>
     *    - When it hits lo → sends  event: input   data: <varName>
     *                        then BLOCKS waiting for frontend to POST /input/<sessionId>
     * 4. Frontend receives events:
     *    - output  → prints to terminal
     *    - input   → shows inline prompt, user types and presses Enter
     *    - done    → session complete
     * 5. User submits input → POST /input/{id}  body=value
     *    Backend unblocks, stores value, continues execution
     */

    // Active sessions: id → blocking queue that holds the user's typed value
    private static final Map<String, BlockingQueue<String>> sessions = new ConcurrentHashMap<>();

    @GetMapping("/initialize")
    public String hello(){
        return "Backend Initialized";
    }
    
    /* ── Start interactive run ── */
    @GetMapping(value = "/run-interactive", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter runInteractive(@RequestParam String code) {

        // Long timeout — user might take time to type
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        String sessionId = UUID.randomUUID().toString();

        BlockingQueue<String> inputQueue = new LinkedTransferQueue<>();
        sessions.put(sessionId, inputQueue);

        // First event: give frontend the session id
        Thread.ofVirtual().start(() -> {
            try {
                // Send session id first
                emitter.send(SseEmitter.event().name("session").data(sessionId));

                // Custom output stream that sends SSE events per line
                InteractiveOutput out = new InteractiveOutput(emitter, sessionId);

                // Custom input supplier that blocks until frontend posts a value
                InteractiveInput in = new InteractiveInput(inputQueue, emitter, sessionId);

                // Run the program
                HinglishInterpreter.runInteractive(code, out, in);

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("Galti: " + e.getMessage()));
                    emitter.complete();
                } catch (IOException ignored) {}
            } finally {
                sessions.remove(sessionId);
            }
        });

        return emitter;
    }

    /* ── Receive user input ── */
    @PostMapping("/input/{sessionId}")
    public String receiveInput(@PathVariable String sessionId, @RequestBody String value) {
        BlockingQueue<String> queue = sessions.get(sessionId);
        if (queue == null) return "session not found";
        queue.offer(value.trim());
        return "ok";
    }

    /* ── Output handler: sends SSE output events ── */
    static class InteractiveOutput {
        private final SseEmitter emitter;
        private final String     sessionId;
        private final StringBuilder lineBuffer = new StringBuilder();

        InteractiveOutput(SseEmitter e, String sid) { emitter = e; sessionId = sid; }

        void print(String text) {
            // Buffer and send line by line
            lineBuffer.append(text);
            flushLines(false);
        }

        void println(String text) {
            lineBuffer.append(text).append('\n');
            flushLines(false);
        }

        void flush() { flushLines(true); }

        private void flushLines(boolean force) {
            String buf = lineBuffer.toString();
            int idx;
            while ((idx = buf.indexOf('\n')) >= 0) {
                String line = buf.substring(0, idx);
                buf = buf.substring(idx + 1);
                sendOutput(line);
            }
            lineBuffer.setLength(0);
            if (force && !buf.isEmpty()) {
                sendOutput(buf);
            } else {
                lineBuffer.append(buf);
            }
        }

        private void sendOutput(String line) {
            try {
                emitter.send(SseEmitter.event().name("output").data(line));
            } catch (IOException e) {
                throw new RuntimeException("Stream closed");
            }
        }
    }

    /* ── Input handler: blocks until user types ── */
    static class InteractiveInput {
        private final BlockingQueue<String> queue;
        private final SseEmitter            emitter;
        private final String                sessionId;

        InteractiveInput(BlockingQueue<String> q, SseEmitter e, String sid) {
            queue = q; emitter = e; sessionId = sid;
        }

        String readLine(String varName) {
            try {
                // Tell frontend: waiting for input for this variable
                emitter.send(SseEmitter.event().name("input_needed").data(varName));
                // Block until frontend posts the value (up to 2 min)
                String val = queue.poll(2, TimeUnit.MINUTES);
                if (val == null) throw new RuntimeException("Input timeout");
                return val;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Input interrupted");
            }
        }
    }
}