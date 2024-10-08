import { ViewStyle, StyleProp } from "react-native";

type ProgressEvent = {
  distanceRemaining: number;
  distanceTraveled: number;
  durationRemaining: number;
  fractionTraveled: number;
};

export type ExpoMapboxNavigationViewProps = {
  coordinates: { latitude: number; longitude: number }[];
  locale?: string;
  onRouteProgressChanged?: (event: { nativeEvent: ProgressEvent }) => void;
  onCancelNavigation?: () => void;
  onWaypointArrival?: (event: {
    nativeEvent: ProgressEvent | undefined;
  }) => void;
  onFinalDestinationArrival?: () => void;
  onRouteChanged?: (event: { nativeEvent: any }) => void;
  onUserOffRoute?: (event: { nativeEvent: any }) => void;
  onLocationChange?: (event: { nativeEvent: any }) => void;
  onRouteReady?: (event: { nativeEvent: any }) => void;
  style?: StyleProp<ViewStyle>;
};
