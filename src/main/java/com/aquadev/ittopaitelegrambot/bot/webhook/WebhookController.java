package com.aquadev.ittopaitelegrambot.bot.webhook;

//@Slf4j
//@RestController
//@Profile("kubernetes")
//@RegisterReflectionForBinding(Update.class)
//public class WebhookController {
//
//    private final UpdateDispatcher updateDispatcher;
//    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
//
//    private final ObjectMapper telegramObjectMapper = new ObjectMapper()
//            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//            .registerModule(new JavaTimeModule());
//
//    public WebhookController(UpdateDispatcher updateDispatcher) {
//        this.updateDispatcher = updateDispatcher;
//    }
//
//    @RequestMapping(value = "/callback/bot", method = RequestMethod.HEAD)
//    public ResponseEntity<Void> head() {
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping(value = "/callback/bot", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Void> onUpdate(@RequestBody String body) {
//        try {
//            Update update = telegramObjectMapper.readValue(body, Update.class);
//            log.info("Received update id={}", update.getUpdateId());
//            executor.submit(() -> {
//                try {
//                    updateDispatcher.dispatch(update);
//                } catch (Throwable e) {
//                    log.error("Error dispatching update", e);
//                }
//            });
//        } catch (Exception e) {
//            log.error("Failed to deserialize update: {}", body, e);
//        }
//        return ResponseEntity.ok().build();
//    }
//}
