package com.example.toycamping.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.toycamping.BuildConfig
import com.example.toycamping.R
import com.example.toycamping.base.BaseFragment
import com.example.toycamping.base.ViewState
import com.example.toycamping.databinding.MapFragmentBinding
import com.example.toycamping.ext.hasPermission
import com.example.toycamping.viewmodel.HomeViewModel
import com.example.toycamping.viewmodel.MapViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : BaseFragment<MapFragmentBinding>(R.layout.map_fragment) {

    private val campingItemList = mutableSetOf<MapPOIItem>()

    private val mapViewModel by viewModel<MapViewModel>()

    private val homeViewModel by activityViewModels<HomeViewModel>()

    private lateinit var currentLocation: MapPOIItem

    private val mapViewEventListener =
        object : MapView.MapViewEventListener {
            override fun onMapViewInitialized(p0: MapView?) {

            }

            override fun onMapViewCenterPointMoved(p0: MapView?, p1: MapPoint?) {
                mapViewModel.currentCenterMapPoint.value = p1
            }

            override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
                mapViewModel.currentZoomLevel.value = p1
            }

            override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {

            }

            override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {

            }

            override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {

            }

            override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
                with(binding.containerPoiInfo) {
                    if (isVisible) {
                        isVisible = false
                        startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.slide_down
                            )
                        )
                    }
                }
            }

            override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {

            }

            override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {

            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationRequest()
        initViewModel()

    }

    private val poiItemEventListener = object : MapView.POIItemEventListener {
        override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
            p1?.let { item ->
                mapViewModel.getSelectPOIItemInfo(item.itemName)
                mapViewModel.checkBookmarkState(item.itemName)

                binding.itemBookmark.setOnClickListener {
                    mapViewModel.toggleBookmark(item.itemName, binding.itemBookmark.isChecked)
                }
            }
        }

        override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {

        }

        override fun onCalloutBalloonOfPOIItemTouched(
            p0: MapView?,
            p1: MapPOIItem?,
            p2: MapPOIItem.CalloutBalloonButtonType?
        ) {

        }

        override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {

        }
    }

    private fun initUi() {
        binding.containerMap.setMapViewEventListener(this@MapFragment.mapViewEventListener)
        binding.containerMap.setPOIItemEventListener(this@MapFragment.poiItemEventListener)

        mapViewModel.setCurrentLocation()
    }

    private fun initViewModel() {
        binding.viewModel = mapViewModel

        mapViewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState: ViewState? ->
            (viewState as? MapViewModel.MapViewState)?.let { onChangedMapViewState(viewState) }
        }

        homeViewModel.viewStateLiveData.observe(viewLifecycleOwner) { viewState: ViewState? ->
            (viewState as? HomeViewModel.HomeViewState)?.let { onChangedHomeViewState(viewState) }
        }
    }


    private fun setCurrentLocation(currentMapPoint: MapPoint) {
        if (::currentLocation.isInitialized) {
            binding.containerMap.removePOIItem(currentLocation)
        }

        currentLocation = MapPOIItem().apply {
            itemName = "Current Location!"
            mapPoint = currentMapPoint
        }

        with(binding.containerMap) {
            addPOIItem(currentLocation)
            setMapCenterPoint(currentMapPoint, false)
        }
    }

    private fun onChangedHomeViewState(viewState: ViewState) {

        when (viewState) {
            is HomeViewModel.HomeViewState.AddBookmark -> {
                binding.itemBookmark.isChecked = true
            }
            is HomeViewModel.HomeViewState.DeleteBookmark -> {
                binding.itemBookmark.isChecked = false
            }
        }

    }

    private fun onChangedMapViewState(viewState: ViewState) {
        when (viewState) {
            is MapViewModel.MapViewState.SetCurrentLocation -> {
                setCurrentLocation(viewState.mapPoint)
            }

            is MapViewModel.MapViewState.GetGoCampingLocationList -> {
                campingItemList.addAll(viewState.itemList)
                binding.containerMap.addPOIItems(viewState.itemList)
            }

            is MapViewModel.MapViewState.GetSearchList -> {
                GlobalScope.launch(Dispatchers.IO) {

                    val mapPOIItem = MapPOIItem().apply {
                        itemName = viewState.item.facltNm
                        mapPoint =
                            MapPoint.mapPointWithGeoCoord(viewState.item.mapY, viewState.item.mapX)
                        markerType = MapPOIItem.MarkerType.RedPin
                    }
                    withContext(Dispatchers.Main) {
                        binding.containerMap.addPOIItem(mapPOIItem)
                        binding.containerMap.setMapCenterPoint(mapPOIItem.mapPoint, true)
                    }
                }
            }

            is MapViewModel.MapViewState.SetZoomLevel -> {
                binding.containerMap.setZoomLevel(viewState.zoomLevel, true)
            }

            is MapViewModel.MapViewState.Error -> {
                Toast.makeText(requireContext(), viewState.errorMessage, Toast.LENGTH_SHORT).show()
            }

            is MapViewModel.MapViewState.GetSelectPOIItem -> {
                with(binding) {
                    containerPoiInfo.apply {
                        bringToFront()
                        isVisible = true
                        startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.slide_up
                            )
                        )
                    }
                    itemName.text = viewState.item.facltNm
                    itemLocation.text = viewState.item.addr1
                }
            }

            is MapViewModel.MapViewState.ShowProgress -> {
                binding.progressbar.bringToFront()
                binding.progressbar.isVisible = true
            }

            is MapViewModel.MapViewState.HideProgress -> {
                binding.progressbar.isVisible = false
            }

            is MapViewModel.MapViewState.BookmarkState -> {
                binding.itemBookmark.isChecked = viewState.isChecked
            }

            is MapViewModel.MapViewState.AddBookmark -> {
                homeViewModel.addBookmark(viewState.itemName)
            }

            is MapViewModel.MapViewState.DeleteBookmark -> {
                homeViewModel.deleteBookmark(viewState.itemName)
            }
        }
    }

    private fun locationRequest() {
        val permissionApproved =
            requireActivity().hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionApproved) {
            initUi()
        } else {
            val provideRationale = shouldShowRequestPermissionRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )

            if (provideRationale) {
                initUi()
            } else {
                requireActivity().requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {

            when {
                grantResults.isEmpty() -> {
                    Toast.makeText(requireContext(), "권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }

                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(requireContext(), "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts(
                        "package",
                        BuildConfig.APPLICATION_ID,
                        null
                    )
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
    }

}
