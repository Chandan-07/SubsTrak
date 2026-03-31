package com.tracker.subscription.presentation.Widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.subscription.R
import com.tracker.subscription.data.Option

@Composable
fun SingleSelectChips(
    label: String,
    selected: String,
    options: List<Option>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    Column(modifier = modifier) {

        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            modifier = Modifier.padding(bottom = 8.dp),
            fontFamily = manropeBold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->

                val isSelected = option.name == selected

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isSelected) Color(0xFF3D5AFE)
                            else Color(0xFFF5F5F5)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF3D5AFE)
                            else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { onSelected(option.name) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row {
                        Text(
                            text = option.emoji,
                            color = if (isSelected) Color.White else Color.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = option.name,
                            color = if (isSelected) Color.White else Color.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                }
            }
        }
    }
}