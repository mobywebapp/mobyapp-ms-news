package com.mobydigital.academy.news.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mobydigital.academy.news.dto.NewsDto;
import com.mobydigital.academy.news.service.ContentfulService;
import com.mobydigital.academy.news.service.NotificationService;
import com.contentful.java.cma.model.CMASnapshot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/contentful/webhook")
public class WebhookController {

    /*

    Webhook -> te avisa q paso algo, observa si hay un cambio y tira un aviso
    Websockets -> manda mensajes (tiempo real, se usa para chats o en este caso actualizar)

     */
    private final ContentfulService contentfulService;
    private final NotificationService notificationService;
    private static final Logger logger = Logger.getLogger(WebhookController.class.getName());

    @Autowired
    public WebhookController(ContentfulService contentfulService, NotificationService notificationService) {
        this.contentfulService = contentfulService;
        this.notificationService = notificationService;
    }

    @PostMapping("/kafka")
    public ResponseEntity<String> mensajePrueba(@RequestBody String mensaje) throws JsonProcessingException{
        return new ResponseEntity<>(notificationService.sendKafka(mensaje), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> handleContentfulWebhook  (
            @RequestHeader("X-Contentful-Topic") String topic,
            @RequestBody(required = false) Map<String, Object> payload) throws RuntimeException, JsonProcessingException {

        logger.info("Webhook recibido. Topic=" + topic);
        logger.info("Payload=" + payload);
        // 1) Siempre invalidamos cache si es Entry.*
        if (topic != null && topic.startsWith("ContentManagement.Entry.")) {
            contentfulService.evictNewsCache();
        }

        // 2) Intentamos extraer el entryId del payload (depende de la plantilla del webhook)
        String entryId = extractEntryId(payload);

        //// Para seguir viendo <-
        /*
        CMASnapshot prev = contentfulMgmt.getPreviousPublishedSnapshot(entryId);
        */


        // 3) Actuar según el tipo de evento
        if ("ContentManagement.Entry.publish".equals(topic)) {
            logger.info("-> PUBLICADA o REPUBLICADA.");
            if (entryId != null) {
                NewsDto dto = fetchWithRetry(entryId); // backoff contra la CDN
                if (dto != null) {
                    notificationService.notifyUpsert(dto, entryId);
                }
            }
        } else if ("ContentManagement.Entry.unpublish".equals(topic)) {
            logger.info("-> DESPUBLICADA: Una novedad ha dejado de ser publicada.");
            if (entryId != null) {
                NewsDto dto = fetchWithRetry(entryId);
                notificationService.notifyRemoved(dto,entryId);
            }
        } else {
            logger.info("-> Evento no manejado específicamente: " + topic);
        }

        logger.info("Webhook procesado con éxito.");
        return ResponseEntity.ok("Webhook recibido con éxito.");
    }


    private NewsDto fetchWithRetry(String entryId) throws RuntimeException {
        int attempts = 5;
        long delay = 300;
        for (int i = 0; i < attempts; i++) {
            Optional<NewsDto> dtoOpt = contentfulService.getNewsById(entryId);
            if (dtoOpt.isPresent()) return dtoOpt.get();
            try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
            delay *= 2;
        }
        logger.warning("CDA aún no refleja los cambios para " + entryId);
        return null;
    }

    private String extractEntryId(Map<String, Object> payload) throws RuntimeException {
        if (payload == null) return null;
            // Muchos webhooks de Contentful traen payload.sys.id
            Map<String, Object> sys = (Map<String, Object>) payload.get("sys");
            if (sys != null && sys.get("id") instanceof String) {
                return (String) sys.get("id");
            }
            // Alternativa: payload.entity.sys.id o payload.payload.sys.id, según config
            Map<String, Object> entity = (Map<String, Object>) payload.get("entity");
            if (entity != null) {
                Map<String, Object> esys = (Map<String, Object>) entity.get("sys");
                if (esys != null && esys.get("id") instanceof String) {
                    return (String) esys.get("id");
                }
            }

        return null;
    }
}