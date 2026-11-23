# Arena dos Monstrinhos

Jogo de batalhas por turnos com criaturas originais, inspirado no gênero de jogos de captura e batalha de monstrinhos.

## Requisitos da atividade atendidos

- Variáveis para controlar treinador, personagem escolhido, turno, pontos de vida e estado da batalha.
- Condicionais `if/else` para validar escolhas, calcular vitória/derrota, controlar vida e exibir estados diferentes.
- Estrutura `for` para percorrer os personagens carregados do JSON, renderizar ataques e consultar o histórico.
- Estrutura `while` no laço principal da batalha: a luta continua enquanto jogador e adversário tiverem pontos de vida.
- Interação com o usuário por nome do treinador, escolha de criatura e escolha de ataque a cada turno.
- Dezesseis criaturas ilustradas disponíveis, cada uma com nome, tipo, vida, força de ataque e dois golpes diferentes.
- Histórico de vitórias e derrotas.

## JSON

### `monstrinhos.json`

Arquivo responsável pelos personagens e atributos. O JavaScript carrega os dados com `fetch()` e `response.json()`.

Cada criatura possui:
- `id`
- `nome`
- `tipo`
- `simbolo`
- `vida`
- `ataque`
- `descricao`
- `ataques` com dois golpes

Há uma cópia para a versão web na raiz e outra em:

`app/src/main/assets/monstrinhos.json`

### `historico.json`

O histórico é representado pelo objeto:

```json
{
  "batalhas": []
}
```

Ao terminar uma batalha, o programa cria um registro com data, treinador, monstrinho do jogador, adversário, resultado, turnos e vida restante.

No Android, a ponte `HistoricoAndroid` salva o JSON em um arquivo real chamado `historico.json` no armazenamento interno do aplicativo. No navegador, o fallback usa `localStorage` e o botão **Exportar historico.json** permite gerar o arquivo.

## Estrutura principal da batalha

O laço é assíncrono para permitir que o usuário escolha um ataque a cada turno:

```js
while (
  batalha.jogador.hp > 0 &&
  batalha.adversario.hp > 0
) {
  const ataque = await esperarAtaqueDoJogador();
  // jogador ataca
  // adversário ataca
}
```

## Android

- WebView fullscreen.
- Assets servidos com `WebViewAssetLoader`.
- `minSdk 21`.
- Versão atual: `2.1.0`.
- O pipeline do GitHub instala o APK em emulador Android e valida a abertura do jogo.

## Observação

O projeto utiliza criaturas, nomes e elementos visuais originais. A mecânica é inspirada no gênero de batalhas por turnos popularizado por jogos como Pokémon, sem copiar personagens ou artes oficiais.


## Criaturas ilustradas

O jogo utiliza um sprite sheet próprio em `monster-sprites.svg` com 16 criaturas originais:

- Flamora — Fogo
- Aquavelo — Água
- Voltrix — Elétrico
- Verdantis — Natureza
- Glacilume — Gelo
- Terrabull — Terra
- Noctivy — Sombra
- Zephling — Vento
- Pyronix — Fogo
- Tiderra — Água
- Sparkoon — Elétrico
- Floracorn — Natureza
- Frostuff — Gelo
- Boulderox — Terra
- Umbrawl — Sombra
- Galeaf — Vento

Cada criatura possui dois golpes próprios. Alguns golpes têm efeitos especiais como cura, drenagem de vida ou chance aumentada de acerto crítico. O sistema também considera vantagens entre tipos.
