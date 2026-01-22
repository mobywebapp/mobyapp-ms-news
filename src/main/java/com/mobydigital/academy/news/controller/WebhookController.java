package com.mobydigital.academy.news.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mobydigital.academy.news.dto.NewsDto;
import com.mobydigital.academy.news.service.ContentfulService;
import com.mobydigital.academy.news.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/contentful/webhook")
@Slf4j
@AllArgsConstructor
public class WebhookController {

    private final ContentfulService contentfulService;
    private final NotificationService notificationService;

    @Operation(summary = "Endpoint de prueba para enviar un mensaje a Kafka.",
            description = "Este endpoint recibe un mensaje en el cuerpo de la solicitud y lo envía a un topic de Kafka.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje enviado exitosamente a Kafka."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/kafka")
    public ResponseEntity<String> mensajePrueba(@Parameter(description = "Mensaje de prueba para enviar a Kafka.") @RequestBody String mensaje){
        return new ResponseEntity<>(notificationService.sendKafka(mensaje), HttpStatus.OK);
    }

    @Operation(summary = "Maneja los webhooks de Contentful.",
            description = "Este endpoint recibe notificaciones de Contentful sobre cambios en el contenido, como publicaciones, despublicaciones y actualizaciones. Procesa estos eventos para invalidar la caché, notificar a otros servicios y mantener la consistencia de los datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook procesado exitosamente."),
            @ApiResponse(responseCode = "400", description = "Error en la solicitud del webhook, como un encabezado faltante."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor durante el procesamiento del webhook.")
    })
    @PostMapping
    public ResponseEntity<String> handleContentfulWebhook  (
            @Parameter(description = "El topic del webhook de Contentful, que indica el tipo de evento.") @RequestHeader("X-Contentful-Topic") String topic,
            @Parameter(description = "El payload del webhook, que contiene los datos del evento.") @RequestBody(required = false) Map<String, Object> payload) throws RuntimeException, JsonProcessingException {

        log.info("Webhook recibido. Topic={}", topic);
        log.info("Payload={}", payload);
        // 1) Siempre invalidamos cache si es Entry.*
        if (topic != null && topic.startsWith("ContentManagement.Entry.")) {
            contentfulService.evictNewsCache();
        }

        // 2) Intentamos extraer el entryId del payload (depende de la plantilla del webhook)
        String entryId = extractEntryId(payload);



        // 3) Actuar según el tipo de evento
        if ("ContentManagement.Entry.publish".equals(topic)) {
            log.info("-> PUBLICADA o REPUBLICADA.");
            if (entryId != null) {
                NewsDto dto = fetchWithRetry(entryId); // backoff contra la CDN
                if (dto != null) {
                    notificationService.notifyUpsert(dto, entryId);
                }
            }
        } else if ("ContentManagement.Entry.unpublish".equals(topic)) {
            log.info("-> DESPUBLICADA: Una novedad ha dejado de ser publicada.");
            if (entryId != null) {
                NewsDto dto = fetchWithRetry(entryId);
                notificationService.notifyRemoved(dto,entryId);
            }
        } else {
            log.info("-> Evento no manejado específicamente: {}", topic);
        }

        log.info("Webhook procesado con éxito.");
        return ResponseEntity.ok("Webhook recibido con éxito.");
    }


    private NewsDto fetchWithRetry(String entryId) throws RuntimeException {
        int attempts = 5;
        long delay = 300;
        for (int i = 0; i < attempts; i++) {
            Optional<NewsDto> dtoOpt = contentfulService.getNewsById(entryId);
            if (dtoOpt.isPresent()) return dtoOpt.get();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restaura el estado de interrupción
                log.warn("Thread interrumpido mientras dormía: {}", e.getMessage());
            }
            delay *= 2;
        }
        log.warn("CDA aún no refleja los cambios para {}", entryId);
        return null;
    }

    private String extractEntryId(Map<String, Object> payload) {
        if (payload == null) return null;

        // payload.sys.id
        if (payload.get("sys") instanceof Map<?, ?> sys
                && sys.get("id") instanceof String id) {
            return id;
        }

        // payload.entity.sys.id
        if (payload.get("entity") instanceof Map<?, ?> entity
                && entity.get("sys") instanceof Map<?, ?> sys
                && sys.get("id") instanceof String id) {
            return id;
        }

        return null;
    }
}