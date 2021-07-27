Fabulous Clouds is a 1.17 Fabric mod that makes minecraft's clouds look much better. It isn't going to be ported to Forge or be backported.

![](https://cdn.discordapp.com/attachments/750122839738286177/869395008262897694/Fabulous_Clouds.png)

Fabulous Clouds has 2 major features: vibrant clouds and cloud layers. Vibrant clouds make the clouds change color in sunrise/sunset similar to bedrock edition. Cloud layers be changed in the `.minecraft/config/fabulous-clouds` config file:

```json
{
	"vibrant_clouds": true,
	"enable_default_cloud_layer": true,
	"cloud_layers": [
		{
			"offset": 64.0,
			"scale": 1.25,
			"speed": -1.0
		}
	]
}
```

This example enables the default vanilla cloud layer and adds a cloud layer 64 blocks above normal cloud height. The negative speed means it will go in the opposite direction to the default cloud layer. Cloud layers all have an `offset`, `scale` and `speed`:

`offset` is how many blocks above or below cloud height the layer is positioned. (default `0.0`)

`scale` is how big the cloud layer is. (default `1.0`)

`speed` is how fast the cloud layer moves. (default `1.0`)

You may add this mod to your modpack.
