# re-serve

This library aims to bring some of the functionality [re-frame](https://github.com/Day8/re-frame) provides on the client side to the **server** side.
#### What is *re-frame*?
[re-frame](https://github.com/Day8/re-frame) is a functional framework for writing [SPAs](https://en.wikipedia.org/wiki/Single-page_application) in ClojureScript, using [Reganet](http://reagent-project.github.io).
#### What is *re-serve*?
*re-serve* is functional framework for writing **server side** applications with long-living connections to the clients. 
It re-uses the internals of *re-frame* and runs on **Clojure** instead of ClojureScript. The API is kept as close to *re-frame*'s API as possible.
## Why should you try it?
- If you are familiar with *re-frame* you will find it very easy to organize your code on the server side in a very similar way as on the client side.
- Don't worry if you don't know *re-frame* yet. It is always a good time to start with the [re-frame docs](https://github.com/Day8/re-frame#derived-values-flowing). It is awesome!
- You will have the advantages you gain using *re-frame* on client side for example: 
    - Encourage the use of Pure Functions.
    - Easy testability.
    - Clean, verifyable state transitions.
- You'll get some additional features as well:
    - An additional session layer is added on top of *re-frame*'s event handling to provide a way to track and handle different events from clients separately.
    - Since the core of the state machine is reimplemented with `core.async`, you can handle any message in a *core.async* channel as an event.

## Examples
You can find examples in the [examples](https://github.com/kotyo/re-serve/tree/master/examples) directory under the code.

## Usage
Add 
`[re-frame "0.1.0"]`
to your dependencies.

Require `[re-frame.core :as re-frame]` where it is needed.

## API (re-frame.core)
### Session handling
#####`(start-session & {:keys [teardown-event]})`
Start and return a *re-serve* session with an empty db. It takes an optional `teardown-event` which dispatched as the last event after the termination of the session.
#####`(stop-session session)`
Terminate the *re-serve* session.

### Dispatching
#####`(dispatch session event)`
Dispatche the `event` vector on the gives `session`.
#####`(dispatch-sync session event)`
The same as the previous dispatch call, but it waits until the `event` is enqueued for processing.

### Effects
#####`(reg-fx id handler)`
Register an effect `handler` for the given `id`.
#####`(clear-fx id)`
Clear an effect registration.
#####`(reg-fx-session id handler)`
Register an effect `handler` for the given `id`. The effect handlers got the *session-context* as the first parameter and need to return with the updated *session-context*. Only for experimental usage.

#### Additional built-in effects
#####`:attach {:keys [chan dispatch]}`
Attache a `core.async` channel to the session. That means *dispatch* event will be dispatched when there is a message available on the *chan*. 
#####`:detach chan`
Detache the channel from the session.
#####`:stop-session _`
Stop the session.

### Co-Effects
#####`(reg-cofx id handler)`
Register a new coeffects.
#####`(inject-cofx id) (inject-cofx id value)`
Create an interceptor from the previously registered coeffect.
#####`(clear-cofx id)`
Clear a coeffect registration.

### Events
#####`(reg-event-db id handler) (reg-event-db id interceptors handler)`
Register the given event *handler* function for the given event *id*.
*handler* is a function: (db event) -> db.
#####`(reg-event-fx id handler) (reg-event-fx id interceptors handler)`
Register the given event *handler* function for the given event *id*.
*handler* is a function: (coeffects-map event-vector) -> effects-map.
#####`(reg-event-ctx id handler) (reg-event-ctx id interceptors handler)`
Register the given event *handler* function for the given event *id*.
*handler* is a function: (context-map event-vector) -> context-map.

## License

Copyright © 2019 

done by kotyo

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
