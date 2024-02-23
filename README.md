## ⬇️ Install
Client of "All Ears" is a `WearOS` application and requires the following dependencies. Make sure to have the correct dependencies for the project.
- java version: 8
- kotlin version: 1.7.20
- gradle version: 8.2
- android: Hedgehog | 2023.1.1 Patch 1
- complie SDK version: API 34

## ⌚ Device
"All Ears" was tested on the `Galaxy Watch 4`, which has an `ARM Mali-G68 MP2 667 MHz GPU`. If you are planning to run this code on a device, make sure your edgy device has the same or higher GPU specs.
- Galaxy watch 4 (44mm)
- ARM Mali-G68 MP2 667 MHz GPU

## ⚙️ Architecture
<div>
  <img alt="Wear OS" src ="https://img.shields.io/badge/wearos-4285F4.svg?&style=for-the-badge&logo=wearos&logoColor=white"/>
  <img alt="Tensorflow Lite" src ="https://img.shields.io/badge/tensorflow lite-FF6F00.svg?&style=for-the-badge&logo=tensorflow&logoColor=white"/>
  <img alt="GCP STT" src ="https://img.shields.io/badge/GCP STT-4285F4.svg?style=for-the-badge&logo=googlecloud&logoColor=white"/>
</div>

<img width="1920" alt="allears-architecture-android" src="https://github.com/TeamAllways-AllEars/All-Ears_Android/assets/89632139/e6ed4554-d6e1-43c0-bb92-42e4875ca379">

Client of "All Ears" uses the **GCP STT API** for **Live Captioning**. To run this code with your GCP resource, make sure to apply your API key for the **GCP STT API Service account** in the `app/src/main/res/raw/credential3.json` file (❗Do not change the directory or file name❗). Please create a Service account for STT in GCP and apply the JSON key file in your project. The format of your JSON API key file should look like this:
```json
{
  "type": "service_account",
  "project_id": "...",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "...",
  "client_id": "...",
  "auth_uri": "...",
  "token_uri": "...",
  "auth_provider_x509_cert_url": "...",
  "client_x509_cert_url": "...",
  "universe_domain": "..."
}
```
