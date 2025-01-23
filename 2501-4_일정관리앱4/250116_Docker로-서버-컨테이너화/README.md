# Docker로 서버 컨테이너화

간단하게 "http://localhost:8080/" 요청했을때 "Hello World"라는 텍스트를 응답해주는 express 앱을 Docker로 말아서 실행시켜 볼 것이다. (express가 가볍고 좋아..)

그러기 위해 필요한 Dockerfile 작성 법을 중점적으로 기록하려 한다.

<br/>

### 목차

- [express 서버 간단 구현하기](#express-서버-간단-구현하기)
- [v1: 도커 파일 작성 시작](#v1-도커-파일-작성-시작)
- [v2: COPY를 통해 로컬 파일 복사](#v2-copy를-통해-로컬-파일-복사)
- [v3: WORKDIR로 정돈하기](#v3-workdir로-정돈하기)
- [v4: COPY 순서를 활용하여 빌드 최적화하기](#v4-copy-순서를-활용하여-빌드-최적화하기)
- [v5: Volumn을 활용하여 실행시키기](#v5-volumn을-활용하여-실행시키기)


<br/>

### express 서버 간단 구현하기

```js
const express = require('express');

const PORT = 8080;
const HOST = '0.0.0.0';

const app = express();
app.get('/', (req, res) => { res.send('Hello World'); });

app.listen(PORT, HOST);
console.log(`Running on http://${HOST}:${PORT}`);
```

js 코드는 위와 같다.

그리고 package.json은 아래와 같이 작성한다.

```json
{
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.21.2"
  }
}
```

해당 코드를 먼저 실행시켜보려면 npm install 을 한 이후에 `node server.js` 로 실행시키면 된다.

<div align="center"><img width="700" alt="Image" src="https://github.com/user-attachments/assets/d08cac6f-47f5-4c30-b4fb-61430b6abd33" /></div>

위와같이 정상 실행되는 것을 확인해 볼 수 있다.

<br/>

### v1: 도커 파일 작성 시작

우선 npm이 들어있는 베이스 이미지를 써야 하는데, `node:10` 이미지를 사용할 것이다.

```dockerfile
FROM node:10

RUN npm install

CMD ["node", "server.js"]
```

위와 같이 작성한 후에 방금 실행했던 흐름대로 작성해 보았다.

- FROM: 이미지 생성시 기반이 되는 이미지 레이어이며 여기에 베이스 이미지를 명시한다.
- RUN: 도커 이미지가 생성되기 전에 수행할 쉘 명령어를 작성한다.
- CMD: 컨테이너가 시작되었을 때 실행할 셸 스크립트를 작성한다. (Dockerfile 내 1회만 사용 가능)

```shell
$ docker build -t 'yeim/node:latest' .
```

그리고 빌드를 하면 package.json이 없다는 에러가 난다.

그 이유는 npm instatll은 package.json에 적혀있는 종속성들을 웹에서 자동으로 다운 받아서 설치해주는 명령어인데, 이미지를 만드는 과정에서 package.json을 찾지 못했기 때문이다.

<div align="center"><img width="367" alt="Image" src="https://github.com/user-attachments/assets/8b7d7db9-3bb4-46c6-bbea-6986df114aa9" /></div>

내가 만들고자 하는 Docker 이미지를 만들 때 베이스 이미지가 임시 컨테이너를 만들어서 내가 `CMD` 로 작성한 것들을 실행시키는데, package.json이 없는 상황인 것이다.

<br/>

### v2: COPY를 통해 로컬 파일 복사

이때 사용하는 명령어가 `COPY` 인데, 로컬에 있는 파일을 도커 컨테이너의 지정된 장소에 복사해줄 수 있다.

```dockerfile
FROM node:10

COPY package*.json ./ # 도커 컨테이너의 지정된 장소에 복사 해주기

RUN npm install

CMD ["node", "server.js"]
```

build는 정상적으로 되는데... 이제 실행을 시켜보자

<div align="center"><img width="500" alt="Image" src="https://github.com/user-attachments/assets/7df47117-8db0-470c-9516-4b3c452c37a7" /></div>

컨테이너를 실행시키니 server.js를 찾을 수 없다고 한다.

server.js 도 이동시켜줘야 하니 말이다.

```dockerfile
FROM node:10

COPY ./ ./

RUN npm install

CMD ["node", "server.js"]
```

server.js만 복사하기에는 언제 파일이 추가될 지 모르는데? package.json만 복사하는 게 아니라 전체를 복사해주자

<div align="center"><img width="400" alt="Image" src="https://github.com/user-attachments/assets/41cadd2a-54ff-4e35-aa1d-8c28bab3b4f6" /></div>

server가 정상 동작 하는 것은 확인했다. 하지만 8080포트로 접속은 되지 않는다.

그 이유는 컨테이너 내부에 있는 네트워크가 현재 로컬 네트워크가 연결되지 않았기 때문이다.

<div align="center"><img width="394" alt="Image" src="https://github.com/user-attachments/assets/9fdca1d6-c95b-4057-951f-9f0cc087c85c" /></div>

위와 같이 로컬 네트워크의 5000 포트를 컨테이너 내부 네트워크의 8080 포트와 매핑시켜주면 된다.

<br/>

### v3: WORKDIR로 정돈하기

아직 문제가 존재하는데, 컨테이너의 루트 디렉터리에 파일을 모두 복사하고 있는 상황이라, 겹치게 되면 어떡하지? 생각이 든다.

예를 들어 local에도 dev 디렉터리가 있고, 우리가 생성할 이미지의 베이스 이미지에도 dev디렉터리가 존재한다면?

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/aa166cd2-d440-4f87-98e6-452c347485c0" /></div>

그리고 정돈되어 있지 않다는 점이 참.. 아쉽다.

```dockerfile
FROM node:10

WORKDIR /usr/src/app

COPY ./ ./

RUN npm install

CMD ["node", "server.js"]
```

WORKDIR을 사용하면 복사될 기본 경로가 지정이 되며, 접속했을때 첫 시작 경로도 해당 경로로 고정이 된다.

<div align="center"><img width="516" alt="Image" src="https://github.com/user-attachments/assets/0e83dc6a-9a65-45c8-acd0-f638eba6f1dc" /></div>

훨씬 더 깔끔해진 것을 볼 수 있다.

<br/>

### v4: COPY 순서를 활용하여 빌드 최적화하기

소스코드를 하나 변경할 때마다 처음부터 다시 빌드해주어야 하는 것이 참 아쉽다.

종속성을 다시 다운(`RUN npm install`)으로 인해 걸리는 시간은 줄이고 싶다.

```dockerfile
FROM node:10

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install

COPY ./ ./

CMD ["node", "server.js"]
```

종속성 부분을 기준으로 COPY한 내역 중에 이전과 변경된 게 있다면 RUN을 수행하고, 변경된게 없다면 RUN이 수행되지 않는다.

<div align="center"><img width="600" alt="Image" src="https://github.com/user-attachments/assets/7cb650da-2f64-402a-acb1-abf11e0b10d7" /></div>

실제로 빌드해보면, RUN npm install 단계가 캐싱되었다고 되며 빠르게 실행이 된다.

<br/>

### v5: Volumn을 활용하여 실행시키기

아직도 소스를 변경할 때마다 변경된 소스 부분은 COPY 한 후 이미지를 다시 빌드 해주고 컨테이너를 다시 실행해줘야 변경된 소스가 화면에 반영 된다.

이런 불편함을 해결 하기 위해 Volume을 활용하여 실행시킬 것이다.

<div align="center"><img width="447" alt="Image" src="https://github.com/user-attachments/assets/4fe09931-4054-4114-99a3-56bbca8e2a66" /></div>

이제는 도커 볼륨을 이용해서 로컬에 있는 파일들을 계속 참조하여, 매번 이미지를 build 하지 않아도 된다.

<div align="center"><img width="597" alt="Image" src="https://github.com/user-attachments/assets/fb014edd-b31f-4647-bc69-09fb8fe2ef3a" /></div>

<img width="1000" alt="Image" src="https://github.com/user-attachments/assets/82a69047-c342-4e57-827a-5fe4ca6b627d" />

run 시점에 컨테이너가 로컬로부터 매핑하여 실행시키기 때문에, 이미지를 다시 빌드할 필요 없이 저 명령어로 다시 실행시키면 된다.
