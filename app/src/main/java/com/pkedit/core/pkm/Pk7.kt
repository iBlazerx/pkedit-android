package com.pkedit.core.pkm

import com.pkedit.core.util.readU16LE
import com.pkedit.core.util.readU32LE
import com.pkedit.core.util.readU8
import com.pkedit.core.util.writeU16LE
import com.pkedit.core.util.writeU32LE
import com.pkedit.core.util.writeU8

/**
 * Wrapper editable sobre los 232 (o 260) bytes desencriptados y deshuffleados de un PK7.
 *
 * Offsets principales (PKHeX): formato heredado pero con extras de Gen7.
 *  0x00 EncryptionConstant (u32)
 *  0x04 Sanity (u16)
 *  0x06 Checksum (u16)
 *  0x08 Species (u16)               -- Bloque A inicio
 *  0x0A HeldItem (u16)
 *  0x0C TID (u16)
 *  0x0E SID (u16)
 *  0x10 EXP (u32)
 *  0x14 Ability (u8)
 *  0x15 AbilityNumber (u8) bits 0-2
 *  0x16 TrainingBag/Hits (u8) - en SuMo/USUM Hyper Training flags están en 0xDE
 *  0x18 PID (u32)
 *  0x1C Nature (u8)
 *  0x1D Gender/Form (u8) bits: 0 fatefulEncounter, 1-2 gender, 3-7 form
 *  0x1E EV_HP (u8)                  -- bloque A sigue
 *  0x1F EV_ATK
 *  0x20 EV_DEF
 *  0x21 EV_SPE
 *  0x22 EV_SPA
 *  0x23 EV_SPD
 *  0x24 CNT_Cool .. 0x29 CNT_Sheen
 *  0x2A Markings (u8)
 *  0x2B PKRS (u8)
 *  ...
 *  0x40 Nickname (24 bytes UTF-16LE = 12 chars + null)
 *  0x5A Move1 (u16) .. 0x60 Move4 (u16)        -- Bloque B
 *  0x62 PP1 .. 0x65 PP4
 *  0x66 PPUps1..4
 *  0x6A Relearn1..4 (u16 cada uno)
 *  0x72 SuperTraining flags
 *  0x74 IV32 (u32): bits 0-4 HP, 5-9 ATK, 10-14 DEF, 15-19 SPE, 20-24 SPA, 25-29 SPD, 30 Egg, 31 Nicknamed
 *  0x78 OT Name (24 bytes UTF-16LE)            -- Bloque C
 *  ...
 *  0xCC Stats current (party only, en 0xF0+)
 *
 * Por simplicidad, este wrapper expone solo los campos relevantes para edición de randomlocke.
 */
class Pk7(val raw: ByteArray) {

    init {
        require(raw.size == Pk7Crypto.PK7_SIZE || raw.size == Pk7Crypto.PK7_PARTY_SIZE) {
            "PK7 inválido: ${raw.size} bytes"
        }
    }

    var encryptionConstant: Long
        get() = raw.readU32LE(0x00)
        set(v) = raw.writeU32LE(0x00, v)

    var species: Int
        get() = raw.readU16LE(0x08)
        set(v) = raw.writeU16LE(0x08, v)

    var heldItem: Int
        get() = raw.readU16LE(0x0A)
        set(v) = raw.writeU16LE(0x0A, v)

    var tid: Int
        get() = raw.readU16LE(0x0C)
        set(v) = raw.writeU16LE(0x0C, v)

    var sid: Int
        get() = raw.readU16LE(0x0E)
        set(v) = raw.writeU16LE(0x0E, v)

    var experience: Long
        get() = raw.readU32LE(0x10)
        set(v) = raw.writeU32LE(0x10, v)

    var ability: Int
        get() = raw.readU8(0x14)
        set(v) = raw.writeU8(0x14, v)

