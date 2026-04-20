package com.tracker.subscription.screens.home

import com.tracker.subscription.data.DashboardData

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
}