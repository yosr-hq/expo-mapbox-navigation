package expo.modules.mapboxnavigation

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.geojson.Point
import androidx.lifecycle.LifecycleOwner
import expo.modules.kotlin.jni.JavaScriptValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExpoMapboxNavigationModule : Module() {

 private val activity get() = requireNotNull(appContext.activityProvider?.currentActivity)

  @com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
  override fun definition() = ModuleDefinition {
    Name("ExpoMapboxNavigation")

    OnActivityEntersForeground {
      (activity as LifecycleOwner).lifecycleScope.launch(Dispatchers.Main){
        if (!MapboxNavigationApp.isSetup()) {
              MapboxNavigationApp.setup {
                NavigationOptions.Builder(activity.applicationContext).build()
              } 
        }
        MapboxNavigationApp.attach(activity as LifecycleOwner)
      }
    }

    OnActivityEntersBackground {
      (activity as LifecycleOwner).lifecycleScope.launch(Dispatchers.Main){
        MapboxNavigationApp.detach(activity as LifecycleOwner)
      }
    }

    View(ExpoMapboxNavigationView::class) {
        Events("onRouteProgressChanged", "onCancelNavigation", "onWaypointArrival", "onFinalDestinationArrival", "onRouteChanged", "onUserOffRoute", "onLocationChange", "onRouteReady")

        Prop("coordinates") { view: ExpoMapboxNavigationView, coordinates: List<Map<String, Any>> ->
            val points = mutableListOf<Point>()
            for (coordinate in coordinates) {
                val longValue = coordinate.get("longitude")
                val latValue = coordinate.get("latitude")
                if(longValue is Double && latValue is Double){
                    points.add(Point.fromLngLat(longValue, latValue))
                }
                
            }
            view.setCoordinates(points)
        }
        
        Prop("locale") { view: ExpoMapboxNavigationView, localeStr: String? ->
            view.setLocale(localeStr)
        }
    }

  }
}