    /** bit 0: ability1, bit 1: ability2, bit 2: hidden */
    var abilityNumber: Int
        get() = raw.readU8(0x15) and 0x07
        set(v) {
            val cur = raw.readU8(0x15) and 0xF8
            raw.writeU8(0x15, cur or (v and 0x07))
        }

    var pid: Long
        get() = raw.readU32LE(0x18)
        set(v) = raw.writeU32LE(0x18, v)

    var nature: Int
        get() = raw.readU8(0x1C)
        set(v) = raw.writeU8(0x1C, v)

    /** Nature opcional usada por mecánicas como Synchronize en Gen7. */
    var statNature: Int
        get() = raw.readU8(0x1D)
        set(v) = raw.writeU8(0x1D, v)

    private val flagsByte: Int get() = raw.readU8(0x1E)
    private fun writeFlags(v: Int) = raw.writeU8(0x1E, v)

    var fatefulEncounter: Boolean
        get() = (flagsByte and 0x01) != 0
        set(v) {
            val cur = flagsByte and 0xFE
            writeFlags(cur or if (v) 1 else 0)
        }

    /** 0=M, 1=F, 2=Genderless */
    var gender: Int
        get() = (flagsByte shr 1) and 0x03
        set(v) {
            val cur = flagsByte and 0xF9
            writeFlags(cur or ((v and 0x03) shl 1))
        }

    var form: Int
        get() = (flagsByte shr 3) and 0x1F
        set(v) {
            val cur = flagsByte and 0x07
            writeFlags(cur or ((v and 0x1F) shl 3))
        }

    // EVs (0x1F..0x24)
    var evHp: Int  get() = raw.readU8(0x1F); set(v) = raw.writeU8(0x1F, v.coerceIn(0, 252))
    var evAtk: Int get() = raw.readU8(0x20); set(v) = raw.writeU8(0x20, v.coerceIn(0, 252))
    var evDef: Int get() = raw.readU8(0x21); set(v) = raw.writeU8(0x21, v.coerceIn(0, 252))
    var evSpe: Int get() = raw.readU8(0x22); set(v) = raw.writeU8(0x22, v.coerceIn(0, 252))
    var evSpa: Int get() = raw.readU8(0x23); set(v) = raw.writeU8(0x23, v.coerceIn(0, 252))
    var evSpd: Int get() = raw.readU8(0x24); set(v) = raw.writeU8(0x24, v.coerceIn(0, 252))

    // Movimientos (0x5A..0x61)
    fun getMove(slot: Int): Int = raw.readU16LE(0x5A + slot * 2)
    fun setMove(slot: Int, value: Int) = raw.writeU16LE(0x5A + slot * 2, value)

    fun getPP(slot: Int): Int = raw.readU8(0x62 + slot)
    fun setPP(slot: Int, value: Int) = raw.writeU8(0x62 + slot, value.coerceIn(0, 99))

    fun getPPUps(slot: Int): Int = raw.readU8(0x66 + slot)
    fun setPPUps(slot: Int, value: Int) = raw.writeU8(0x66 + slot, value.coerceIn(0, 3))

    // IVs empacados en u32 en 0x74
    private val ivPacked: Long get() = raw.readU32LE(0x74)
    private fun writeIvPacked(v: Long) = raw.writeU32LE(0x74, v)

    var ivHp: Int  get() = (ivPacked.toInt() shr 0) and 0x1F
                   set(v) = setIv(0, v)
    var ivAtk: Int get() = (ivPacked.toInt() shr 5) and 0x1F
                   set(v) = setIv(5, v)
    var ivDef: Int get() = (ivPacked.toInt() shr 10) and 0x1F
                   set(v) = setIv(10, v)
    var ivSpe: Int get() = (ivPacked.toInt() shr 15) and 0x1F
                   set(v) = setIv(15, v)
    var ivSpa: Int get() = (ivPacked.toInt() shr 20) and 0x1F
                   set(v) = setIv(20, v)
    var ivSpd: Int get() = (ivPacked.toInt() shr 25) and 0x1F
                   set(v) = setIv(25, v)

