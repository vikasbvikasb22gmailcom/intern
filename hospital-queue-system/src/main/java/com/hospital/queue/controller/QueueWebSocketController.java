package com.hospital.queue.controller;

import com.hospital.queue.dto.AppointmentDto;
import com.hospital.queue.service.AppointmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Tag(name = "WebSocket", description = "Real-time queue updates via STOMP WebSocket")
public class QueueWebSocketController {

    private final AppointmentService appointmentService;

    /**
     * Client sends to: /app/queue/{doctorId}
     * Server broadcasts to: /topic/queue/{doctorId}
     *
     * Connect with SockJS/STOMP:
     *   var socket = new SockJS('/ws');
     *   var stompClient = Stomp.over(socket);
     *   stompClient.connect({}, function() {
     *     stompClient.subscribe('/topic/queue/1', function(msg) {
     *       console.log(JSON.parse(msg.body));
     *     });
     *   });
     */
    @MessageMapping("/queue/{doctorId}")
    @SendTo("/topic/queue/{doctorId}")
    public List<AppointmentDto.QueueStatusResponse> getQueueUpdate(
            @DestinationVariable Long doctorId) {
        return appointmentService.getLiveDoctorQueue(doctorId);
    }
}
