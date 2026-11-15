# Filtro de Chamadas (Android)

Rejeita automaticamente ligações de números que não estão nos seus contatos.
Kotlin + Jetpack Compose, Android 10 ou mais novo (API 29+).

## Como gerar o .apk

**Opção 1 – GitHub (não instala nada no PC)**
1. Crie um repositório no GitHub e envie esta pasta inteira (inclusive `.github`).
2. Abra a aba **Actions**, rode **Gerar APK** (ou espere o push terminar).
3. Ao fim, baixe o artefato **FiltroDeChamadas-apk** (dentro dele está o `app-debug.apk`).

**Opção 2 – Android Studio**
1. **File > Open** e escolha a pasta `FiltroDeChamadas` (a que tem o `gradlew`). Espere o *Sync*.
2. Se o Studio oferecer atualizar o Android Gradle Plugin, pode recusar (o projeto já está configurado).
3. **Build > Build APK(s)**. Quando terminar, clique em *locate* no aviso:
   o arquivo é `app/build/outputs/apk/debug/app-debug.apk`.

Se o Sync reclamar de JDK, vá em **Settings > Build, Execution, Deployment > Build Tools > Gradle**
e escolha *Gradle JDK* = JDK 24 (ou 17 a 23; o "jbr" que vem com o Studio também serve).
Esta versão usa Gradle 8.14.3, plugin do Android 8.10.1 e Kotlin 2.1.21, que rodam no JDK 24.
O JDK 25 **não** funciona com este Gradle.
Não envie ao GitHub o arquivo `gradle/gradle-daemon-jvm.properties` (já está no `.gitignore`):
ele quebra o build do GitHub Actions.

**Opção 3 – Terminal** (com JDK 17 a 24 e Android SDK instalados)
`gradle assembleDebug` e pegue `app/build/outputs/apk/debug/app-debug.apk`.

Para instalar, copie o .apk para o celular e permita "instalar apps desconhecidos".

## Primeiro uso

1. **Acesso aos contatos** – toque em *Permitir*.
2. **App de filtro de chamadas** – toque em *Ativar* e confirme no aviso do Android.
3. (Android 13+) permita os avisos e, nas configurações de bateria, deixe o app **Sem restrições**.

Depois disso o Android chama o app em toda ligação recebida, mesmo após reiniciar,
sem precisar de serviço rodando em segundo plano.

## O que acontece com um desconhecido

A ligação é **rejeitada** na hora, como se você tivesse tocado em recusar. O telefone não toca.
Se quem ligou ouve ocupado ou cai na caixa postal depende da operadora.
Teste com um número que não está na sua agenda.

## Limitações

- Logo após reiniciar, **antes do primeiro desbloqueio** (PIN/biometria), os contatos
  ainda estão criptografados e o app não consegue filtrar. As ligações tocam normalmente.
  Depois de desbloquear, a filtragem passa a valer.
- Em caso de erro (sem permissão, contatos indisponíveis) a ligação passa em vez de ser
  barrada, para nunca bloquear um contato seu por engano.
- Chamadas por VoIP/SIP não são filtradas, e ligações de emergência nunca passam pelo filtro.
- Alguns fabricantes (Xiaomi, Samsung, Huawei...) matam apps em segundo plano. Se o filtro
  falhar, libere a bateria e o início automático do app.
- Esta versão é um APK de teste (assinado com a chave de debug) e foi escrita sem
  poder rodar num aparelho. Se o build der erro, mande a mensagem que eu corrijo.
