package com.example.toasty.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.toasty.arcore.LoadArDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.BaseArFragment

class CustomArFragment : ArFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val frameLayout : FrameLayout = super.onCreateView(inflater, container, savedInstanceState) as FrameLayout
        //planeDiscoveryController.hide()
        //planeDiscoveryController.setInstructionView(null)
        return frameLayout
    }

        override fun getSessionConfiguration(session: Session): Config {
            val config: Config = Config(session)
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE)
            config.setFocusMode(Config.FocusMode.AUTO)
            session.configure(config)
            (activity as LoadArDatabase).loadDB(session, config)
            //(activity as LoadArDatabase).loadDatabase(session, config)
            this.arSceneView.setupSession(session)
            return config
        }
}