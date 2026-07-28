# Multi-User-Chat-Server-Process-and-Threads-of-the-Operating-System-
A chat server accessible by any number of clients over the network, with messages broadcast to everyone connected — built with raw Java TCP sockets.



## What it does

- Any number of clients can connect to the server (not limited to two).
- Each client picks a username on connecting.
- Every message a client sends is broadcast to **all other connected clients**.
- Join/leave events are announced to everyone (`"Alice has joined the chat."`).

This corresponds to the course topic **Process and Threads of the
Operating System**.

## Threads vs. Processes — the core concept

Each connected client is handled by the server on its own **thread**, not
a separate **process**. This distinction matters:

| | Threads (used here) | Processes |
|---|---|---|
| Memory | Share the same memory space | Isolated, separate memory |
| Communication | Direct — can access shared objects (like the `clients` list) | Requires explicit IPC (pipes, sockets, shared memory) |
| Creation cost | Lightweight, fast to create | Heavier, slower to create |
| Crash isolation | One thread crashing can affect the whole process | One process crashing doesn't affect others |

Because all client-handling threads run inside the **same process** and
share memory, broadcasting is simple: every thread can directly read and
write to the same shared `clients` list. If this were built using separate
*processes* instead (e.g. the classic Unix `fork()`-per-client model), the
server would need an explicit inter-process communication mechanism just
to let one client's handler notify another's — there's no shared memory
by default between processes.

The OS scheduler is what actually decides which thread runs on the CPU at
any given moment — from the programmer's side, we just create threads and
let the OS handle scheduling them fairly across all connected clients.

## How to run

**1. Compile:**
```bash
javac ChatServer.java ChatClient.java
```

**2. Start the server:**
```bash
java ChatServer
```

**3. Start as many clients as you want**, each in its own terminal:
```bash
java ChatClient
```
Enter a username when prompted, then start chatting.

### Example (3 clients: Alice, Bob, Charlie)

Alice's terminal:
```
Bob has joined the chat.
Charlie has joined the chat.
You: Hi everyone!
Bob: Hey Alice!
Charlie: Hello all
```

Server console:
```
Alice joined from /127.0.0.1:35524
Bob joined from /127.0.0.1:35502
Charlie joined from /127.0.0.1:35514
Alice: Hi everyone!
Bob: Hey Alice!
Charlie: Hello all
```

## Key concepts demonstrated

- **Thread-per-client model, scaled**: unlike the previous 2-user project
  (fixed at exactly two clients), this server accepts an unbounded number
  of connections in a loop, spawning a new thread for each.
- **Shared mutable state across threads**: `CopyOnWriteArrayList` is used
  for the client list specifically because it's safe to read (broadcast)
  from many threads while occasionally being written to (client
  joins/leaves) from others — this is a real concurrency-safety decision,
  not an arbitrary choice.
- **Broadcast pattern**: one message triggers writes to multiple sockets,
  demonstrating one-to-many communication over independent connections.
- **Graceful cleanup**: when a client disconnects, its handler thread
  removes it from the shared list and notifies everyone else — showing
  proper resource and state cleanup in a multi-threaded environment.

## Tech stack

- Java 21+ (no external dependencies)

## Where this fits in the larger project

This is the actual **group chat engine** for the unified app — it's a
direct extension of the previous 2-user chat's thread-per-client pattern,
generalized to support any number of simultaneous users. The next projects
(file transfer, file sharing) will build on this same connection-handling
foundation to add media/file capabilities to the chat.
