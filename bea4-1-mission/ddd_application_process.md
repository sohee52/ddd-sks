# bea4-1-mission 프로젝트에 DDD 적용 과정

## 1. BoundedContext

### BoundedContext
- com.back.boundedContext.member 패키지 도입 후 관련 클래스들을 해당 패키지로 옮김
- com.back.boundedContext.post 패키지 도입 후 관련 클래스들을 해당 패키지로 옮김
  - cf. PostComment도 여기에 포함됨

### Global
- com.back.global 패키지 도입 후 관련 클래스들을 해당 패키지로 옮김
- 전역적으로 사용되는 클래스가 여기에 해당됨

---

## 2. 이벤트 기반 결합도 제거

### eventPublisher 도입

- [EventPublisher](after\02_event_driven_decoupling\global\eventPublisher\EventPublisher.java)
- [GlobalConfig](after\02_event_driven_decoupling\global\config\GlobalConfig.java)

#### eventPublisher의 역할
- 이벤트를 발행하는 역할
- 즉, 이벤트를 생성해서 이벤트 리스너에게 전달하는 역할
- 즉, 이벤트가 발생했다는 사실만 알려주는 역할

### event 생성

- [PostCommentCreatedEvent](after\02_event_driven_decoupling\shared\post\event\PostCommentCreatedEvent.java)
- [PostCreatedEvent](after\02_event_driven_decoupling\shared\post\event\PostCreatedEvent.java)

#### 엔티티가 아닌 DTO로 감싸서 이벤트 생성
- [PostCommentDto](after\02_event_driven_decoupling\shared\post\dto\PostCommentDto.java)
- [PostDto](after\02_event_driven_decoupling\shared\post\dto\PostDto.java)
- cf. 왜 점수는 포함 안 했을까?
  - 점수는 이벤트를 처리하는 쪽에서 새로 계산하면 되기 때문
  - 점수를 포함하게 되면, 나중에 점수 계산 방식이 바뀌었을 때 이벤트를 발행하는 쪽도 수정해야 함
  - 따라서 결합도가 높아짐
  - DTO로 감싸서 필요한 데이터만 전달하는 것이 더 유연함

### eventListener 도입

- [MemberEventListener](after\02_event_driven_decoupling\boundedContext\member\eventListener\MemberEventListener.java)

#### eventListener의 역할
- 이벤트를 수신하는 역할
- 즉, 이벤트가 발생했을 때 해당 도메인의 규칙을 적용하는 역할

---

## 3. 각 바운디드 컨텍스트 내부를 DDD 형태로 구조화

외부에서 어떤 변화가 들어오고(in) → 그걸 비즈니스 규칙으로 처리하고(app/domain) → 그 결과를 외부로 내보낸다(out)

### in : 변화의 시작점, 외부의 입력신호에 따라서 어떠한 일을 시작하는 영역
- controller : 외부의 입력신호를 받아서 처리하는 영역
- eventListener : event를 받아서 처리하는 영역
- scheduler : scheduler를 통해서 주기적으로 실행되는 영역

### app : facade와 service가 존재하는 영역
- facade : in의 클래스는 usecase에 바로 접근할 수 없고 facade를 통해야 한다.
  - Facade 메서드에는 `@Transactional`이 붙여서 트랜잭션 경계를 설정한다.
- usecase : usecase는 비즈니스 로직을 구현하는 영역
  
### domain : 순수 비즈니스 로직을 구현하는 영역
- entity : 비즈니스 규칙을 구현하는 영역
- policy : 비즈니스 규칙을 구현하는 영역

### out : 변화의 결과를 외부에 반환하는 영역
- repository : 데이터를 저장하고 조회하는 영역
- apiClient : 외부 서비스와 연동하는 영역

---

## 4. Service를 Facade와 Usecase로 분리
- [MemberService](after\02_event_driven_decoupling\boundedContext\member\service\MemberService.java) → [MemberFacade](after\04_facade_usecase\MemberFacade.java) + [MemberUsecase](after\04_facade_usecase\MemberJoinUsecase.java)
- [PostService](after\02_event_driven_decoupling\boundedContext\post\service\PostService.java) → [PostFacade](after\04_facade_usecase\MemberFacade.java) + [PostUsecase](after\04_facade_usecase\PostWriteUsecase.java)

---

## 5. Policy 도입
- [MemberPolicy](after\05_policy\MemberPolicy.java)

---

## 6. RsData 도입
- [RsData](after\06_rs_data.md\RsData.java) = Response Data로, 응답 데이터를 감싸는 클래스
- ResponseEntity와 비슷한 역할로, ResponseEntity를 사용해도 무방하다.
- resultCode, msg, data 필드를 가짐
- RsData의 장점:
  - 일관된 응답 형식 제공
  - 추가적인 메타데이터 포함 가능
  - 에러 처리 용이

