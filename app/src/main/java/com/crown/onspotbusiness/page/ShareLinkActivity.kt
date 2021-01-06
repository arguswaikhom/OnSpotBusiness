package com.crown.onspotbusiness.page

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crown.library.onspotlibrary.controller.OSPreferences
import com.crown.library.onspotlibrary.model.business.BusinessV0
import com.crown.library.onspotlibrary.utils.OSCommonIntents
import com.crown.library.onspotlibrary.utils.OSInAppUrlUtils
import com.crown.library.onspotlibrary.utils.emun.OSPreferenceKey
import com.crown.onspotbusiness.databinding.ActivityShareLinkBinding

class ShareLinkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShareLinkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val business = OSPreferences.getInstance(this).getObject(OSPreferenceKey.BUSINESS, BusinessV0::class.java)
        binding.businessProfileOpi.setOnClickListener {
            OSCommonIntents.onIntentShareText(this, OSInAppUrlUtils.getBusinessUrl(business?.businessRefId))
        }
        binding.shoppingPageOpi.setOnClickListener {
            OSCommonIntents.onIntentShareText(this, OSInAppUrlUtils.getBusinessOrderOnlineUrl(business?.businessRefId))
        }
        binding.thisAppOpi.setOnClickListener {
            OSCommonIntents.onIntentShareAppLink(this)
        }
    }
}
