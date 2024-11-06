# 파이어베이스 접근 코드 모듈화

[어제 실습했던 코드](../241105_파이어베이스)에 이어서 파이어베이스의 함수(getDocs, deleteDoc, addDoc, ...)를 사용하는 코드를 분리해서 재사용하기 쉽도록 변경했다.



## RepositoryInterface 만들기

순수 JavaScript 문법에서는 Interface를 제공하지 않기때문에, 객체지향프로그래밍에서 중요한 다형성을 활용하기가 불가능하다.

하지만, 이 다형성을 활용하지 못하더라도, **`앞으로 만들고 싶은 Repository는 반드시 이 메서드를 구현하라`** 라는 메시지를 담아서 코드를 확장성 있게 유지보수 할 수 있도록 하고 싶었다.

이를 위한 코드는 아래와 같다. 생성자에 집중!

```javascript
export default class RepositoryInterface {
  constructor() {
      if (new.target === RepositoryInterface) {
        throw new Error("RepositoryInterface 클래스는 인스턴스화할 수 없습니다.");
      }
      const interfaceMethods = Object.getOwnPropertyNames(RepositoryInterface.prototype)
        .filter(prop => 
                	typeof RepositoryInterface.prototype[prop] === 'function' 
	              	&& prop !== 'constructor');
      
      const overridedMethods = Object.getOwnPropertyNames(Object.getPrototypeOf(this))
        .filter(prop => typeof this[prop] === 'function' && prop !== 'constructor');
      
        interfaceMethods.forEach(method => {
        if (!overridedMethods.includes(method)) {
          throw new Error(`${method} 메서드를 오버라이드해야 합니다.`);
        }
      });
  }

  async save(obj) {}
  async findAll() {}
  async findBy(key, value) {}
  async deleteById(id) {}
  async updateById(id, obj) {}
}
```

- 이 클래스를 통해서 바로 생성하려고 하면 생성 못하게 예외 발생

- `interfaceMethods` 에 현재 선언되어 있는 메서드들을 담고, `overridedMethods`에 extends할 클래스가 갖고 있는 메서드들을 담아 이 둘을 비교하여 정상적으로 모두 구현되었는지 확인한다.

이렇게 되면, **코딩 (컴파일) 단계**에서 확인할 수는 없지만, 적어도 **코드 실행(런타임) 단계**에서는 '아 이 클래스는 이렇게 사용해야겠구나' 하는 메시지를 줄 수 있다.

물론 타입스크립트는 다형성 활용이나, 인터페이스 필수 구현 이런게 되어 있지만, 지금은 순수 js로 하고 있기 때문에... 재밌는 도전이었다.



## Repository 구현체 만들기

다음으로 위에서 만든 인터페이스를 구현하는 구현체를 만들었다.

```js
const db = getFirestore(initializeApp(firebaseConfig));

export default class FirebaseRepository extends RepositoryInterface {
  #collectionName;
  constructor(collectionName) {
    super();
    this.#collectionName = collectionName;
  }

  async save(obj) {
    addDoc(collection(db, this.#collectionName), obj);
  }

  async findAll() {
    const querySnapshot = await getDocs(collection(db, this.#collectionName));
    return querySnapshot.docs.map((doc) => ({...doc.data(), id: doc.id}));
  }

  async findBy(key, value) {
    const q = query(collection(db, this.collectionName), where(key, '==', value));
    const querySnapshot = await getDocs(q);
    return querySnapshot.docs.map((doc) => ({...doc.data(), id: doc.id}));
  }
  
  async updateById(id, obj) {
    updateDoc(doc(db, this.#collectionName, id), obj);
  }

  async deleteById(id) {
    deleteDoc(doc(db, this.#collectionName, id));
  }
}
```

인터페이스를 구현하기 위해 어제 배운 add, get, delete 말고도 공식문서 참고해서 updateDoc으로 수정, query와 where을 이용해 필터링 하는 것도 반영했다.

각 테이블별로 Repository 인터페이스와 구현체를 만들어볼까.. 하다가 그건 너무 과한거 같아서 간단하게 테이블 이름은 생성자를 통해 입력 받도록 했다.



## 의존관계를 위한 파일 분리하기

지금까지 코드들을 활용하여 `comments` 테이블의 모든 값들을 불러오는 코드를  main.js 파일에서 작성한다고 해보자. 만들어둔게 신나서 아래처럼 바로 사용할 수도 있을 것이다.

```js
// main.js
import FirebaseRepository from "./FirebaseRepository.js";

const repsoitory = new FirebaseRepository("comments");
const data = await repsoitory.findAll();
```

하지만, 우리가 더이상 FirebaseRepository를 사용하지 않고 다른 서비스 기반의 Repository로 변경하려 한다면? 코드를 변경해야 한다.

```js
// main.js
import MemoryRepository from "./MemoryRepository.js";

const repsoitory = new MemoryRepository("comments");
const data = await repsoitory.findAll();
```

위 처럼 코드가 바뀐다. 이제 이 파일에는 두가지 책임이 있다는 것을 알 수 있다. 

1. repository가 제공하는 메서드를 적절하게 잘 사용하는 책임
2. 필요한 repository 인스턴스와 의존관계를 갖게하는 책임

나는.. 1번의 책임만 갖게 하여, 사용하려는 repository가 달라도 이 파일이 수정되지 않길 원한다.

그러기 위해서는 의존관계를 설정하는 파일(`dependency.js`)을 따로 만들어야 했다.

```js
// dependency.js
import FirebaseRepository from "./FirebaseRepository.js";

export function commentRepository() {
  return new FirebaseRepository("comments"); //변경시 이 라인 변경
}

// main.js
const repsoitory = commentRepository();
const data = await repsoitory.findAll();
```

이렇게 하면 다른 서비스를 위한 Repository 구현체로 변경하고 싶을때, `dependency.js`만 수정한다면 `main.js`를 건들이지 않아도 된다.

<br/>

'이게 뭐 별거야? 이전에도 그냥 main.js에서 한줄 바꾸는 건 동일하잖아?' 할 수 있지만,

프로젝트가 커져서 의존관계가 늘어날수록 어디서 의존관계가 형성되어 있는지 매번 찾기가 힘들고,
그 의존관계가 바뀜에 따라 제공하는 기능들마저 달라지면 정말 머리아플 것이다.

<br/>

물론.. 조금 과한것도 없지않아 싶긴 하지만,
그동안 웬만해서 돌아가다가 뜬금없이 터지는 js로 더 나은 유지보수를 고민해본 경험으로 남기려 한다.ㅎㅎ





