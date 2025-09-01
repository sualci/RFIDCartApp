# Android App – Smart Shopping Cart

Este repositorio inclúe a aplicación Android que funciona como interface de usuario para o sistema de carro intelixente con tecnoloxía RFID/NFC.

## 📌 Versionado

- **Tag [`v1.0-nfc`](https://github.com/sualci/RFIDCartApp/releases/tag/v1.0-nfc)**  
  Versión inicial que emprega o **lector NFC integrado no móbil** para ler directamente as etiquetas dos produtos.

- **Rama [`rfid-uhf`](https://github.com/sualci/RFIDCartApp/tree/rfid-uhf)**  
  Versión que se conecta vía **MQTT** e recibe os produtos detectados polo **lector UHF do carro** en tempo real.

## ⚙️ Configuración da aplicación Android

1. Instalar e abrir [Android Studio](https://developer.android.com/studio).  
2. Importar o proxecto desde o repositorio clonado.  
3. Verificar as dependencias en `build.gradle`:  
   - Jetpack Compose  
   - Lifecycle  
   - Material3  
   - Paho MQTT   
4. Seleccionar un dispositivo físico (recomendado) ou un emulador configurado (consume moitos recursos).  
5. Compilar e executar a aplicación:  Run → Run 'app'


## Estrutura do proxecto

O proxecto está organizado en varios paquetes, cada un cunha responsabilidade específica:

- **data**  
  Contén a capa de datos da aplicación. Inclúe modelos, repositorios e toda a lóxica relacionada co almacenamento ou recuperación de información (por exemplo: productos, preferencias de usuario).

- **mqtt**  
  Xestiona a comunicación co broker MQTT. Encárgase das conexións e a subscricións a o topico.

- **tts**  
  Contén a funcionalidade de Text-to-Speech (síntese de voz). Converte en audio as mensaxes de texto (como cambios no carro ou avisos) para informar ao usuario.

- **ui**  
  Inclúe todos os compoñentes da interface de usuario construídos con Jetpack Compose. Aquí están as pantallas (carro, axustes, etc.), a navegación e os elementos visuais.

- **util**  
  Contén utilidades e funcións auxiliares reutilizables en diferentes partes do proxecto. Por exemplo, lóxica común.
