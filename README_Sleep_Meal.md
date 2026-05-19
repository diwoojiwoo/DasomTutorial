# 취침 및 식사 문답 (Sleep and Meal Questionnaire)

## 📌 개요 (Overview)
본 기능은 사용자의 일상 시간(취침, 기상, 아침, 점심, 저녁 식사)에 맞춰 로봇(또는 디바이스)이 안부를 묻고 대화를 유도하는 **상호작용(문답) 기능**입니다. 사용자의 음성 응답을 인식하여 서버에 기록하고, 상황에 맞는 멘트와 모션을 통해 자연스러운 대화를 이어나갑니다.

## ⚙️ 주요 기능 및 동작 흐름 (Features & Flow)

### 1. 화면 진입 및 UI 설정 (`LearnFragment.kt`)
- `OnethefullBase.MEAL_TYPE_SHOW` 및 `OnethefullBase.MEAL_TYPE_FINISH` 인텐트 타입을 통해 Fragment가 실행됩니다.
- 전달받은 `mealCategory` 파라미터(예: `SLEEP_TIME_NAME`, `LUNCH_TIME_NAME`)를 통해 현재 진행할 문답의 종류를 식별합니다.
- UI 시각 효과:
  - **취침 및 기상 문답**: 수면 배경 (`R.drawable.img_sleep`) 적용
  - **식사 문답**: 식사 배경 (`R.drawable.img_meal`) 적용

### 2. 문답 데이터 수신 및 TTS 발화 (`LearnViewModel.kt`)
- `getMessageList()` API 로직을 통해 해당 카테고리(취침/식사)에 맞는 TTS 발화 텍스트를 서버로부터 수신합니다.
- `GCTextToSpeech` 엔진을 이용해 수신받은 텍스트를 사용자에게 음성으로 읽어줍니다.
- 음성 출력이 완료되면, 자동으로 마이크(STT)를 켜서 사용자의 응답을 대기합니다.

### 3. 사용자 음성 인식 및 응답 기록 (`LearnViewModel.kt`)
- 사용자의 음성 응답이 인식되면 `handleRecognition(text: String)` 함수가 호출됩니다.
- `/log/checkChatBotData/` API를 호출하여, 인식된 사용자의 발화 데이터와 해당 카테고리 정보를 서버에 기록합니다.

### 4. 상황별 종료 리액션 및 모션 제공 (`LearnRepository.kt`)
- 문답 시 **최초 질문은 서버에서 수신**하지만, 사용자가 대답한 후 **문답이 종료될 때의 리액션(마무리 멘트)은 앱 내부(로컬)에서 처리**됩니다.
- 타겟 디바이스(BEANQ, KEBBI)에 맞게 `LearnRepository.kt`에 정의된 멘트 리스트 중 하나를 랜덤으로 선택하여, 해당하는 애니메이션(모션)과 함께 제공합니다.
- **`getMealFinishKebbiUiAction()` / `getMealFinishBeanQUiAction()` 내부 예시 (랜덤 선택)**:
  - **기상문답**: "아침 맛있게 드세요. 속에 부담가지 않도록 천천히 드셔야 해요." 등 (초롱초롱 모션)
  - **점심문답**: "점심 맛있게 드시고 즐거운 오후 보내세요." 등 (음악/즐거움 모션)
  - **저녁문답**: "저녁 맛있게 드세요. 오늘 하루도 수고 많으셨어요." 등 (사랑/부끄러움 모션)
  - **취침문답**: "오늘 하루 대화를 나눠주셔서 감사해요. 푹 주무시고 내일 봬요." 등 (수면 모션)

## 📁 주요 관련 파일 (Core Files)
- **`ui/learn/LearnFragment.kt`**
  - 화면 UI 구성, 카테고리에 따른 배경 이미지 분기, 애니메이션 리소스 관리
- **`ui/learn/LearnViewModel.kt`**
  - STT/TTS 제어 로직, 음성 인식 콜백 처리, API 통신 (응답 데이터 전송)
- **`repository/LearnRepository.kt`**
  - 식사 및 취침 카테고리별 종료 멘트 데이터베이스 관리, 디바이스별 애니메이션 맵핑 (`KebbiAnimData`)
