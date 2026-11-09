# Serpente Neon

Jogo arcade em Canvas, empacotado como aplicativo Android nativo com WebView.

## Arquitetura

- Jogo: HTML5 Canvas/JavaScript, empacotado dentro do APK.
- Android: WebView fullscreen em modo paisagem.
- Backend: Supabase.
- Ranking: `public.game_scores`, leitura pública protegida por RLS.
- Escrita: Edge Function `submit-score`; nenhuma chave secreta é distribuída no APK.
- CI: GitHub Actions executa lint, compila, valida o APK e publica APK + ZIP como artefatos.

## Build Android

O workflow `.github/workflows/android-apk.yml` usa JDK 17, Android SDK 37, Android Gradle Plugin 9.4.1 e Gradle 9.6.0.

Build local (com SDK Android configurado):

```bash
gradle :app:lintDebug :app:assembleDebug
```

APK gerado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Supabase

As alterações de schema estão em `supabase/migrations/` e a Edge Function em `supabase/functions/submit-score/`.

A publishable key pode ficar no cliente. Chaves secretas/service-role nunca devem ser colocadas no app ou no repositório.


Runtime validation: Android emulator boot + game start is enforced by CI.
