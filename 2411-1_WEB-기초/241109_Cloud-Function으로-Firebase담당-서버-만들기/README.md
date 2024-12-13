# Cloud Functions로 Firebase담당 서버 만들기

Firebase에 따로 카드를 등록 안해서.. '뭐 과금 나올정도로 과한 요청 받으면, 알아서 뭐 서비스 정지 되겠지' 라는 생각으로 그냥 모든걸 오픈했는데

아무래도 DB에 누구나 접근하게끔 오픈하는건 영 찝찝했다.

단순히 github에서 secret key를 등록해서 하려 했는데, github page는 그렇게 할 수 없다네?

서브모듈도 해보려했는데, 앞의 방식이 안되면 그건 당연히 안될 느낌이었다.

<br/>

gpt 선생님 도와주세요!

<img width="495" alt="스크린샷 2024-11-10 오전 1 35 28" src="https://github.com/user-attachments/assets/86ea2cd0-e25d-4f69-b4c2-e8bebe035925">

Netlify나 Vercel을 사용해서 해결하기 보다는, 뭔가 firebase 자체적으로 제공해주는 기능을 통해 해결하고 싶었다.

뭐랄까.. 관심사의 분리? firebase DB 보안은 firebase 안의 서비스로 관리하자? 그런 느낌?

그래서 Cloud Functions 사용 결정!

<img width="1500" src="https://github.com/user-attachments/assets/f396b9b8-c9a1-43fc-842d-6c7588aeef2d">
위와 같이 변경할 예정이다!

## 목차

- [Cloud Functions를 위한 설정](#cloud-functions를-위한-설정)
- [트리거되는 함수 작성 후 배포](#트리거되는-함수-작성-후-배포)
- [클라이언트에서](#클라이언트에서)
- [마치며](#마치며)

<br/>

## Cloud Functions를 위한 설정

내가 이해한 Cloud Functions는 트리거(HTTP 요청, Firebase 서비스에서의 이벤트, Google Cloud 서비스에서의 이벤트, Pub/Sub 메시지, ...)가 생기면 내가 작성한 함수가 실행되는 서버리스 서비스이다.

이 함수를 작성할때 Python또는 Node.js로 작성이 가능한데, 나는 익숙한 Javascript로 할 거다.

```shell
$ npm install -g firebase-tools
$ firebase login
$ firebase init functions
```

- 로컬 컴퓨터에 Firebase CLI를 설치한다.
- Firebase 프로젝트와 연동하기 위해 로그인하고 초기화한다. 이때 어떤 프로젝트에 연결할건지, 언어는 뭘 쓴건지 설정하는 선택지가 주어진다.
- 초기화 도중에 파이어베이스에 연결되며 결제정보를 입력하라는 내용이 나오는데, 생각보다 저렴해서 50,000원 근접했을 때 알림 오도록 설정해두고 결제정보 입력했다.



## 트리거되는 함수 작성 후 배포

나는 crud 라는 함수 하나를 배포할 것이다.

get, post, put, delete 요청을 처리하여 클라이언트에게 응답하는 함수이다.

함수를 배포하기 위해서는 우선 `firebase` 폴더 안에 index.js파일(firebase init functions 하면서 구조가 이미 만들어져 있다.) 에 함수를 작성해야 한다.

```js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
const corsHandler = cors({
  origin: true
});

admin.initializeApp();
const db = admin.firestore();

exports.crud = functions.https.onRequest((req, res) => {
  corsHandler(req, res, async () => { 
    switch(req.method) {
      case 'GET' : {}
      case 'POST' : {}
      case 'PUT' : {}
      case 'DELETE' : {}
      default : {
        res.status(400).send("Bad Request");
      }
    }
  });
});
```

express도 쓸 수 있긴 하지만, 최대한 기본기능을 활용해서 작성해봤다. 여기엔 비워두긴 했는데, 저 빈칸안에 처리하고 응답하는 코드를 작성하면 된다.

cors 설정은 일단 임시로 true로 해두었다.

마지막으로 아래의 명령어를 입력하면 배포가 완료된다.


```shell
$ firebase deploy --only functions
```

<img width="1227" alt="스크린샷 2024-11-10 오전 2 30 22" src="https://github.com/user-attachments/assets/6ea3db38-26c5-452f-b24f-8d245595b13e">

여기서 트리거 필드를 자세히 보면 url이 나와있다.

이제 이전 코드를 파이어베이스 직접 접근하는 방식이 아닌, 내가 배포한 트리거 함수를 거치는 방식으로 변경할 것이다.



## 클라이언트에서 

내가 지난번에 Interface 만든게 드디어 도움이 되는구나! [변경된 커밋 보기](https://github.com/yeongbinim/TIL/commit/1d768d762d6bdd0b1dc0720e1c6a41e6a4d653b8)

RepositoryInterface를 구현하는 FunctionsRepository를 저 `https://crud-4xwhswem4q-uc.a.run.app` 에 직접 fetch 요청을 하도록 변경하여 제공되는 기능은 달라지지 않도록 한다.

```js
import RepositoryInterface from "./RepositoryInterface.js";

export default class RequestRepository extends RepositoryInterface {
  #collectionName;
  #baseUrl = "https://crud-4xwhswem4q-uc.a.run.app";

  constructor(collectionName) {
    super();
    this.#collectionName = collectionName;
  }

  async findAll() {
    const response = await fetch(`${this.#baseUrl}?collectionName=${this.#collectionName}`);
    return response.json();
  }

  ...생략
}

```

그리고 마지막으로 dependency만 바꿔주면 된다.

```js
// import FirebaseRepository from "./FirebaseRepository.js";
import RequestRepository from "./RequestRepository.js";

export function commentRepository() {
  return new RequestRepository("comments");
}
```

뿌듯하다. 이 맛에 객체지향하는구나



여튼 이대로 페이지를 띄우니 정상적으로 실행되는 결과를 확인할 수 있었다.

클라이언트에서 Config정보 노출 없이, 내가 Cloud Functions에 배포한 함수를 통해 DB에 접근하는 방식이다.



## 마치며

그런데 하고보니 결국 설정정보 자체를 은닉화하는 방식이아닌, 서버 하나를 띄운 방식이 됐다.

그래도 서버리스 뭔가.. 서버가 없는건가.. 했는데

이번 기회에 '아 서버관리 없이 코드관리에만 신경쓸 수 있도록 해준 서비스를 말하는 구나'를 알았다.

정말 말도 안되고 어이없는게, 서버 하나를 이렇게 쉽게 배포할 수 있다고? DB까지 지원되는걸?

'프론트엔드 개발자는 사이드 프로젝트 할 때 이걸로 가볍게 api 만들어두고 프론트만 집중하면 되겠는데?' 라는 생각도 들었다.

물론 이걸로 프로덕션 수준의 최적화된 서비스는 제공하기 힘들거 같다. (지금도 속도가 상당히 느리다.)

<br/>

하지만 역으로 백엔드 개발자가 만든 API를 프론트엔드 개발자 팀원 없이, GUI로 쉽게쉽게 웹 서비스를 만들 수 있지 않을까? 생각해서 찾아보니

이런 방식을 노-코드 방식이라고 한다고 한다. 그리고 실제로 그런 서비스를 제공하는 많은 곳들이 있었다. (나중에 최종 프로젝트에 써먹어 볼 수도 있겠다는 생각도 든다.)