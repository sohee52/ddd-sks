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
- usecase : usecase는 비즈니스 로직을 구현하는 영역
  
### domain : 순수 비즈니스 로직을 구현하는 영역

### out : 변화의 결과를 외부에 반환하는 영역
- repository : 데이터를 저장하고 조회하는 영역
- apiClient : 외부 서비스와 연동하는 영역

