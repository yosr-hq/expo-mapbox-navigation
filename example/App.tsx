import * as Location from "expo-location";
import { ExpoMapboxNavigationView } from "expo-mapbox-navigation";
import { StatusBar } from "expo-status-bar";
import React, { useState } from "react";
import { StyleSheet, View, Text } from "react-native";

export default function App() {
  const [locationAllowed, setLocationAllowed] = React.useState(false);
  const [coordinates, setCoordinates] = React.useState([
    { latitude: 33.5873, longitude: -7.5902 },
    { latitude: 33.5939, longitude: -7.58189 },
    { latitude: 33.5957, longitude: -7.56383 },
    { latitude: 33.5985, longitude: -7.54626 },
  ]);
  const [speedLimit, setSpeedLimit] = useState<any>(0);
  const [speed, setSpeed] = useState<any>(0);
  const [speedColor, setSpeedColor] = useState<any>("#1f2937");
  React.useEffect(() => {
    (async () => {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status === "granted") {
        setLocationAllowed(true);
      } else {
        setLocationAllowed(false);
      }
    })();
  }, []);

  const updateLocation = (event: any) => {
    const speedInKph = event.driverLocation.speed * 3.6;
    setSpeed(Number(speedInKph));
    const speedLimitValue = event.speedLimitData.speed;
    if (speedLimitValue) {
      setSpeedLimit(speedLimitValue);
      if (speedInKph > speedLimitValue) {
        setSpeedColor("#FF375E");
      } else {
        setSpeedColor("#1f2937");
      }
    } else {
      setSpeedLimit(40);
      if (speedInKph > 40) {
        setSpeedColor("#FF375E");
      } else {
        setSpeedColor("#1f2937");
      }
    }
  };

  return (
    <View style={styles.container}>
      {locationAllowed ? (
        <>
          <ExpoMapboxNavigationView
            style={{ flex: 1 }}
            coordinates={coordinates}
            onRouteProgressChanged={(event: any) => {
              console.log(event.nativeEvent);
            }}
            onLocationChange={(event: any) => {
              updateLocation(event.nativeEvent);
            }}
            onRouteChanged={(event: any) => {
              const route = JSON.parse(event.nativeEvent.route);
              const directionsRoute = route.directionsRoute;
              console.log("routeChanged");
            }}
            onWaypointArrival={(event: any) => {
              console.log("waypointArrival", event.nativeEvent);
            }}
            onNextRouteLegStart={(event: any) => {
              console.log("nextRouteLegStart", event.nativeEvent);
            }}
            onFinalDestinationArrival={(event: any) => {
              console.log("finalDestinationArrival", event.nativeEvent);
            }}
          />

          <View
            style={{
              backgroundColor: "#fff",
              width: 60,
              height: 110,
              position: "absolute",
              zIndex: 1000,
              left: 10,
              top: "19%",
              shadowColor: "#444",
              elevation: 15,
              borderRadius: 100,
              alignContent: "center",
              alignItems: "center",
            }}
          >
            <View
              style={{
                backgroundColor: "#FF375E",
                width: 60,
                height: 60,
                borderRadius: 100,
                borderColor: "#fff",
                borderWidth: 1.5,
                justifyContent: "center",
                alignContent: "center",
                alignItems: "center",
              }}
            >
              <View
                style={{
                  backgroundColor: "#fff",
                  width: 49,
                  height: 48,
                  borderRadius: 100,
                  justifyContent: "center",
                  alignContent: "center",
                  alignItems: "center",
                }}
              >
                <Text
                  style={{
                    fontSize: 22,
                    fontWeight: 700,
                    color: "#1f2937",
                  }}
                >
                  {speedLimit || 0}
                </Text>
              </View>
            </View>
            <View
              style={{
                backgroundColor: "#fff",
                width: 40,
                height: 40,
                borderRadius: 100,
                justifyContent: "center",
                alignContent: "center",
                alignItems: "center",
              }}
            >
              <Text
                style={{
                  fontSize: 20,
                  fontWeight: 900,
                  color: speedColor,
                }}
              >
                {Number(speed).toFixed(0)}
              </Text>
              <Text
                style={{
                  fontSize: 12.5,
                  fontWeight: 300,
                  marginTop: -3,
                  color: speedColor,
                }}
              >
                Km/h
              </Text>
            </View>
          </View>
        </>
      ) : (
        <Text style={styles.text}>Location required for mapbox navigation</Text>
      )}

      <StatusBar style="dark" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
  },
  text: {
    alignSelf: "center",
  },
});
