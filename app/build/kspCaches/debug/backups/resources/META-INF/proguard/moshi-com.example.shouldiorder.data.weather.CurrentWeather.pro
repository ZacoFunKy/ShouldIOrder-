-if class com.example.shouldiorder.data.weather.CurrentWeather
-keepnames class com.example.shouldiorder.data.weather.CurrentWeather
-if class com.example.shouldiorder.data.weather.CurrentWeather
-keep class com.example.shouldiorder.data.weather.CurrentWeatherJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