---

## 7. 글 작성 시, 회원 관련 랜덤 팁 제공
- 단, 타 모듈 간 메서드 호출 없이 API 호출로 구현해야 한다. 그렇지 않으면 결합도가 높아지기 때문.
- 흐름: Post 모듈 → MemberApiClient → HTTP 요청 → ApiV1MemberController → MemberFacade

### Event vs API 호출 비교

| 구분 | Event | API 호출 |
|------|-------|----------|
| 방식 | 비동기 (fire and forget) | 동기 (요청-응답) |
| 응답 | ❌ 받을 수 없음 | ✅ 받을 수 있음 |
| 용도 | "이런 일이 일어났어" 알림 | "이거 줘" 요청 |

#### 코드에서 보면

```java
// PostWriteUseCase.java

// 1. Event 사용 - 응답 필요 없음
eventPublisher.publish(new PostCreatedEvent(...));
// → "글이 생성됐어!" 라고 알리기만 하면 됨
// → 누가 듣든 말든, 뭘 하든 말든 상관없음

// 2. API 호출 - 응답 필요함
String randomSecureTip = memberApiClient.getRandomSecureTip();
// → 보안 팁 문자열을 받아서 결과 메시지에 넣어야 함
return new RsData<>("201-1", "...보안 팁 : %s".formatted(..., randomSecureTip), post);
```

`getRandomSecureTip()`의 **반환값**이 필요하기 때문에 Event로는 불가능해. Event는 단방향 통신이라 "야 보안 팁 하나 줘" 하고 기다릴 수가 없거든.

#### 왜 직접 메서드 호출 안 하고 API?

```java
// ❌ 직접 호출 - 결합도 높음
private final MemberFacade memberFacade;  // post 모듈이 member 모듈에 의존
memberFacade.getRandomSecureTip();

// ✅ API 호출 - 결합도 낮음
private final MemberApiClient memberApiClient;  // HTTP 통신만
memberApiClient.getRandomSecureTip();
```

API 호출 방식은 나중에 member 모듈을 별도 서버로 분리해도 URL만 바꾸면 되니까 확장성이 좋아.

## 8. Post_Member 테이블 도입

- POST_MEMBER 테이블 생성 → 테이블 간의 결합도를 낮추기 위함
- POST_MEMBER 테이블의 id, createDate, modifyDate 칼럼에 자동 값 등록 옵션 제거
  - 복제본(ReplicaMember)과 원본(SourceMember) 간의 충돌 방지 목적

| 항목 | BaseIdAndTime | BaseIdAndTimeManual |
|------|---------------|---------------------|
| ID 생성 | `@GeneratedValue(IDENTITY)` 자동 | 없음 (수동) |
| 생성일 | `@CreatedDate` 자동 | 없음 (수동) |
| 수정일 | `@LastModifiedDate` 자동 | 없음 (수동) |
| Auditing | `@EntityListeners` 있음 | 없음 |

---

## 9. BaseMember 클래스 도입

- BaseMember : Member의 공통 로직
- ReplicaMember : 복제 Member 클래스용 상위 클래스
- SourceMember : 원본 Member 클래스용 상위 클래스
- Member : 원본 Member
- PostMember : 복제 Member

### 복제 모델과 원본 모델로 분리하는 이유

이 구조는 보통 **복제(read) 모델과 원본(write) 모델을 분리**하거나, **동기화·이벤트 기반 구조**에서 사용된다.

### 1️⃣ BaseMember — 공통 규칙의 단일 위치

```text
BaseMember
 ├─ 공통 필드 (id, createdAt 등)
 ├─ 공통 불변 규칙
 └─ 공통 메서드
```

**목적**

* Member 계열에서 **절대 중복되면 안 되는 로직을 한 곳에 모음**
* “Member라면 반드시 지켜야 하는 규칙”을 강제

**왜 필요한가**

* 복제/원본이 나뉘어도
  → “회원은 회원이다”라는 **도메인 불변성**은 동일해야 함
* 중복 제거 + 규칙 누락 방지

### 2️⃣ SourceMember — 원본(Write 모델)의 규칙 보호

```text
BaseMember
 └─ SourceMember
      └─ Member (원본)
```

**SourceMember의 역할**

* **쓰기 가능한 Member의 상위 타입**
* 변경 가능한 행위(행동)를 이쪽 계층에만 둠

예:

```java
changePassword()
changeNickname()
withdraw()
```

**의도**

* “이 객체는 수정해도 되는 Member다”를 **타입으로 명확히 표현**
* 실수로 복제 객체에서 `changePassword()` 호출하는 걸 **컴파일 단계에서 차단**

---

### 3️⃣ ReplicaMember — 복제(Read 모델)의 안전장치

```text
BaseMember
 └─ ReplicaMember
      └─ PostMember (복제)
```

