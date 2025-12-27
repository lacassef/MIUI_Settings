# MIUI Settings

MIUI Settings e um app Android para abrir configuracoes ocultas/avancadas do MIUI/HyperOS usando intents e um catalogo de destinos. Ele mostra compatibilidade do dispositivo, permite busca e filtros por categoria, e sincroniza um catalogo remoto.

## Recursos
- Lista de configuracoes com busca, chips por categoria e resumo de compatibilidade.
- Abre telas via intents (acao ou activity), com fallback por prioridade.
- Catalogo local com seed + sincronizacao remota opcional.
- Regras de compatibilidade por SDK, MIUI/HyperOS e fabricante (Xiaomi/Redmi/Poco).
- Persistencia local com Room e injecao de dependencia com Hilt.

## Requisitos
- Android 8+ (minSdk 26).
- Dispositivo Xiaomi/Redmi/Poco para compatibilidade completa.
- Android Studio e JDK 11.

## Como rodar
- Android Studio: abrir o projeto e executar o modulo `app`.
- Linha de comando:
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:installDebug`

## Catalogo de configuracoes
- Seed local: `app/src/main/java/com/recodex/miuisettings/data/local/SeedCatalogProvider.kt`
- Catalogo remoto: `catalog/catalog.json` (URL definida em `app/build.gradle.kts` via `REMOTE_CATALOG_URL`).

Campos principais:
- `version`: string de versao.
- `settings[]`: lista de configuracoes.
- `settings[].targets[]`: destinos com `packageName` + `className` ou apenas `action`.
- `settings[].targets[].extras`: (opcional) objeto com extras para o Intent (valores em string).
- `minSdkVersion`, `maxSdkVersion`, `requiredMiuiVersion`, `isLegacyOnly` controlam compatibilidade.
- `signature`: opcional; pode ser exigida via `REMOTE_CATALOG_SIGNATURE_REQUIRED` e `REMOTE_CATALOG_SIGNATURE_SALT`.

Exemplo:
```json
{
  "version": "2025.12.25",
  "settings": [
    {
      "id": "private_dns",
      "title": "DNS privado",
      "category": "Rede",
      "minSdkVersion": 28,
      "searchKeywords": ["dns", "doh"],
      "targets": [
        {
          "id": "private_dns_action",
          "action": "android.settings.PRIVATE_DNS_SETTINGS",
          "priority": 100,
          "requiresExported": false
        }
      ]
    }
  ]
}
```

## Sincronizacao
- No app, use o menu "Sync" para buscar o catalogo remoto e atualizar o banco local.

## Estrutura
- `app/src/main/java/com/recodex/miuisettings/presentation`: UI + ViewModels.
- `app/src/main/java/com/recodex/miuisettings/domain`: modelos e use cases.
- `app/src/main/java/com/recodex/miuisettings/data`: Room, repositorio e fontes local/remota.
- `catalog/catalog.json`: catalogo publico de configuracoes.

## Observacoes
- Nem todas as configuracoes estao disponiveis em todas as ROMs/dispositivos.
- O app nao e afiliado a Xiaomi; a disponibilidade depende da ROM (MIUI/HyperOS) e do Android.
