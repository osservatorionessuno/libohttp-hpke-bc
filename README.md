# libohttp-hpke-bc

BouncyCastle HPKE backend for [libohttp](https://github.com/osservatorionessuno/libohttp), implements its `HpkeBackend` interface.

```kotlin
import org.osservatorionessuno.libohttp.OhttpClient
import org.osservatorionessuno.libohttp.bouncycastle.BouncyCastleHpkeBackend

val client = OhttpClient.fromKeyConfig(keyConfigBytes, BouncyCastleHpkeBackend())
```