**ReplicaMember의 역할**

* **읽기 전용 Member의 상위 타입**
* 상태 변경 메서드를 아예 가지지 않음

**왜 필요한가**

* 복제 데이터는:

  * 이벤트로 동기화되거나
  * 외부 시스템에서 받아온 스냅샷일 가능성이 큼
* → **절대 도메인 로직으로 수정되면 안 됨**

즉:

> “이 객체는 보기만 하라고 존재한다”는 걸 **타입으로 강제**하는 것

### 4️⃣ Member vs PostMember — 이름은 비슷하지만 역할이 다름

| 클래스          | 의미                           |
| ------------ | ---------------------------- |
| `Member`     | **진짜 원본 엔티티 (write source)** |
| `PostMember` | 게시글에 붙어 있는 **복제된 회원 정보**     |

#### 왜 굳이 분리하나?

예를 들어:

* 게시글은 `authorName`, `authorProfileImage`만 필요
* 굳이 전체 Member를 참조하면:

  * 결합도 증가
  * 원본 수정 시 게시글까지 영향

➡ **PostMember는 “게시글에 필요한 회원 정보의 스냅샷”**


### 이 구조가 주는 핵심 이점

#### 1. 실수 방지 (타입으로 차단)

```java
void update(Member m)        // 가능
void update(PostMember m)    // 불가능
```

#### 2. 도메인 의도 명확

* “이건 원본이다”
* “이건 복제다”
  → 코드만 봐도 의미가 드러남

#### 3. 확장에 강함

* 나중에

  * `CommentMember`
  * `LikeMember`
    같은 복제 모델 추가 가능


#### 한 줄로 정리하면

> **“같은 Member라도, 수정 가능한 원본과 읽기 전용 복제는 절대 같은 타입이면 안 된다”**
> 그래서 **상속 구조로 책임과 역할을 강제 분리한 것**이다.

---

## 10. 회원 가입시 MemberJoinedEvent 발생, PostMember 테이블에 동기화, password 필드는 제외

**syncMember 메서드 리팩토링, PostMember 클래스 생성자가 id, createDate, modifyDate 칼럼도 같이 받기**

- [MemberJoinUseCase](after\10_memberJoinedEvent_PostMemberSync\MemberJoinUseCase.java) : 회원 가입 요청
- [MemberJoinedEvent](after\10_memberJoinedEvent_PostMemberSync\MemberJoinedEvent.java) : 회원 가입 이벤트 발행
- [PostEventListener](after\10_memberJoinedEvent_PostMemberSync\PostEventListener.java) : 이벤트 수신
- [PostFacade.syncMember()](after\10_memberJoinedEvent_PostMemberSync\PostFacade.java) : PostMember 동기화

---

## 11. 회원 정보(activityScore) 수정 시 MemberModifiedEvent 발생, PostMember 테이블에 동기화

- [Member.increaseActivityScore()](after\11_memberModifiedEvent_PostMemberSync\Member.java) : activityScore 증가
  - `publishEvent(new MemberModifiedEvent(new MemberDto(this)));` 호출
- [PostEventListener](after\11_memberModifiedEvent_PostMemberSync\PostEventListener.java) : 이벤트 수신
- [PostFacade.syncMember()](after\11_memberModifiedEvent_PostMemberSync\PostFacade.java) : PostMember 동기화
  - `postMemberRepository.save(postMember)` 호출 → DB에 저장

---

## 12. Member → PostMember로 변경

### 기존 코드 문제점
- Post 테이블의 FK가 MEMBER_MEMBER 테이블을 직접 참조
- Member 도메인이 변경되면 Post 도메인도 영향 받음
- 나중에 마이크로서비스로 분리 불가능

### Member → PostMember로 변경
- Post 컨텍스트는 자기 안에 있는 PostMember만 참조해서 외부 의존성이 없어짐

---

## 13. 초기 데이터 만드는 코드도 모듈별로 분리

### DataInit → MemberDataInit + PostDataInit 이렇게 분리하는 이유
- 각 모듈이 자기 데이터 초기화만 책임지도록 하기 위함
- 모듈 간의 결합도를 낮추기 위함

코드
- [MemberDataInit](after\13_separate_datainit\MemberDataInit.java)
- [PostDataInit](after\13_separate_datainit\PostDataInit.java)

주의할 점
- MemberDataInit이 PostDataInit보다 먼저 실행되어야 함
- PostDataInit에서 PostMember를 생성할 때, Member가 이미 존재해야 하기 때문
- 이를 위해 스프링 빈 설정에서 MemberDataInit을 먼저 등록함
- [MemberDataInit](after\13_separate_datainit\MemberDataInit.java) → `@Order(1)`
- [PostDataInit](after\13_separate_datainit\PostDataInit.java) → `@Order(2)`

