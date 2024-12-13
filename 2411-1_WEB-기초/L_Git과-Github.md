# git 기초

## 이론 정리

- **git**: 로컬 시스템에 설치되어서 사용 가능한 버전관리 시스템으로 CLI로 제공이 된다.
- **github**: 협업을 위해 원격으로 깃 레포지터리 기능을 제공하는 호스팅 서비스이다. GUI로 제공이 된다.

- **git이 버전을 관리하는 방법 1 - 영역**:

  <img width="600" alt="03-1" src="https://github.com/user-attachments/assets/1f3ef588-a6f6-4153-a9db-537a973818f2">

  - **Working tree(Working directory)**: git repository가 존재하는 현재 directory로 현재 파일이 저장되는 폴더이다.
  - **Stage(Staging area)**: 눈에 보이지 않는 단계로 버전으로 만들 파일들이 대기하는 중간 영역이다. `git add`
  - **Repository**: Stage에 대기하고 있던 파일들을 버전으로 만들어 저장하는 영역이다. `git commit`

  결국 현재 **Working Directory**에서 `git add`를 하면 파일이 **Staging area**로 이동하고, `git commit`을 하면 Staging area에 있는 파일들의 변경사항이 **Repository**에 반영되는 것이다.

- **git이 버전을 관리하는 방법 2 - 상태:**

  <img width="600" alt="03-2" src="https://github.com/user-attachments/assets/0c5d3dbd-3ff7-49a1-aa9a-e4a6e6d40ed5">

  - **Untracked**: git을 초기화하거나 파일을 새로 만들 때, 아직 git에 올라가지 않은 파일들을 의미한다.
  - **Tracked**: Stage 영역에 한 번이라도 간 파일들은 Tracked파일들을 의미한다.
    - Unmodified: commit한 후 수정되지 않은 파일들
    - Modified: 파일들을 수정하고 아직 staged 되지 않은 파일들
    - Staged: 수정하고 `git add` 명령어가 입력된 상태의 파일들

  즉, 단 한 번도 git add가 안된 애들은 **Untracked** 파일이고, Tracked는 크게 add직후의 **Staged**파일, commit완료된 **Unmodified** 파일, 커밋이후 수정된 **Modified** 파일로 나뉘는 것이다.

- **git이 버전을 관리하는 방법 3 - 파일:**

  - git은 내부적으로 **commit, tree, blob, tag**의 4가지 오브젝트 타입을 관리한다.

  - 이런 오브젝트는 .git/objects에 개별적인 파일들로 존재하며, 각각이 하나의 파일이다.

  - git에 "hello.txt"라는 파일을 하나 추가하면, 내용 전부를 해시테이블에 넣어, 40자리의 해시값을 뽑아내어 오브젝트 파일 이름으로 사용한다.

  - 오브젝트의 파일이름 중 앞 2글자는 디렉토리 이름으로 사용하고,나머지 38글자를 파일이름으로 사용하게 된다.



## git 명령어

init, add, pull, push, remote는 생략

- commit:

  ```shell
  $ git commit
      : 현재 상태의 스냅샷을 기록한다.
    -m: "메시지"를 같이 입력하면 vi에디터 안거치고 바로 커밋한다.
    -a: add와 동시에 commit을 진행한다.
  ```

  - 커밋은 Git 저장소에 현재 디렉토리에 있는 모든 파일에 대한 스냅샷을 기록하는 것이다.
  - 스냅샷을 기록할 때 매번 디렉토리 전체를 복사하는 것이 아닌, 이전 버전과 현재 버전의 변경내역을 저장하는 것이다.
  - 이 변경내역을 `delta` 라고 하는데, git clone할 때 `resolving delta` 라는 문구를 볼 수 있는 것도 같은 이유에서다.

- branch:

  ```shell
  $ git branch
      : 로컬 브랜치 목록을 보여주고, "브랜치이름"을 같이 입력하면 브랜치를 생성한다.
    -r: 원격 브랜치 목록을 보여준다.
    -a: 원격과 로컬 브랜치 모두의 목록을 보여준다.
  ```

  - 브랜치는 특정 커밋에 대한 참조(reference)일 뿐이다.
  - 따라서 브랜치를 이동하면 "**하나의 커밋과 그 부모 커밋들을 포함하는 작업 내역**"에 있게 된다.

- switch(checkout):

  ```shell
  $ git switch
      : "브랜치이름"을 같이 입력하면 해당 브랜치로 이동한다.
    -c: "브랜치이름"을 같이 입력하면 브랜치를 생성하여 해당 브랜치로 이동한다.
  ```

  - switch는 브랜치의 이동을 위한 명령어이다.

  - 동일한 기능을 제공하는 checkout이 원조이지만,

    브랜치 전환뿐만 아니라 파일을 복원하고, 태그를 이동하는 등 다양한 기능이 있기 때문에,

    혼란 방지를 위해 브랜치 이동은 "switch"를 사용하자