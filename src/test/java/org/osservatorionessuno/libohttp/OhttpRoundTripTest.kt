package org.osservatorionessuno.libohttp

import org.bouncycastle.crypto.hpke.HPKE
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.osservatorionessuno.libohttp.bouncycastle.BouncyCastleHpkeBackend
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// Full round trip for the mandatory suite, RFC 9458 §8.2
class OhttpRoundTripTest {
    @Test
    fun `request and response round trip`() {
        val hpke = HPKE(HPKE.mode_base, KEM_X25519_HKDF_SHA256.toShort(), KDF_HKDF_SHA256.toShort(), AEAD_AES_128_GCM.toShort())
        val gatewayKeyPair = hpke.generatePrivateKey()
        val publicKey = hpke.serializePublicKey(gatewayKeyPair.public)

        // Encode a key config §3
        val config =
            byteArrayOf(0x2A) + u16(KEM_X25519_HKDF_SHA256) + publicKey +
                u16(4) + u16(KDF_HKDF_SHA256) + u16(AEAD_AES_128_GCM)
        val client = OhttpClient.fromKeyConfig(config, BouncyCastleHpkeBackend())

        val request = "the inner bhttp request".toByteArray()
        val encapsulated = client.encapsulateRequest(request)

        // Gateway decapsulates §4.3
        val msg = encapsulated.bytes
        val hdr = msg.copyOfRange(0, 7)
        val enc = msg.copyOfRange(7, 7 + hpke.encSize)
        val ct = msg.copyOfRange(7 + hpke.encSize, msg.size)
        val info = "message/bhttp request".toByteArray(Charsets.US_ASCII) + 0x00.toByte() + hdr
        val recipient = hpke.setupBaseR(enc, gatewayKeyPair, info)
        assertArrayEquals(request, recipient.open(ByteArray(0), ct))

        // Gateway encapsulates §4.4
        val response = "the inner bhttp response".toByteArray()
        val nk = 16
        val nn = 12
        val secret = recipient.export("message/bhttp response".toByteArray(Charsets.US_ASCII), nk)
        val responseNonce = ByteArray(maxOf(nn, nk)).also { SecureRandom().nextBytes(it) }
        val prk = hkdfExtract(enc + responseNonce, secret)
        val encResponse =
            responseNonce + aesGcmSeal(hkdfExpand(prk, "key".toByteArray(), nk), hkdfExpand(prk, "nonce".toByteArray(), nn), response)

        assertArrayEquals(response, encapsulated.decapsulateResponse(encResponse)) // client decapsulates (§4.5)
    }

    private fun u16(v: Int) = byteArrayOf((v ushr 8).toByte(), v.toByte())

    private fun hkdfExtract(
        salt: ByteArray,
        ikm: ByteArray,
    ) = Mac.getInstance("HmacSHA256").run {
        init(SecretKeySpec(salt, "HmacSHA256"))
        doFinal(ikm)
    }

    private fun hkdfExpand(
        prk: ByteArray,
        info: ByteArray,
        length: Int,
    ): ByteArray {
        val mac = Mac.getInstance("HmacSHA256").apply { init(SecretKeySpec(prk, "HmacSHA256")) }
        val out = ByteArray(length)
        var t = ByteArray(0)
        var filled = 0
        var counter = 1
        while (filled < length) {
            mac.update(t)
            mac.update(info)
            mac.update(counter.toByte())
            t = mac.doFinal()
            val take = minOf(t.size, length - filled)
            t.copyInto(out, filled, 0, take)
            filled += take
            counter++
        }
        return out
    }

    private fun aesGcmSeal(
        key: ByteArray,
        nonce: ByteArray,
        pt: ByteArray,
    ) = Cipher.getInstance("AES/GCM/NoPadding").run {
        init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
        doFinal(pt)
    }
}
