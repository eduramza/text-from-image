package com.eduramza.cameratextconversor.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView


private const val TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"
private const val PROD_BANNER = "ca-app-pub-8606072529117264/6070486610"
@Composable
fun AdmobBanner(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = TEST_BANNER
                loadAd(AdRequest.Builder().build())
            }
        })
}