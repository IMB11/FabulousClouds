Fabulous Clouds is a client-side 1.17 Fabric mod that makes minecraft's clouds look much better. It isn't going to be backported.

Fabulous Clouds is compatible with Sodium.

![](https://cdn.discordapp.com/attachments/870400219525832795/871180457717415946/2021-07-31_17.59.38.png)

Fabulous Clouds has 3 major features: vibrant clouds, customizable cloud layers, and procedurally generated cloud covers. Vibrant clouds make the clouds change color in sunrise/sunset similar to bedrock edition. Procedural cloud covers change how the clouds are shaped over time based on three presets: Clear, Normal, and Cloudy. Cloud layers be changed in the `.minecraft/config/fabulous-clouds` config file:

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
	"debug_noise_clouds": false
}
```

This example enables the default vanilla cloud layer and adds a cloud layer 64 blocks above normal cloud height. The negative speed means it will go in the opposite direction to the default cloud layer. Cloud layers all have an `offset`, `scale` and `speed`:

* `offset` is how many blocks above or below cloud height the layer is positioned. (default `0.0`)

* `scale` is how big the cloud layer is. (default `1.0`)

* `speed` is how fast the cloud layer moves. (default `1.0`)

You may add this mod to your modpack.
