package com.mobydigital.academy.news.service;

import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cma.CMAClient;
import com.contentful.java.cma.model.CMAEntry;
import com.mobydigital.academy.news.dto.Audience;
import com.mobydigital.academy.news.dto.NewsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@Slf4j
public class ContentfulService {

    private final CDAClient client;     //  Es para hacer operaciones ONLY READ en Contentful (CDA)
    private final CMAClient cmaClient;  // Es para hacer operaciones de gestión en Cntentful (Read-Write-Delete-Update) (CMA)

    @Value("${contentful.spaceId}")
    private String spaceId;
    private static final String ENVIRONMENT_ID  = "master";
    private static final String EXPIRATION_DATE_FIELD = "expiration_date";
    private static final String CONTENT_TYPE_NEWS = "news";
    private static final String CONTENT_TYPE_FIJAS= "novedadesFijas";

    // Comparación de fechas y prioridad
    private static final Comparator<NewsDto> PRIORITY_ORDER =
            Comparator
                    // 1) Prioridad primero: true (o mayor) antes que false.
                    .comparing(NewsDto::getPriority, Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed()
                    // 2) Luego por fecha de expiración más cercana (ascendente). Los null al final.
                    .thenComparing(NewsDto::getExpirationDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    // 3) Si NO tiene expiración (es null), ordenar por creación más reciente primero.
                    .thenComparing(dto -> dto.getExpirationDate() == null ? dto.getCreatedAt() : null,
                            Comparator.nullsLast(Comparator.reverseOrder()));

    // Para expulsar cuando hay mas de 8 novedades: expira antes primero y, si empatan, creada más antigua
    private static final Comparator<NewsDto> DROP_RULE =
            Comparator.comparing(NewsDto::getExpirationDate,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(NewsDto::getCreatedAt).reversed();

    private static final DateTimeFormatter FLEX_OFFSET =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd'T'HH:mm")
                    .optionalStart().appendLiteral(':').appendPattern("ss").optionalEnd()
                    .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
                    .appendOffset("+HH:MM", "Z")
                    .toFormatter();

    @Autowired
    public ContentfulService(CDAClient client, CMAClient cmaClient) {
        this.client = client;
        this.cmaClient = cmaClient;
    }

    private NewsDto mapEntryToDto(CDAEntry entry) {
        String title = entry.getField("title");
        Boolean active = entry.getField("is_active");
        CDAAsset imageAsset = entry.getField("image");
        String expirationDateString = entry.getField(EXPIRATION_DATE_FIELD);
        Boolean isMobyWeb = entry.getField("isMobyWeb");
        Boolean isMobyApp = entry.getField("isMobyApp");
        String description = entry.getField("description");
        String url = entry.getField("url");

        Boolean priority = entry.getField("priority");
        String createdDateString = entry.getAttribute("createdAt");

        ZoneId zoneAR = ZoneId.of("America/Argentina/Buenos_Aires"); //Para cambiar al huso argentino

        // Parsear el string original
        OffsetDateTime createdOffset = OffsetDateTime.parse(createdDateString, FLEX_OFFSET);

        // Convertir a horario argentino
        ZonedDateTime createdInAR = createdOffset.atZoneSameInstant(zoneAR);

        // Mostrar sin el [America/Argentina/Buenos_Aires]
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");
        String createdParse = createdInAR.format(formatter);
        ZonedDateTime createdAt = ZonedDateTime.parse(createdParse, formatter);

        ZonedDateTime expirationDate = null;

        if (expirationDateString != null) {
            expirationDate = OffsetDateTime.parse(expirationDateString, FLEX_OFFSET).toZonedDateTime();

        }
        String imageUrl = (imageAsset != null) ? imageAsset.url() : null;
        String entryId = entry.id();

        return new NewsDto(
                entryId,
                title,
                active != null && active,
                imageUrl,
                description,
                isMobyWeb,
                isMobyApp,
                url,
                expirationDate,
                createdAt,
                priority != null && priority
        );
    }

    @Cacheable(value = {"newsFinal","news"}, key = "#audience") // cache separada por canal
    public List<NewsDto> buildFinalNews(Audience audience) {
        // 1) Traer y ordenar NEWS por prioridad
        List<NewsDto> news = new ArrayList<>(fetchNewsActiveNotExpired(audience));
        news.sort(PRIORITY_ORDER);

        // 2) Recortar a 8 con tu regla de “drop”
        while (news.size() > 8) {
            NewsDto toRemove = news.stream().min(DROP_RULE).orElse(null);
            if (toRemove == null) break;
            news.remove(toRemove);
        }

        // 3) Completar con extras del mismo canal, sin repetir
        if (news.size() < 8) {
            Set<String> used = news.stream().map(NewsDto::getId).collect(Collectors.toSet());
            List<NewsDto> extras = fetchAllExtras(audience);

            List<NewsDto> pool = extras.stream()
                    .filter(e -> !used.contains(e.getId()))
                    .collect(Collectors.toList());

            Collections.shuffle(pool);
            int toAdd = Math.min(pool.size(), 8 - news.size());
            news.addAll(pool.subList(0, toAdd));
        }

        if (news.size() > 8) news = news.subList(0, 8);
        return news;
    }

    private List<NewsDto> fetchNewsActiveNotExpired(Audience audience) {
        CDAArray arr = client.fetch(CDAEntry.class)
                .withContentType(CONTENT_TYPE_NEWS)
                .all();

        return arr.entries().values().stream()
                .map(CDAEntry.class::cast)
                .map(this::mapEntryToDto)
                .filter(NewsDto::getActive)                                 // solo activas
                .filter(n -> n.getExpirationDate() == null
                        || n.getExpirationDate().isAfter(ZonedDateTime.now(ZoneOffset.UTC)))
                .filter(n -> audience == Audience.MOBY_APP
                        ? Boolean.TRUE.equals(n.getIsMobyApp())
                        : Boolean.TRUE.equals(n.getIsMobyWeb()))            // FILTRO CLAVE
                .toList();
    }

    private List<NewsDto> fetchAllExtras(Audience audience) {
        CDAArray arr = client.fetch(CDAEntry.class)
                .withContentType(CONTENT_TYPE_FIJAS)
                .limit(8)
                .all();

        List<NewsDto> list = new ArrayList<>(arr.entries().values().stream()
                .map(this::mapEntryToDto)
                .filter(n -> audience == Audience.MOBY_APP
                        ? Boolean.TRUE.equals(n.getIsMobyApp())
                        : Boolean.TRUE.equals(n.getIsMobyWeb()))            // MISMO FILTRO EN EXTRAS
                .toList());

        Collections.shuffle(list);
        return list;
    }

    // Busca la novedad por su ID
    public Optional<NewsDto> getNewsById(String entryId) {
        try {
            CDAEntry entry = client.fetch(CDAEntry.class).one(entryId);
            if (entry == null) return Optional.empty();
            return Optional.of(mapEntryToDto(entry));
        } catch (Exception e) {
            log.warn("No se pudo obtener la entrada CDA id={}: {}", entryId, e.getMessage());
            return Optional.empty();
        }
    }

    // Limpia la caché para mantener las novedades actualizadas
    @CacheEvict(value = {"news","newsFinal"}, allEntries = true)
    public void evictNewsCache() {
        log.info("Cache de novedades invalidada.");
    }

    // Elimina las novedades expiradas, se ejecuta cada un minunto para pruebas
    @Scheduled(cron = "0 */1 * * * ?")
    public void deleteExpiredNews() {
        final ZonedDateTime nowUtc = ZonedDateTime.now(ZoneId.of("UTC"));
        final String isoDate = nowUtc.format(FLEX_OFFSET);
        log.info("STARTING CLEANUP: Buscando noticias caducadas a " + isoDate);
        try {
            Map<String, String> queryFilters = new HashMap<>();
            queryFilters.put("fields." + EXPIRATION_DATE_FIELD + "[lte]", isoDate);
            queryFilters.put("content_type", CONTENT_TYPE_NEWS);

            List<CMAEntry> expiredEntries = cmaClient.entries()
                    .fetchAll(spaceId, ENVIRONMENT_ID, queryFilters)
                    .getItems();

            if (expiredEntries.isEmpty()) {
                log.info("CLEANUP SUCCESS: No se encontraron noticias caducadas.");
                return;
            }

            for (CMAEntry entry : expiredEntries) {
                processExpiredEntry(entry);
            }
            evictNewsCache();
        } catch (Exception e) {
            log.warn("FATAL CLEANUP ERROR: No se pudo consultar CMA. Error: {}", e.getMessage());
        }
    }

    public void processExpiredEntry(CMAEntry entry) {
                try {
                    String entryId = entry.getId();
                    String title = (String) entry.getField("title", "en-US"); // ajustar locale si corresponde
                    log.info("PROCESSING: Eliminando noticia caducada: {} ({})", title, entryId);

                    if (Boolean.TRUE.equals(entry.isPublished())) {
                        log.info("  -> Despublicando entrada...");
                        cmaClient.entries().unPublish(entry);
                    }
                    log.info("  -> Eliminando entrada...");
                    cmaClient.entries().delete(entry);

                    log.info("SUCCESS: Noticia eliminada: {}", title);

                } catch (Exception e) {
                    log.warn("ERROR PROCESSING ENTRY: {}: {}", entry.getId(), e.getMessage());
                }
    }
}