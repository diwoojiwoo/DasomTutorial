package com.onethefull.dasomtutorial.ui.meal

/**
 * Created by jeaseok on 2022/02/22
 */
enum class MealStatus {
    START,

    MEAL_INIT,
    MEAL_GUIDE_TIME,
    MEAL_GUIDE_FOOD,

    MEAL_GUIDE_SERVICE,
    MEAL_GUIDE_RETRY,
    MEAL_GUIDE_FINISH,

    END
}