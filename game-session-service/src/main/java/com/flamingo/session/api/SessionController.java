package com.flamingo.session.api;

import com.flamingo.session.api.dto.CreateSessionResponse;
import com.flamingo.session.api.dto.SessionDetailsResponse;
import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.service.SessionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<CreateSessionResponse> createSession() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @PostMapping("/{sessionId}/simulate")
    public ResponseEntity<Void> startSimulation(@PathVariable String sessionId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping("/{sessionId}")
    public SessionDetailsResponse getSession(@PathVariable String sessionId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @GetMapping(value = "/{sessionId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SimulationEvent>> streamEvents(@PathVariable String sessionId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
