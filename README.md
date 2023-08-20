___

<p align="center" style="margin-left: 20px;">
  <img src="https://cdn.discordapp.com/attachments/870400219525832795/880161415632150558/icon.png" width="256" height="256" style="display: block;margin-left: auto;margin-right: auto;"/>
</p>

___

## Fabulous Clouds

Fabulous Clouds is my first mod. The concept for Fabulous Clouds came when I got bedrock edition and realised that the
clouds tinted during sunrise and sunset. I wanted to add this ingame, but I also wanted to do more with clouds than just
this so I decided to add cloud layers too, and give people the ability to add their own cloud layers. Fabulous Clouds is
entirely client side so you could use it on a server without everyone having to download the mod. Fabulous Clouds is
compatible with Sodium.

![](https://cdn.discordapp.com/attachments/507982666755473427/875202883908435968/2021-08-11_20.17.17.png)

Fabulous Clouds has 3 major features: vibrant clouds, customizable cloud layers, and procedurally generated cloud
covers. Vibrant clouds make the clouds change color in sunrise/sunset similar to bedrock edition. Procedural cloud
covers change how the clouds are shaped over time based on three presets: Clear, Normal, and Cloudy. Cloud layers be
changed in the `.minecraft/config/fabulous-clouds` config file:

```json
{
  "vibrant_clouds": true,
  "vibrance_intensity": 1.0,
  "enable_default_cloud_layer": true,
  "cloud_layers": [
    {
      "offset": 64.0,
      "scale": 1.25,
      "speed": -1.0
    }
  ]
  "noise_clouds": true,
  "debug_noise_clouds": false,
  "offset_cloud_rendering": false
}
```

This example enables the default vanilla cloud layer and adds a cloud layer 64 blocks above normal cloud height. The
negative speed means it will go in the opposite direction to the default cloud layer. Cloud layers all have
an `offset`, `scale` and `speed`:

* `offset` is how many blocks above or below cloud height the layer is positioned. (default `0.0`)

* `scale` is how big the cloud layer is. (default `1.0`)

* `speed` is how fast the cloud layer moves. (default `1.0`)

You can find more information about the config in the [wiki](https://github.com/misterslime/fabulousclouds-fabric/wiki).

### Building/Contributing.

#### Building

1. Clone the repository.
2. Run `gradlew build` in the project folder.

#### Contributing

1. Clone the repository.

###### IntelliJ Idea

1. Open IntelliJ IDEA.
2. Press Open.
3. Choose the `build.gradle` file and open it as a project.