package expo.modules.mapboxnavigation

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.view.ViewOutlineProvider
import android.os.Build
import androidx.core.view.ViewCompat
import android.util.Log
import android.view.Gravity
import android.view.View;
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView;
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.DirectionsCriteria.ProfileCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.bindgen.Expected
import com.mapbox.common.location.Location
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.attribution.*
import com.mapbox.maps.plugin.logo.*
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.OffRouteObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.speedlimit.api.MapboxSpeedInfoApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateValue
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.components.maneuver.model.ManeuverPrimaryOptions
import com.mapbox.navigation.ui.components.maneuver.model.ManeuverViewOptions
import com.mapbox.navigation.ui.components.maneuver.view.MapboxManeuverView
import com.mapbox.navigation.ui.components.maps.camera.view.MapboxRecenterButton
import com.mapbox.navigation.ui.components.maps.camera.view.MapboxRouteOverviewButton
import com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.components.voice.view.MapboxSoundButton
import com.mapbox.navigation.ui.maps.camera.data.FollowingFrameOptions.FocalPoint
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.*
import com.mapbox.navigation.ui.maps.route.RouteLayerConstants.TOP_LEVEL_ROUTE_LINE_LAYER_ID
import com.mapbox.navigation.voice.api.*
import com.mapbox.navigation.voice.model.SpeechAnnouncement
import com.mapbox.navigation.voice.model.SpeechError
import com.mapbox.navigation.voice.model.SpeechValue
import com.mapbox.navigation.voice.model.SpeechVolume
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import expo.modules.mapboxnavigation.R
import java.util.Locale
import com.google.gson.Gson

val PIXEL_DENSITY = Resources.getSystem().displayMetrics.density

class ExpoMapboxNavigationView(context: Context, appContext: AppContext) : ExpoView(context, appContext){
    private var isMuted = false
    private var currentCoordinates: List<Point>? = null
    private var currentLocale = Locale.getDefault()
    private var currentRoutesRequestId: Long? = null
    private var currentMapStyle: String? = "mapbox://styles/voolt-admin/cm2krrib001c901pifactb1qg"
    private var distanceUnit: String = DirectionsCriteria.IMPERIAL

    private val onRouteProgressChanged by EventDispatcher()
    private val onCancelNavigation by EventDispatcher()
    private val onWaypointArrival by EventDispatcher()
    private val onNextRouteLegStart by EventDispatcher()
    private val onFinalDestinationArrival by EventDispatcher()
    private val onRouteChanged by EventDispatcher()
    private val onUserOffRoute by EventDispatcher()
    private val onLocationChange by EventDispatcher()
    private val onRouteReady by EventDispatcher()

    private val mapboxNavigation = MapboxNavigationApp.current()
    private var mapboxStyle: Style? = null
    private val navigationLocationProvider = NavigationLocationProvider()
    private var voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(context, currentLocale.toLanguageTag())

