# JS로 Firebase 조작하기

사용자가 form을 제출하거나, 버튼을 누르거나 하는 이벤트가 발생했을때, JavaScript로 Firebase 저장소에 접근하여 데이터를 조회/삭제/수정하는 기능을 개발했다.



### firebase 제공 함수로 CRUD하기

파이어베이스에서는 javascript 개발자를 위해 아래와 같은 함수들을 제공한다.

```javascript
import { addDoc, collection, getDocs, deleteDoc, doc } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-firestore.js";
```

그리고, 설정 방법은 아래와 같다. (데이터베이스를 생성하면 이거 복사하라고 뜨니 대충 보자)

```java
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "",
  authDomain: "",
  projectId: "",
  storageBucket: "",
  messagingSenderId: "",
  appId: ""
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
```

테이블에 데이터를 추가/조회/삭제 하기 위한 addDoc, getDocs, deleteDoc의 사용법은 아래와 같다.

```javascript
await addDoc(collection(db, "collection이름"), 넣을객체);
const querySnapshot = await getDocs(collection(db, "collection이름"));
await deleteDoc(doc(db, "collection이름", "문서id"));
```

이때 주의할 점은 getDocs를 통해 조회하는 데이터는 일반 배열이 아니라 QuerySnapshot 이라는 객체이다. querySnapshot.forEach()를 할 수 있기 때문에 배열로 오인할 수 있지만, 그것은 제공해주는 함수일 뿐이라는 것.

따라서 map이나 sort같은 js문법을 사용하고 싶다면 querySnapshot.docs 로 조회하자.



### firebase 함수 없이 fetch로 해보자

있겠지.. 있겠지.. 하면서 한 10분 열심히 찾다가 공식 문서를 결국 찾아냈다 -> [FireStore API REST](https://firebase.google.com/docs/firestore/reference/rest?hl=ko&_gl=1*1sh7ulp*_up*MQ..*_ga*Mzc2NjkxMzEzLjE3MzA3OTQyNjQ.*_ga_CW55HF8NVT*MTczMDc5NDI2My4xLjAuMTczMDc5NDI2My4wLjAuMA..) 

방법은 여러가지가 있지만 그중 한 예시는 아래와 같다.

```javascript
const response = await fetch('https://firestore.googleapis.com/v1/projects/프로젝트ID/databases/(default)/documents/컬렉션ID');
const jsonData = await response.json();
```

JavaScript의 fetch함수를 통해서 이 url로 GET, POST, DELETE 메서드를 보낸다면 원하는 컬렉션을 조작할 수 있는 것이다.

이렇게 하면 어떤 장점이 있을까? 

우선... getDocs, addDoc, deleteDoc 이런 것들은.. firebase에서 제공하는 함수에 의존적이다.

뭐 있는거 잘 쓰는것도 나쁘지 않지만, 다른 서비스를 사용한다거나 직접 서버를 구성하는 경우 바꿔야 할 코드가 넘쳐날 것이다.

이때 url만 짠 하고 바꾸면 어떨까? 참 편할 것 같다.

이렇게 되면 firebase를 mocking용 DB로 사용해도 좋고..!!



그런데.. 생각해보니 객체지향적으로 잘 풀면 꼭 REST로 안해도 될 것 같기도... 한데,

쨌든 이번주에 시간이 나면 한 번 시도해보면 좋을 것 같긴 하다.

