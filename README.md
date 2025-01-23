## Usage

### Data

> ⚠️ Don't forget to close the connections to the databases after using it.

#### In-Memory (Redis)

:TheGreenSuits#getJedisPool: `JedisPool` - Returns the JedisPool object to connect to the Redis database.

```java
JedisPool jedisPool = TheGreenSuits.getJedisPool();
```

#### Database

:TheGreenSuits#getRemoteClient: `RemoteClient` - Returns the RemoteClient object to interact with the database.
`RemoteClient` is a class that contains the methods to interact with the database through the `TheGreenSuits` API.

```java
RemoteClient remoteClient = TheGreenSuits.getRemoteClient();
```
