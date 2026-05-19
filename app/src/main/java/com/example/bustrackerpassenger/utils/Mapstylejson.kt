package com.example.bustrackerpassenger.utils

/**
 * Google Maps custom style — Light, clean, minimal.
 * Sama seperti dashboard web admin.
 */
const val MAP_STYLE_JSON = """
[
  {"featureType":"all","elementType":"geometry","stylers":[{"color":"#f5f5f5"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#c9e9f6"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#9e9e9e"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#ffffff"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#d9d9d9"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#fef5e0"}]},
  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#f5d89f"}]},
  {"featureType":"road.arterial","elementType":"labels.text.fill","stylers":[{"color":"#757575"}]},
  {"featureType":"road.local","elementType":"labels.text.fill","stylers":[{"color":"#9e9e9e"}]},
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"transit","stylers":[{"visibility":"off"}]},
  {"featureType":"administrative.land_parcel","stylers":[{"visibility":"off"}]},
  {"featureType":"administrative.neighborhood","stylers":[{"visibility":"off"}]},
  {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#bdbdbd"}]},
  {"featureType":"landscape.man_made","elementType":"geometry.fill","stylers":[{"color":"#f0f0f0"}]},
  {"featureType":"landscape.natural","elementType":"geometry.fill","stylers":[{"color":"#e8f5e9"}]},
  {"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#616161"}]}
]
"""