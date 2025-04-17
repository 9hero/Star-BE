# 타이머

## 타이머 보정([**출처**](https://sirius7.tistory.com/156))

### 문제 요소
1. **브라우저 최적화로 인해 다른 탭을 보거나 브라우저 최소화를 사용할 경우**,  
setTimerOut이나 setInterval 함수가 느리게 작동함  
**해결법 → Web Workers 활용**  

2. **JavaScript 작동 원리에 의한 타이머 드리프트 현상**  
setTimerOut이나 setInterval 등 비동기 함수는 입력한 시간 간격으로 항상 작동하지 않음(랜덤임)  
이유는 싱글 스레드인 JS와 EventLoop에 있음.  

JavaScript의 작동 과정을 요약하자면, 

1. 콜 스택에서 코드들을 처리함.  

2. 만약 콜 스택에서 비동기 코드(함수)를 만나면, WebAPI나 Queue에 해당 함수를 넣어 둠  

3. 우리의 타이머 함수는 WEB API에 들어감.  

4. setInterval 같은 타이머 함수에 입력하는 지연 시간은 WEB API → Queue로 이동하는 시간임.  

5. [문제 발생 지점]  
EventLoop가 Queue에 들어간 함수를 꺼내서 CallStack에 넣어줌.  
5의 과정은 JavaScript 엔진이 다른 작업을 처리하고 있는지에 따라 달라질 수 있음.  
(자세히 말하자면 싱글스레드인데 작업량이 많아서 Queue에 있는 함수를 콜스택에 못 넣어줌)

![eventloop.gif](eventloop.gif)

### **해결 방법([출처](https://sirius7.tistory.com/156))**

**시간 차이를 측정해서 보정하는** 방법.  
이 방법은 기대 실행 시간과 실제 실행 시간의 차이를 이용합니다.  
동작 방식은 다음과 같습니다.  

1. 다음 콜백 함수가 호출될 기대 시간 (`expectedTime`)을 계산합니다.  
(`expectedTime = 현재 시간 + INTERVAL`)  
2. 타이머 콜백 함수가 실행되면, 현재 시간과 기대 시간의 차이 (=오차)를 계산합니다.  
3. 타이머 실행 간격 (`INTERVAL`)에서 오차를 뺍니다.  

예를 들어, 1000ms 간격으로 동작하는 타이머가 20ms 늦게 실행되었다면, 다음 타이머는 (1000-20=)980ms 후에 실행되도록 조정해 오차를 줄이는 방법입니다.

### 추가 보정
T1만큼 2번 보정 시각으로부터 빼줌  
T2: 2. ~ 3.과정 : 보정 시각부터 실행 시점까지 보정함.  

프론트에서 T2 실행 할 때 초 단위로 초과하는 것은 timeSoFar에 더해주고, 
나머지는 첫 setTimeout에 빼줘서 첫 회차의 로컬타임을 더 빨리 증가시킴  
초기에 1000ms마다 1초 단위로 증가하는 로컬타임의 setTimeout에서 뺌. 
(1000ms - 나머지) 지금은 1초 초과했으니, timeSoFar 1초 증가시키고, 0.2초만큼 빼줌  
1000ms - 200ms = 800ms 첫 증가 시점은 0.8초만에 시간 증가 시킴.  
현재는 T1만 보정하고 있습니다.

![타이머 보정.drawio.svg]
## 타이머 프로세스

## 📝 설명

**설명**  
`(시작 - 일시정지 - 종료)` 를 1개의 행 레코드로 생각하지 않고,  
**당일 날짜에는 1개의 행만 존재**한다.

---

## 📌 예시 시나리오

### 경우

- **첫 번째 세션**
  - 아침 11시에 첫 시작
  - 1시에 일시정지
  - 2시에 재시작
  - 3시에 종료

- **두 번째 세션**
  - 오후 4시에 두 번째 시작
  - 6시에 일시정지
  - 7시에 재시작
  - 8시에 종료

---

## ⏱️ 첫 시작 (`created_at` 여부 체크)

```plaintext
- insert
  → status: 시작  
     start_time: 11:00:00  
     created_at, study_date: 당일 날짜

- update
  → status: 일시정지  
     start_time: null  
     time_so_far: 7200초

- update
  → status: 시작  
     start_time: 14:00:00

- update  (*종료)
  → status: 종료  
     start_time: null  
     end_time: 15:00:00  
     total_time:  
       total_time + time_so_far + (end_time - start_time)  
         = 0 + 7200 + (15:00:00 - 14:00:00 = 3600) = `10800초 (3시간)`  
     time_so_far: 0


**두번쨰 시작(created_at 여부 체크)** 
- update -> status: 시작, start_time: 16:00:00 
일시정지
- update -> status: 일시정지, time_so_far: 7200초, start_time: null
시작
- update -> status: 시작, start_time: 19:00:00, 
종료
- update -> status: 종료, start_time: null, end_time: 20:00:00, 
		total_time: 
			total_time + time_so_far + (end_time - start_time) 
			   10800   +    7200     + (20:00:00 - 19:00:00 = 3600) = 21600(6시간), 
		time_so_far: 0
```
