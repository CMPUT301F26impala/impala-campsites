package com.impala.campala.ui_sections.lottery_details

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

class LotteryDetailsView {
    @Composable
    fun Render(navController: NavController, lotteryId: String){
        Button(
            onClick = { navController.navigate("waitlist/$lotteryId") }
        ) { Text("View Waitlist") }
    }
}