    private fun setIv(shift: Int, value: Int) {
        val v = value.coerceIn(0, 31).toLong()
        val mask = 0x1FL shl shift
        val packed = (ivPacked and mask.inv()) or (v shl shift)
        writeIvPacked(packed)
    }

    var isEgg: Boolean
        get() = ((ivPacked shr 30) and 1L) != 0L
        set(v) {
            val cur = ivPacked and (1L shl 30).inv()
            writeIvPacked(cur or (if (v) 1L shl 30 else 0L))
        }

    var isNicknamed: Boolean
        get() = ((ivPacked shr 31) and 1L) != 0L
        set(v) {
            val cur = ivPacked and (1L shl 31).inv()
            writeIvPacked(cur or (if (v) 1L shl 31 else 0L))
        }

    // Nombre y OT en UTF-16LE (12 chars + null)
    var nickname: String
        get() = readUtf16(0x40, 12)
        set(v) = writeUtf16(0x40, 12, v)

    var otName: String
        get() = readUtf16(0x78, 12)
        set(v) = writeUtf16(0x78, 12, v)

    /** Nivel actual (party only). En PC se calcula desde EXP. */
    fun calcLevelFromExp(growthRate: Int): Int {
        return ExpTable.levelAtExperience(growthRate, experience)
    }

    /** Fija EXP exacto necesario para alcanzar `level` según la growth-rate. */
    fun setLevel(level: Int, growthRate: Int) {
        val clamped = level.coerceIn(1, 100)
        experience = ExpTable.experienceForLevel(growthRate, clamped)
    }

    var isShiny: Boolean
        get() {
            val pid = pid
            val tid = tid.toLong()
            val sid = sid.toLong()
            val xor = ((pid ushr 16) xor (pid and 0xFFFF) xor tid xor sid).toInt() and 0xFFFF
            return xor < 16
        }
        set(v) {
            if (v == isShiny) return
            if (v) makeShiny() else makeNonShiny()
        }

    private fun makeShiny() {
        // Forzamos el PID para que xor con TID/SID dé < 16 manteniendo nature y gender.
        val tid = tid
        val sid = sid
        val low = (pid and 0xFFFF).toInt()
        val newHigh = (low xor tid xor sid) and 0xFFFF
        pid = ((newHigh.toLong() shl 16) or low.toLong())
    }

    private fun makeNonShiny() {
        val high = ((pid ushr 16) and 0xFFFF).toInt()
        // Ajustar high para que xor con low/tid/sid sea >= 16
        val low = (pid and 0xFFFF).toInt()
        var newHigh = high xor 0x8000
        if (((newHigh xor low xor tid xor sid) and 0xFFFF) < 16) {
            newHigh = newHigh xor 0xFFFF
        }
        pid = ((newHigh.toLong() shl 16) or low.toLong())
    }

    private fun readUtf16(offset: Int, maxChars: Int): String {
        val sb = StringBuilder()
        for (i in 0 until maxChars) {
            val ch = raw.readU16LE(offset + i * 2)
            if (ch == 0) break
            sb.append(ch.toChar())
        }
        return sb.toString()
    }

    private fun writeUtf16(offset: Int, maxChars: Int, value: String) {
        for (i in 0 until maxChars) {
            val ch = if (i < value.length) value[i].code else 0
            raw.writeU16LE(offset + i * 2, ch)
        }
        // Null terminator garantizado en la última posición
        raw.writeU16LE(offset + (maxChars) * 2, 0)
    }

    /** Devuelve copia limpia de 232 bytes (sin party stats). */
    fun toStorageBytes(): ByteArray {
        val out = ByteArray(Pk7Crypto.PK7_SIZE)
        System.arraycopy(raw, 0, out, 0, Pk7Crypto.PK7_SIZE)
        return out
    }
}
