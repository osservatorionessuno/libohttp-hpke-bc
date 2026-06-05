package org.osservatorionessuno.libohttp.bouncycastle

import org.bouncycastle.crypto.hpke.HPKE
import org.osservatorionessuno.libohttp.HpkeBackend
import org.osservatorionessuno.libohttp.HpkeSender

// HpkeBackend on BouncyCastle's HPKE (RFC 9180)
class BouncyCastleHpkeBackend : HpkeBackend {
    override fun setupBaseSender(
        kemId: Int,
        kdfId: Int,
        aeadId: Int,
        recipientPublicKey: ByteArray,
        info: ByteArray,
    ): HpkeSender {
        val hpke = HPKE(HPKE.mode_base, kemId.toShort(), kdfId.toShort(), aeadId.toShort())
        val ctx = hpke.setupBaseS(hpke.deserializePublicKey(recipientPublicKey), info)
        return object : HpkeSender {
            override val encapsulation: ByteArray = ctx.encapsulation

            override fun seal(
                aad: ByteArray,
                plaintext: ByteArray,
            ) = ctx.seal(aad, plaintext)

            override fun export(
                exporterContext: ByteArray,
                length: Int,
            ) = ctx.export(exporterContext, length)
        }
    }
}
