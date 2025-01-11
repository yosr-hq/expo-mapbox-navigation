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
  onRouteProgressChanged?: (event: { nativeEvent: any }) => void;
  onCancelNavigation?: () => void;
  onWaypointArrival?: (event: { nativeEvent: any }) => void;
  onNextRouteLegStart?: (event: { nativeEvent: any }) => void;
  onFinalDestinationArrival?: (event: { nativeEvent: any }) => void;
  onRouteChanged?: (event: { nativeEvent: any }) => void;
  onUserOffRoute?: (event: { nativeEvent: any }) => void;
  onLocationChange?: (event: { nativeEvent: any }) => void;
  onRouteReady?: (event: { nativeEvent: any }) => void;
  style?: StyleProp<ViewStyle>;
};
