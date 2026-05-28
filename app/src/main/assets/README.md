# Catálogo de datos

Los JSON aquí son listas mínimas con los IDs correctos pero solo unos
pocos nombres rellenos. Para ampliarlos:

## Opción 1: editar a mano

Abre `species_gen7.json` con cualquier editor y cambia los `"#152"`,
`"#153"`, etc. por nombres reales. Los IDs van por número del Pokédex Nacional
(Bulbasaur=1, ... Marshadow=802, Zeraora=807).

## Opción 2: usar fuente externa

PokéAPI tiene la lista completa en JSON. Un script Python para descargarlos:

```python
import requests, json
species = []
for i in range(1, 808):
    r = requests.get(f"https://pokeapi.co/api/v2/pokemon-species/{i}").json()
    name_es = next(n["name"] for n in r["names"] if n["language"]["name"] == "es")
    species.append({"id": i, "name": name_es})
# añadir índice 0 vacío al principio
species.insert(0, {"id": 0, "name": "—"})
```

Lo mismo aplica a `items_gen7.json`, `moves_gen7.json`, `abilities_gen7.json`.

## Recompilar tras editar

Es solo cambiar el JSON y volver a compilar (push a GitHub → Actions →
descargar el APK nuevo). La app lee los JSON al abrir, no hay nada que cachear.
