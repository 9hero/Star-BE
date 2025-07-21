
# Timer Domain Architecture (Before Refactoring)

아래 다이어그램은 리팩토링 전 타이머 도메인의 아키텍처를 보여줍니다.
`WebSocketEventListener`가 중앙에서 너무 많은 역할을 수행하며 여러 컴포넌트와 직접 상호작용하고 있음을 확인할 수 있습니다. 이로 인해 결합도가 높고 유연성이 떨어지는 구조가 되었습니다.

```mermaid
graph TD
    subgraph "사용자 (Browser)"
        Client([<font size=5>💻 Client</font>])
    end

    subgraph "Backend Server"
        direction TB

        A[<font size=5>WebSocketEventListener</font>]

        %% 서비스 및 템플릿 계층 (수평 배치)
        subgraph "의존 도메인들"
            direction LR
            B[<font size=4>RedisTemplate</font><br>Focus Room State]
            C[<font size=4>TimerService</font><br>Timer Logic]
            D["<font size=4>SimpMessagingTemplate</font><br>/topic/groups/{groupId}/timers"]
            E[<font size=4>StudyGroupSseService</font><br>SSE Notifications]
        end
        
        %% 리포지토리 및 엔티티 계층
        F[<font size=4>TimerRepository</font><br>Data Access]
        G["<font size=4>Timer (JPA Entity)</font><br>"]
    end

    %% --- 흐름 및 관계 정의 ---
    Client -- "1.웹소캣 연결or해제" --> A

    A -- "2.접속유저 save/remove 
    직접 호출" --> B
    A -- "3.타이머 서비스 직접 호출." --> C
    A -- "4.스터디 접속 여부 추적." --> D
    A -- "5.SSE Events: 
    현재 사용자 상태
    (공부중,휴식중 등)" --> E
    
    C -- "6.타이머 데이터 
    조회/수정
    start -> stop" --> F
    F -- "7.CRUD" --> G
    
    E -- "8.SSE Events 응답" --> Client


    %% --- 스타일링 (색상, 모양 등) ---
    %% Main Component
    style A fill:#BDE0FE,stroke:#555,stroke-width:3px,color:#000

    %% Client & DB
    style Client fill:#FFF1C9,stroke:#555,stroke-width:2px
    style G fill:#D8BFD8,stroke:#555,stroke-width:2px

    %% Service & Template Layer
    style B fill:#A2D2FF,stroke:#555,stroke-width:2px
    style C fill:#A2D2FF,stroke:#555,stroke-width:2px
    style D fill:#A2D2FF,stroke:#555,stroke-width:2px
    style E fill:#A2D2FF,stroke:#555,stroke-width:2px

    %% Repository Layer
    style F fill:#CDB4DB,stroke:#555,stroke-width:2px
```

## 문제점

1.  **단일 책임 원칙 (SRP) 위반**: `WebSocketEventListener`는 웹소켓 생명주기 관리, Redis 세션 관리, 타이머 비즈니스 로직 호출, WebSocket 메시징, SSE 알림 전송 등 너무 많은 책임을 가지고 있습니다.
2.  **강한 결합 (High Coupling)**: `WebSocketEventListener`가 `TimerService`, `StudyGroupSseService`, `RedisTemplate` 등 구체적인 구현체에 직접 의존하여, 한 부분의 변경이 다른 부분에 영향을 미칠 가능성이 높습니다.
3.  **낮은 유연성 및 확장성**: 연결 해제 시 또 다른 동작을 추가하려면 `WebSocketEventListener` 코드를 직접 수정해야만 합니다.
4.  **테스트 어려움**: 여러 컴포넌트와 얽혀있어 단위 테스트 작성이 매우 어렵습니다.
