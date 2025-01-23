# Docer컴포즈로 컨테이너간 네트워크 연결

Docker Compose는 다중 컨테이너 도커 애플리케이션을 정의하고 실행하기 위한 도구이다.

nodejs에서 redis-client를 통해 redis-server와 통신하는 코드를 작성한다고 했을때 

<div align="center"><img width="467" alt="Image" src="https://github.com/user-attachments/assets/139f27cf-2186-414c-9e69-ea734cb92f66" /></div>

위와 같은 멀티 컨테이너 상황에서 쉽게 네트워크를 연결시켜주기 위해서 Docker Compose가 필요하다.


<br/>

### 목차

- [redis 사용 서버 간단 구현하기](#redis-사용-서버-간단-구현하기)
- [Docker Compose파일 작성하기](#docker-compose파일-작성하기)


<br/>

### redis 사용 서버 간단 구현하기

새로고침 할 때마다 redis의 데이터가 1씩 증가하는 서비스를 구현할 것이다.

```js
const express = require('express');
const redis = require("redis");

const PORT = 8080;

const redisClient = redis.createClient();
const app = express();

app.get('/', async (req, res) => {
  try {
    await redisClient.connect();
    let number = await redisClient.get('number');
    if (number == null) {
      number = 0;
    }
    console.log('Number: ' + number);
    res.send("숫자가 1씩 올라갑니다. 숫자: " + number);
    await redisClient.set("number", parseInt(number) + 1);
  } catch (error) {
    console.error('Error handling Redis operation:', error);
    res.status(500).send('Internal Server Error');
  } finally {
    await redisClient.disconnect();
  }
});

app.listen(PORT);
console.log(`Running on ${PORT}`);
```

js코드는 위와 같고 package.json은 아래와 같이 작성한다.

```json
{
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "express": "^4.21.2",
    "redis": "^4.7.0"
  }
}
```

그리고 npm install을 한 이후 `node server.js`로 실행시킨다.

<div align="center"><img src="https://github.com/user-attachments/assets/c5620651-0f31-4814-a311-be2ea3f020f4" alt="Image"/></div>

만약 6379포트에 실행중인 redis 서버가 있다면 실행 결과는 위와 같다.

나는 컨테이너간 통신으로 할 것이기 때문에 저 `redis.createClient()` 내부 파라미터로

```js
const redisClient = redis.createClient({
  socket: {
    host: "redis-server", //docker-compose.yml 파일에 명시한 컨테이너 이름으로 주면된다.
    port: 6379
}});
```

를 넘길 것이다.

<br/>

### Docker Compose파일 작성하기

<div align="center"><img width="467" alt="Image" src="https://github.com/user-attachments/assets/139f27cf-2186-414c-9e69-ea734cb92f66" /></div>

그림을 다시 보자. 서로 다른 컨테이너가 있을 때 이 컨테이너 사이에 통신을 하려면 Docker Compose가 필요하다고 했다.

그러기 위해서는 우선 Docker Compse 파일이 필요하다.

`docker-compose.yml` 을 생성하자

```yaml
version: "3" # 도커 컴포즈 파일 버전
services: # 실행하려는 컨테이너들 묶음 정의
  redis-server: # 컨테이너 이름
    image: redis # 컨테이너가 사용하는 이미지
  node-app: # 컨테이너 이름
    build: . # 현 디렉토리에 있는 Dockerfile 사용
    ports:  # 포트 매핑 [로컬포트:컨테이너포트]
      - "5000:8080"
```

위의 파일을 잘 보면 이전에 우리가 Dockerfile로 생성한 이미지를 생성하고 실행할 때 사용했던 명령어와 유사한 것을 확인할 수 있다.

```sh
$ docker build -t '저장소/프로젝트이름:버전'
$ docker run -p 5000:8000 이미지이름
```

다시봐도 상당히 유사하다.

그럼 이렇게 작성한 yml파일을 어떻게 컨테이너화 할까?

```sh
$ docker-compose up
```

위의 명령어를 실행시키면 된다. (--build 옵션을 주지 않으면 이미지가 없을때만 이미지를 빌드하고 -d옵션을 하면 바로 나와진다.)  종료할 때에는

```sh
$ docker-compose down
```

이 명령어를 사용하면 된다.

