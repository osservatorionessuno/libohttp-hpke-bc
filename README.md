# libohttp-hpke-bc

BouncyCastle HPKE backend for [libohttp](https://github.com/osservatorionessuno/libohttp), implements its `HpkeBackend` interface.

```kotlin
import org.osservatorionessuno.libohttp.OhttpClient
import org.osservatorionessuno.libohttp.bouncycastle.BouncyCastleHpkeBackend

val client = OhttpClient.fromKeyConfig(keyConfigBytes, BouncyCastleHpkeBackend())
```

## BouncyCastle provider

BouncyCastle is a `compileOnly` dependency: this library does not pull a BouncyCastle provider into
your runtime classpath, so it never imposes a particular variant on you. **You must provide one** —
e.g. `org.bouncycastle:bcprov-jdk18on` (JVM) or `org.bouncycastle:bcprov-jdk15to18` (Android). This
lets you keep a single BouncyCastle variant and avoids duplicate-class build failures when your app
already ships one.
