/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.examples.inject;

import java.util.ArrayList;
import java.util.List;

import io.helidon.service.registry.Event;
import io.helidon.service.registry.Service;
import io.helidon.service.registry.Services;

/**
 * An example that illustrates usages of {@link Event}.
 */
class EventsExample {

    private EventsExample() {
    }

    public static void main(String[] args) {
        var myEmitter = Services.get(MyEmitter.class);
        var myObserver = Services.get(MyObserver.class);
        var myIdEmitter = Services.get(MyIdEmitter.class);
        var myIdObserver = Services.get(MyIdObserver.class);
        var myNameEmitter = Services.get(MyNameEmitter.class);
        var myNameObserver = Services.get(MyNameObserver.class);

        myEmitter.emit("foo");
        myEmitter.emit("bar");
        System.out.println(myObserver.messages);

        myIdEmitter.emit("123");
        myIdEmitter.emit("456");
        System.out.println(myIdObserver.ids);

        myNameEmitter.emit("Jack");
        myNameEmitter.emit("Jill");
        System.out.println(myNameObserver.names);
    }

    /**
     * A custom event payload.
     * @param msg message
     */
    record MyEvent(String msg) {
    }

    /**
     * A service that emits {@link MyEvent}.
     * @param emitter emitter
     */
    @Service.Singleton
    record MyEmitter(Event.Emitter<MyEvent> emitter) {

        void emit(String msg) {
            emitter.emit(new MyEvent(msg));
        }
    }

    /**
     * A service that observes {@link MyEvent}.
     */
    @Service.Singleton
    static class MyObserver {

        private final List<String> messages = new ArrayList<>();

        List<String> messages() {
            return messages;
        }

        @Event.Observer
        void event(MyEvent event) {
            messages.add(event.msg);
        }
    }

    /**
     * A service that emits string events named {@code id}.
     *
     * @param emitter emitter
     */
    @Service.Singleton
    record MyIdEmitter(@Service.Named("id") Event.Emitter<String> emitter) {

        void emit(String msg) {
            emitter.emit(msg);
        }
    }

    /**
     * A service that observes string events named {@code id}.
     */
    @Service.Singleton
    static class MyIdObserver {

        private final List<String> ids = new ArrayList<>();

        List<String> ids() {
            return ids;
        }

        @Event.Observer
        @Service.Named("id")
        void event(String id) {
            ids.add(id);
        }
    }

    /**
     * A service that emits string events named {@code name}.
     *
     * @param emitter emitter
     */
    @Service.Singleton
    record MyNameEmitter(@Service.Named("name") Event.Emitter<String> emitter) {

        void emit(String msg) {
            emitter.emit(msg);
        }
    }

    /**
     * A service that observes string events named {@code name}.
     */
    @Service.Singleton
    static class MyNameObserver {

        private final List<String> names = new ArrayList<>();

        List<String> names() {
            return names;
        }

        @Event.Observer
        @Service.Named("name")
        void event(String name) {
            names.add(name);
        }
    }
}
