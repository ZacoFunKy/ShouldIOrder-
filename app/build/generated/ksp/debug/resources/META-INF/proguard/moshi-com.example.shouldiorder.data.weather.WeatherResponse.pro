-if class com.example.shouldiorder.data.weather.WeatherResponse
-keepnames class com.example.shouldiorder.data.weather.WeatherResponse
-if class com.example.shouldiorder.data.weather.WeatherResponse
-keep class com.example.shouldiorder.data.weather.WeatherResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
