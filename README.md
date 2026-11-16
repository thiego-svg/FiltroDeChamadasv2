# Filtro de Chamadas (Android) - Versão 2.0

Aplicativo moderno em Kotlin e Jetpack Compose para Android 10+ (API 29+) que rejeita automaticamente ligações de números desconhecidos (fora da sua agenda de contatos).

---

## Novidades da Versão 2.0

- 🌟 **Interface Glassmorphism & Frosty Blur**: Design refinado em vidro fosco com efeitos translúcidos e bordas luminosas.
- 🌌 **Fundo Aurora Animado**: Gradientes dinâmicos que se adaptam suavemente ao estado de proteção (Ativo / Desativado).
- ⚙️ **Painel de Configurações Lateral**: Gaveta deslizante (*Right-to-Left Drawer*) em vidro fosco acessível por botão de engrenagem flutuante na tela inicial.
- 🌍 **Suporte a 8 Idiomas**: Alterne instantaneamente entre:
  - 🇧🇷 Português do Brasil (`pt-BR`)
  - 🇺🇸 Inglês (EUA) (`en-US`)
  - 🇨🇳 Chinês (`zh-CN`)
  - 🇫🇷 Francês (`fr-FR`)
  - 🇩🇪 Alemão (`de-DE`)
  - 🇪🇸 Espanhol (`es-ES`)
  - 🇮🇹 Italiano (`it-IT`)
  - 🇬🇧 Inglês (Reino Unido) (`en-GB`)
- ⏱️ **Transições Rápidas**: Animações de 350 ms com atenuação natural (*FastOutSlowInEasing*).

---

## Primeiro Uso e Permissões

1. **Acesso aos contatos**: Permita o acesso para que o app saiba quem está na sua agenda.
2. **App de filtro de chamadas**: Ative a função padrão de triagem de chamadas do Android.
3. **Bateria e Notificações**: Em dispositivos modernos, garanta que o app esteja configurado como **Sem restrições de bateria**.

O app utiliza a API oficial `CallScreeningService`, funcionando de forma eficiente em segundo plano sem consumir bateria desnecessária.
