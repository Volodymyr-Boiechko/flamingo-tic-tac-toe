package com.flamingo.session.api;

import com.flamingo.session.api.dto.CreateSessionResponse;
import com.flamingo.session.api.dto.SessionDetailsResponse;
import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.service.GameSimulator;
import com.flamingo.session.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final GameSimulator simulator;

    public SessionController(SessionService sessionService, GameSimulator simulator) {
        this.sessionService = sessionService;
        this.simulator = simulator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreateSessionResponse> createSession() {
        return sessionService.createSession()
                .map(CreateSessionResponse::from);
    }

    @PostMapping("/{sessionId}/simulate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> startSimulation(@PathVariable String sessionId) {
        return sessionService.startSimulation(sessionId);
    }

    @GetMapping("/{sessionId}")
    public Mono<SessionDetailsResponse> getSession(@PathVariable String sessionId) {
        return sessionService.getSession(sessionId)
                .map(SessionDetailsResponse::from);
    }

    @GetMapping(value = "/{sessionId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SimulationEvent>> streamEvents(@PathVariable String sessionId) {
        return simulator.eventStream(sessionId)
                .map(event -> ServerSentEvent.builder(event).build());
    }
}