    private val parentConstraintLayout = ConstraintLayout(context).also {
        addView(it, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))   
    }

    private val mapViewId = 1
    private val mapView = createMapView(mapViewId, parentConstraintLayout)
    private val mapboxMap = mapView.mapboxMap   

    private val viewportDataSource = createViewportDataSource(mapboxMap)
    private val navigationCamera = NavigationCamera(mapboxMap, mapView.camera, viewportDataSource).apply{
        mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(this)
        )
    }

    private val maneuverViewId = 2
    private val maneuverView = createManueverView(maneuverViewId, parentConstraintLayout, context)


    private val soundButtonId = 4
    private val soundButton = createSoundButton(soundButtonId, parentConstraintLayout){
        voiceInstructionsPlayer.volume(SpeechVolume(if(isMuted) 1.0f else 0.0f))
        it.findViewById<ImageView>(com.mapbox.navigation.ui.components.R.id.buttonIcon).setImageResource(if(isMuted) R.drawable.icon_sound else R.drawable.icon_mute)
        isMuted = !isMuted
    }

    private val overviewButtonId = 5
    private val overviewButton = createOverviewButton(overviewButtonId, parentConstraintLayout){
        navigationCamera.requestNavigationCameraToOverview()
    } 
    
    private val recenterButtonId = 6
    private val recenterButton = createRecenterButton(recenterButtonId, parentConstraintLayout){
        navigationCamera.requestNavigationCameraToFollowing()
    }

    private val parentConstraintSet = createAndApplyConstraintSet(
        mapViewId=mapViewId,
        maneuverViewId=maneuverViewId,
        soundButtonId=soundButtonId,
        overviewButtonId=overviewButtonId,
        recenterButtonId=recenterButtonId,
        constraintLayout=parentConstraintLayout
    )

    private val routeLineApiOptions = MapboxRouteLineApiOptions.Builder().build()
    private val routeLineApi = MapboxRouteLineApi(routeLineApiOptions)

    private val routeLineViewOptions = MapboxRouteLineViewOptions.Builder(context)
        .routeLineBelowLayerId("road-label-navigation")
        .destinationWaypointIcon(R.drawable.waypoint_icon)
        .build()
    private val routeLineView = MapboxRouteLineView(routeLineViewOptions)

    private val routeArrow = MapboxRouteArrowApi()
    private val routeArrowOptions = RouteArrowOptions.Builder(context)
        .withAboveLayerId(TOP_LEVEL_ROUTE_LINE_LAYER_ID)
        .build()
        
    private val routeArrowView = MapboxRouteArrowView(routeArrowOptions)

    val unitType = if (distanceUnit == "imperial") UnitType.IMPERIAL else UnitType.METRIC
    private val distanceFormatter = DistanceFormatterOptions.Builder(context).unitType(unitType).build()

    private var maneuverApi = MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatter))

    private var tripProgressFormatter = TripProgressUpdateFormatter.Builder(context)
			.distanceRemainingFormatter(DistanceRemainingFormatter(distanceFormatter))
	      	.timeRemainingFormatter(TimeRemainingFormatter(context))
			.estimatedTimeToArrivalFormatter(EstimatedTimeToArrivalFormatter(context))
			.build()
    private var tripProgressApi = MapboxTripProgressApi(tripProgressFormatter)

    private var speechApi = MapboxSpeechApi(context, currentLocale.toLanguageTag())
    private val voiceInstructionsPlayerCallback = MapboxNavigationConsumer<SpeechAnnouncement> { value ->
        speechApi.clean(value)
    }

    private val speechCallback = MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
        expected.fold(
            { error ->
                voiceInstructionsPlayer.play(
                    error.fallback,
                    voiceInstructionsPlayerCallback
                )
            },
            { value ->
                voiceInstructionsPlayer.play(
                    value.announcement,
                    voiceInstructionsPlayerCallback
                )
            }
        )
    }
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(
            voiceInstructions,
            speechCallback
        )
    }

    private var pointAnnotationManager: PointAnnotationManager? = null

    private val routesRequestCallback = object : NavigationRouterCallback {
        override fun onRoutesReady(routes: List<NavigationRoute>, @RouterOrigin routerOrigin: String) {
            onRoutesReady(routes)
        }
        override fun onCanceled(routeOptions: RouteOptions, @RouterOrigin routerOrigin: String) {}
        override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {}
    }

    private val routesObserver = object : RoutesObserver {
        override fun onRoutesChanged(result: RoutesUpdatedResult) {
            // Handle viewport data source
            if (result.navigationRoutes.isNotEmpty()) {
                viewportDataSource.onRouteChanged(result.navigationRoutes.first())
                viewportDataSource.evaluate()
            } else {
                viewportDataSource.clearRouteData()
                viewportDataSource.evaluate()
            }

            // Handle route lines
            val alternativesMetadata = mapboxNavigation?.getAlternativeMetadataFor(result.navigationRoutes)
            if(alternativesMetadata != null){
                routeLineApi.setNavigationRoutes(result.navigationRoutes, alternativesMetadata) { value ->
                    mapboxStyle?.let { routeLineView.renderRouteDrawData(it, value) }
                }
            }

            // Clear speech
            speechApi.cancel()
            voiceInstructionsPlayer.clear()

            // Add observer to navigation camera
            navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
                // shows/hide the recenter button depending on the camera state
                when (navigationCameraState) {
                    NavigationCameraState.TRANSITION_TO_FOLLOWING,
                    NavigationCameraState.FOLLOWING -> recenterButton.visibility = View.GONE
                    NavigationCameraState.TRANSITION_TO_OVERVIEW,
                    NavigationCameraState.OVERVIEW,
                    NavigationCameraState.IDLE -> recenterButton.visibility = View.VISIBLE
                }
            }

            val gson = Gson()
            val routeJson = gson.toJson(result.navigationRoutes.first())
            this@ExpoMapboxNavigationView.onRouteChanged(mapOf(
                "route" to routeJson
            ))
        }
    }

    private val routeProgressObserver = object : RouteProgressObserver {
        override fun onRouteProgressChanged(routeProgress: RouteProgress) {
            // Handle viewport data source
            viewportDataSource.onRouteProgressChanged(routeProgress)
            viewportDataSource.evaluate()

            // Handle route lines
            routeLineApi.updateWithRouteProgress(routeProgress) { result ->
                mapboxStyle?.let { routeLineView.renderRouteLineUpdate(it, result) }
            }

            // Handle route arrows
            val updatedManeuverArrow = routeArrow.addUpcomingManeuverArrow(routeProgress)
		    mapboxStyle?.let { routeArrowView.renderManeuverUpdate(it, updatedManeuverArrow) }

            // Handle manuevers
            val maneuvers = maneuverApi.getManeuvers(routeProgress)
            maneuverView.renderManeuvers(maneuvers)

            val stopDistanceTraveled = routeProgress.currentLegProgress?.distanceTraveled?.toDouble() ?: 0.0
            val stopDurationRemaining = routeProgress.currentLegProgress?.durationRemaining?.toDouble() ?: 0.0
            val stopDistanceRemaining = routeProgress.currentLegProgress?.distanceRemaining?.toDouble() ?: 0.0
            val stopIndex = routeProgress.currentLegProgress?.legIndex?.toDouble() ?: 0.0

            // Create a mutable map to store the progress data
            val currentLegProgress = mutableMapOf<String, Double>()
            currentLegProgress["stopIndex"] = stopIndex
            currentLegProgress["stopDistanceTraveled"] = stopDistanceTraveled
            currentLegProgress["stopDurationRemaining"] = stopDurationRemaining
            currentLegProgress["stopDistanceRemaining"] = stopDistanceRemaining

            // Send progress event
            this@ExpoMapboxNavigationView.onRouteProgressChanged(mapOf(
                "distanceRemaining" to routeProgress.distanceRemaining,
                "distanceTraveled" to routeProgress.distanceTraveled,
                "durationRemaining" to routeProgress.durationRemaining,
                "fractionTraveled" to routeProgress.fractionTraveled,
                "currentLegProgress" to currentLegProgress,
            ))
        }
    }

    private val speedInfoApi: MapboxSpeedInfoApi by lazy {
        MapboxSpeedInfoApi()
    }

    private val locationObserver = object : LocationObserver {

        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: com.mapbox.common.location.Location) {
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation

            // Update puck location
             navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                    .maxDuration(0) // instant transition
                    .build()
                )
            }
            
            val speedLimitInfo = locationMatcherResult.speedLimitInfo

            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            val driverLocation = mutableMapOf<String, Any?>()
            driverLocation["longitude"] = enhancedLocation.longitude
            driverLocation["latitude"] = enhancedLocation.latitude
            driverLocation["speed"] = enhancedLocation.speed
            driverLocation["speedAccuracy"] = enhancedLocation.speedAccuracy

            val speedLimitData = mutableMapOf<String, Any?>()
            speedLimitData["speed"] = speedLimitInfo?.speed
            speedLimitData["unit"] = speedLimitInfo?.unit
            speedLimitData["sign"] = speedLimitInfo?.sign

            this@ExpoMapboxNavigationView.onLocationChange(mapOf(
                "driverLocation" to driverLocation,
                "speedLimitData" to speedLimitData
            ))

        }
    }

    private val arrivalObserver = object : ArrivalObserver {
        override fun onWaypointArrival(routeProgress: RouteProgress) {
            onWaypointArrival(mapOf(
                "waypointArrival" to true
            ))
        }
        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            onNextRouteLegStart(mapOf(
                "nextRouteLegStart" to true
            ))
        }
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            onFinalDestinationArrival(mapOf(
                "finalDestinationArrival" to true
            ))
        }
    }

    private val offRouteObserver = object : OffRouteObserver {
        override fun onOffRouteStateChanged(offRoute: Boolean) {
            if(offRoute){
                //onUserOffRoute(mapOf(
                    //"offRoute" to offRoute
                //))
            }
        }
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        val result = routeLineApi.updateTraveledRouteLine(point)
        mapboxStyle?.let { routeLineView.renderRouteLineUpdate(it, result) }
    }

    private fun createMapView(id: Int, parent: ViewGroup): MapView {
        return MapView(context).apply {
            setId(id)
            parent.addView(this)

            mapboxMap.loadStyleUri("mapbox://styles/voolt-admin/cm2krrib001c901pifactb1qg") { style: Style ->
                mapboxStyle = style
                routeLineView.initializeLayers(style)
            }

            location.apply {
                locationPuck = LocationPuck2D(
                    bearingImage = ImageHolder.from(R.drawable.mapbox_navigation_puck_icon),
                )
                setLocationProvider(navigationLocationProvider)
                puckBearingEnabled = true
                enabled = true
            }    
        }
    }

    fun createManueverView(id: Int, parent: ViewGroup, context: Context): MapboxManeuverView {
        return MapboxManeuverView(context).apply {
            setId(id)
            parent.addView(this)
            
            val backgroundDrawable = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(ContextCompat.getColor(context, R.color.maneuver_view_background))
                cornerRadius = 17f * context.resources.displayMetrics.density
            }

            background = backgroundDrawable
            
            val padding = (3 * context.resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
            
            val maneuverViewOptions = ManeuverViewOptions.Builder()
                .primaryManeuverOptions(
                    ManeuverPrimaryOptions.Builder()
                        .textAppearance(R.style.ManeuverTextAppearance)
                        .build()
                )
                .maneuverBackgroundColor(R.color.maneuver_view_background)
                .stepDistanceTextAppearance(R.style.StepDistanceTextAppearance)
                .turnIconManeuver(R.style.MapboxCustomManeuverTurnIconStyle)
                .build()
            
            updateManeuverViewOptions(maneuverViewOptions)

            // Add elevation for shadow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = 9f * context.resources.displayMetrics.density
                outlineProvider = ViewOutlineProvider.BACKGROUND
                clipToOutline = true
            } else {
                // For pre-Lollipop devices, use ViewCompat
                ViewCompat.setElevation(this, 9f * context.resources.displayMetrics.density)
            }
        }
    }

    private fun createCenteredTextView(): TextView {
        return TextView(context).apply {
            setGravity(Gravity.CENTER)
        }
    }

    private fun createSoundButton(id: Int, parent: ViewGroup, onClick: (MapboxSoundButton) -> Unit): MapboxSoundButton {
        return MapboxSoundButton(context).apply {
            setId(id)
            parent.addView(this)
            val buttonIcon = findViewById<ImageView>(com.mapbox.navigation.ui.components.R.id.buttonIcon).setImageResource(R.drawable.icon_sound)
            setOnClickListener {
                onClick(this)
            }
            // Add elevation for shadow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = 4f * context.resources.displayMetrics.density
                outlineProvider = ViewOutlineProvider.BACKGROUND
                clipToOutline = true
            } else {
                // For pre-Lollipop devices, use ViewCompat
                ViewCompat.setElevation(this, 4f * context.resources.displayMetrics.density)
            }

            // Optional: Add a background if the button doesn't have one
            if (background == null) {
                setBackgroundResource(R.drawable.button_background)
            }
        }
    }

    private fun createOverviewButton(id: Int, parent: ViewGroup, onClick: () -> Unit): MapboxRouteOverviewButton {
        return MapboxRouteOverviewButton(context).apply {
            setId(id)
            parent.addView(this)
            findViewById<ImageView>(com.mapbox.navigation.ui.components.R.id.buttonIcon).setImageResource(R.drawable.icon_overview)
            setOnClickListener {
                onClick()
            }
            // Add elevation for shadow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = 4f * context.resources.displayMetrics.density
                outlineProvider = ViewOutlineProvider.BACKGROUND
                clipToOutline = true
            } else {
                // For pre-Lollipop devices, use ViewCompat
                ViewCompat.setElevation(this, 4f * context.resources.displayMetrics.density)
            }

            // Optional: Add a background if the button doesn't have one
            if (background == null) {
                setBackgroundResource(R.drawable.button_background)
            }
        }
    }

    private fun createRecenterButton(id: Int, parent: ViewGroup, onClick: ()->Unit): MapboxRecenterButton {
        return MapboxRecenterButton(context).apply {
            setId(id)
            findViewById<ImageView>(com.mapbox.navigation.ui.components.R.id.buttonIcon).setImageResource(R.drawable.icon_compass)
            parent.addView(this)
            setVisibility(View.GONE)
            setOnClickListener {
                onClick()
            }
            // Add elevation for shadow
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = 4f * context.resources.displayMetrics.density
                outlineProvider = ViewOutlineProvider.BACKGROUND
                clipToOutline = true
            } else {
                // For pre-Lollipop devices, use ViewCompat
                ViewCompat.setElevation(this, 4f * context.resources.displayMetrics.density)
            }

            // Optional: Add a background if the button doesn't have one
            if (background == null) {
                setBackgroundResource(R.drawable.button_background)
            }
        }
    }


    private fun createAndApplyConstraintSet(
        mapViewId: Int, 
        maneuverViewId: Int, 
        soundButtonId: Int,
        overviewButtonId: Int,
        recenterButtonId: Int,
        constraintLayout: ConstraintLayout
    ): ConstraintSet {
        return ConstraintSet().apply{
            // Add MapView constraints
            connect(mapViewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(mapViewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            connect(mapViewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(mapViewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            setMargin(mapViewId, ConstraintSet.BOTTOM, (90 * PIXEL_DENSITY).toInt())

            // Add ManeuverView constraints
            connect(maneuverViewId, ConstraintSet.TOP, mapViewId, ConstraintSet.TOP, (25 * PIXEL_DENSITY).toInt())
            connect(maneuverViewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, (12 * PIXEL_DENSITY).toInt())
            connect(maneuverViewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (12 * PIXEL_DENSITY).toInt())
            constrainHeight(maneuverViewId, ConstraintSet.WRAP_CONTENT)


            // Add SoundButton constraints
            connect(soundButtonId, ConstraintSet.TOP, maneuverViewId, ConstraintSet.BOTTOM, (8 * PIXEL_DENSITY).toInt())
            connect(soundButtonId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (16 * PIXEL_DENSITY).toInt())
            constrainWidth(soundButtonId, ConstraintSet.WRAP_CONTENT)
            constrainHeight(soundButtonId, ConstraintSet.WRAP_CONTENT)

            // Add OverviewButton constraints
            connect(overviewButtonId, ConstraintSet.TOP, soundButtonId, ConstraintSet.BOTTOM, (8 * PIXEL_DENSITY).toInt())
            connect(overviewButtonId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (16 * PIXEL_DENSITY).toInt())
            constrainWidth(overviewButtonId, ConstraintSet.WRAP_CONTENT)
            constrainHeight(overviewButtonId, ConstraintSet.WRAP_CONTENT)

            // Add RecenterButton constraints
            connect(recenterButtonId, ConstraintSet.TOP, overviewButtonId, ConstraintSet.BOTTOM, (8 * PIXEL_DENSITY).toInt())
            connect(recenterButtonId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, (16 * PIXEL_DENSITY).toInt())
            constrainWidth(recenterButtonId, ConstraintSet.WRAP_CONTENT)
            constrainHeight(recenterButtonId, ConstraintSet.WRAP_CONTENT)

            applyTo(constraintLayout)
        }
    }

    private fun createViewportDataSource(mapboxMap: MapboxMap): MapboxNavigationViewportDataSource{
         val portraitOverviewPadding = EdgeInsets(140.0 * PIXEL_DENSITY, 40.0 * PIXEL_DENSITY, 120.0 * PIXEL_DENSITY, 40.0 * PIXEL_DENSITY)
         val landscapeOverviewPadding = EdgeInsets(30.0 * PIXEL_DENSITY, 380.0 * PIXEL_DENSITY, 110.0 * PIXEL_DENSITY, 20.0 * PIXEL_DENSITY)
         val portraitFollowingPadding = EdgeInsets(180.0 * PIXEL_DENSITY, 40.0 * PIXEL_DENSITY, 150.0 * PIXEL_DENSITY, 40.0 * PIXEL_DENSITY)
         val landscapeFollowingPadding = EdgeInsets(30.0 * PIXEL_DENSITY, 380.0 * PIXEL_DENSITY, 110.0 * PIXEL_DENSITY, 40.0 * PIXEL_DENSITY)

        return MapboxNavigationViewportDataSource(mapboxMap).apply {
            options.followingFrameOptions.focalPoint = FocalPoint(0.5, 0.9)
            options.followingFrameOptions.centerUpdatesAllowed= true
            options.followingFrameOptions.bearingUpdatesAllowed = true
            options.followingFrameOptions.bearingSmoothing.enabled = true
            options.followingFrameOptions.defaultPitch = 45.0
            options.followingFrameOptions.maxZoom = 18.0
            options.followingFrameOptions.minZoom = 15.0
            options.followingFrameOptions.pitchUpdatesAllowed = true
            options.followingFrameOptions.zoomUpdatesAllowed = true
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                followingPadding = landscapeFollowingPadding
                overviewPadding = landscapeOverviewPadding
            } else {
                followingPadding = portraitFollowingPadding
                overviewPadding = portraitOverviewPadding
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mapboxNavigation?.registerRoutesObserver(routesObserver)
        mapboxNavigation?.registerRouteProgressObserver(routeProgressObserver)
        mapboxNavigation?.registerLocationObserver(locationObserver)
        mapboxNavigation?.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation?.registerArrivalObserver(arrivalObserver)
        mapboxNavigation?.registerOffRouteObserver(offRouteObserver)
        mapView.location.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.logo.updateSettings {
            enabled = false
        }
        mapView.attribution.updateSettings {
            enabled = false
        }
        mapView.compass.enabled = false
        mapView.scalebar.enabled = false
        mapboxNavigation?.startTripSession()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mapboxNavigation?.unregisterRoutesObserver(routesObserver)
        mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation?.unregisterLocationObserver(locationObserver)
        mapboxNavigation?.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
        mapboxNavigation?.unregisterArrivalObserver(arrivalObserver)
        mapboxNavigation?.unregisterOffRouteObserver(offRouteObserver)
        if(mapboxNavigation?.isDestroyed != true){
            mapboxNavigation?.stopTripSession()
        }
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        mapView.location.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        routeLineApi.cancel()
        routeLineView.cancel()
        maneuverApi.cancel()
    }

    private fun onRoutesReady(routes: List<NavigationRoute>){
        mapboxNavigation?.setNavigationRoutes(routes)
        navigationCamera.requestNavigationCameraToFollowing(
            stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                .maxDuration(0)
                .build()
        )
        val gson = Gson()
        val routeJson = gson.toJson(routes[0])
        this@ExpoMapboxNavigationView.onRouteReady(mapOf(
            "route" to routeJson
        ))

    }

    @com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI 
    fun setCoordinates(coordinates: List<Point>){
        currentCoordinates = coordinates
        update();
    }

    @com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
    fun setLocale(localeStr: String?){
        currentLocale = if (localeStr == null || localeStr == "default") Locale.getDefault() else Locale.Builder().setLanguageTag(localeStr).build()
        update()
    }

    @com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
    private fun update(){
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(context, currentLocale.toLanguageTag())
        speechApi = MapboxSpeechApi(context, currentLocale.toLanguageTag())

        if(currentMapStyle != null){
            mapboxMap.loadStyle(currentMapStyle!!) { style: Style ->
                mapboxStyle = style
                style.localizeLabels(currentLocale)  
            }
        } else {
            mapboxMap.getStyle { style: Style ->
                style.localizeLabels(currentLocale)  
            }
        }
        val unitType = if (distanceUnit == "imperial") UnitType.IMPERIAL else UnitType.METRIC
        val distanceFormatter = DistanceFormatterOptions.Builder(context).locale(currentLocale).unitType(unitType).build()
        maneuverApi = MapboxManeuverApi(MapboxDistanceFormatter(distanceFormatter))
        requestRoutes()
    }

    private fun requestRoutes(){
        var optionsBuilder = RouteOptions.builder()
                                .applyDefaultNavigationOptions()
                                .coordinatesList(currentCoordinates!!)
                                .steps(true)
                                .voiceInstructions(true)
                                .language(currentLocale.toLanguageTag())
                                .profile(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC)
                                .annotationsList(listOf(DirectionsCriteria.ANNOTATION_MAXSPEED)) 
                                .roundaboutExits(true)
                                .alternatives(false)
                                .overview("full")
                                .voiceUnits(distanceUnit)

        currentRoutesRequestId = mapboxNavigation?.requestRoutes(
                optionsBuilder.build(),
                routesRequestCallback
            )
    }
}
