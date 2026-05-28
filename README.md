# PkEdit — Editor de saves Pokémon USUM para Android

Editor para archivos `main` de **Pokémon Ultra Sun / Ultra Moon**.
Inspirado en [PKHeX](https://github.com/kwsch/PKHeX) (no afiliado, bajo GPL).

## Estado actual (v0.4 — beta)

✅ **Verificado contra save real:**
- Detección USUM (445440 bytes)
- Descifrado completo de los 6 Pokémon del equipo
- Lectura/edición de: especie, apodo, nivel, naturaleza, habilidad, objeto,
  IVs, EVs, 4 movimientos, shiny toggle
- **Exportar el save funciona**: CRCs de los 39 bloques recalculados con
  el algoritmo CRC16Invert de PKHeX. Verificado que exportar sin cambios
  produce un archivo byte-a-byte idéntico al original.

⚠️ **Experimental:**
- Lectura de mochila (los IDs son correctos pero el bit-packing exacto
  de cantidades puede variar entre revisiones de USUM). Editar y guardar
  funciona; los CRC se recalcularán correctamente, pero el juego podría
  interpretar mal cantidades extremas.

❌ **No implementado:**
- Edición de Pokémon de las cajas del PC
- Validación de legalidad
- Soporte para Sun/Moon (solo USUM)

## Compilar

### GitHub Actions
Push a `main` → pestaña Actions → último run → Artifacts → `pkedit-debug-apk`.

### Android Studio
File → Open → carpeta del proyecto → ▶️ Run.

## Cómo usar

1. Extrae el `main` de tu USUM con **Checkpoint** o **JKSM** en una 3DS con CFW.
2. Pásalo al móvil.
3. **Haz copia de seguridad** del `main` antes de tocar nada.
4. Abre PkEdit → Abrir save → elige tu `main`.
5. Edita en Equipo o Mochila.
6. Exportar → guarda como nuevo `main`.
7. Devuelve el `main` editado a la SD y restáuralo con Checkpoint/JKSM.

## Estructura del save USUM (descubierta y verificada)

```
0x00000  MyItem (mochila)           0xE28 bytes
0x01000  Situation                  0x7C
0x01200  RandomGroup                0x14
0x01400  MyStatus                   0xC0
0x01600  Party ← 6 PK7 (260b cada)  0x61C
0x01E00  EventWork                  0xE00
0x02C00  Pokédex                    0xF78
...
0x05200  PC Boxes (32×30 PK7)       0x36600
...
0x6CA00  Metadata header (BEEF)     0x18
0x6CA18  Tabla de checksums        0x1E8 (39 entries × 8 bytes)
```

Cada PK7 = 232 bytes (260 en party) cifrado con PRNG cuyo seed es el
EncryptionConstant (primeros 4 bytes). 4 bloques de 56 bytes A/B/C/D
barajados según `(EC >> 13) & 0x1F`.

Cada entry de checksum: `u16 id, u16 crc, u32 length`.
Algoritmo CRC: **CRC16Invert** con tabla precalculada (poly 0xA001, init
0xFFFF, XOR final por inversión de bits). Portado de
`PKHeX.Core/Saves/Util/Checksums.cs`.

## Licencia

Pendiente. Si distribuyes, mantén bajo GPL-3.0 (PKHeX está bajo GPL).
