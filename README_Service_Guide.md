# 로봇 서비스 사용법 안내 (Service Usage Guide)

## 개요

로봇(다솜)의 각종 서비스 기능을 어르신에게 **사용법을 안내·연습**시키는 기능입니다.
취침/식사 문답(`learn` 패키지, `MEAL_TYPE_SHOW`)과는 **별개**이며, `guide` 패키지에서 처리합니다.

## 진입 경로

```
Scene onCommand()
  → App.onCommand()  (GUIDE_TYPE_PARAM 설정)
  → MainActivity.startGuideService()
  → GuideFragment
```

| 상수 | 안내 내용 |
|------|-----------|
| `GUIDE_WAKEUP` | 대화 호출(웨이크워드) — "다솜아"로 말 거는 법 |
| `GUIDE_VISION` | 얼굴 인식·근접 대화 |
| `GUIDE_MEDICATION` | 복약 알림 |
| `GUIDE_COMMUNITY` | 친구 찾기 |
| `GUIDE_MONITORING` | 긴급콜·모니터링 |
| `GUIDE_MESSAGE` | 문자/전화 알림 |

## 주요 관련 파일

- `ui/guide/GuideFragment.kt` — 안내 화면 UI, 카메라(얼굴 인식 가이드)
- `ui/guide/GuideViewModel.kt` — TTS/STT 제어, 가이드 진행 상태
- `ui/guide/GuideStatus.kt` — 가이드 단계별 상태
- `repository/GuideRepository.kt` — 안내 멘트 데이터 (로컬)

## learn 패키지와의 구분

| | `guide` (본 문서) | `learn` (README_Sleep_Meal.md) |
|---|---|---|
| 목적 | 서비스 사용법 안내 | 취침/식사 등 일상 문답 |
| 진입 | `GUIDE_TYPE_PARAM` | `PARAM_PRAC_TYPE` (`Meal_show` 등) |
| 화면 | `GuideFragment` | `LearnFragment` |
