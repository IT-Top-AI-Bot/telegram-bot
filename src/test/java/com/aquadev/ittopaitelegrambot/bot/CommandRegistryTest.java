package com.aquadev.ittopaitelegrambot.bot;

import com.aquadev.ittopaitelegrambot.bot.annotation.TelegramBotCommand;
import com.aquadev.ittopaitelegrambot.bot.handler.CommandHandler;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandRegistryTest {

    @TelegramBotCommand(value = "/foo", description = "Foo cmd")
    static class FooHandler implements CommandHandler {
        @Override
        public void handle(Update update) {
        }
    }

    @TelegramBotCommand(value = "/bar", description = "Bar cmd")
    static class BarHandler implements CommandHandler {
        @Override
        public void handle(Update update) {
        }
    }

    static class NoAnnotationHandler implements CommandHandler {
        @Override
        public void handle(Update update) {
        }
    }

    @TelegramBotCommand(value = "/dup", description = "Dup")
    static class DupHandler1 implements CommandHandler {
        @Override
        public void handle(Update update) {
        }
    }

    @TelegramBotCommand(value = "/dup", description = "Dup2")
    static class DupHandler2 implements CommandHandler {
        @Override
        public void handle(Update update) {
        }
    }

    @Test
    void find_returnsCorrectHandler() {
        var foo = new FooHandler();
        var bar = new BarHandler();
        var registry = new CommandRegistry(List.of(foo, bar));
        registry.build();

        assertThat(registry.find("/foo")).isSameAs(foo);
        assertThat(registry.find("/bar")).isSameAs(bar);
    }

    @Test
    void find_returnsNull_forUnknownCommand() {
        var registry = new CommandRegistry(List.of(new FooHandler()));
        registry.build();

        assertThat(registry.find("/unknown")).isNull();
    }

    @Test
    void find_returnsNull_beforeBuild() {
        var registry = new CommandRegistry(List.of(new FooHandler()));

        assertThat(registry.find("/foo")).isNull();
    }

    @Test
    void getCommandMetadata_returnsAnnotationsInOrder() {
        var foo = new FooHandler();
        var bar = new BarHandler();
        var registry = new CommandRegistry(List.of(foo, bar));
        registry.build();

        var metadata = registry.getCommandMetadata();
        assertThat(metadata).hasSize(2);
        assertThat(metadata.get(0).value()).isEqualTo("/foo");
        assertThat(metadata.get(1).value()).isEqualTo("/bar");
    }

    @Test
    void build_skipHandlersWithoutAnnotation() {
        var registry = new CommandRegistry(List.of(new NoAnnotationHandler()));
        registry.build();

        assertThat(registry.getCommandMetadata()).isEmpty();
    }

    @Test
    void build_throwsOnDuplicateCommand() {
        var registry = new CommandRegistry(List.of(new DupHandler1(), new DupHandler2()));

        assertThatThrownBy(registry::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("/dup");
    }
}
