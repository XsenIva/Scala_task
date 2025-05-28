package swagger

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Game field representation")
case class GameFieldModel(
  @Schema(description = "Unique identifier of the game", required = true, example = "1")
  id: Long,
  
  @Schema(description = "Width of the game field", required = true, example = "100")
  width: Int,
  
  @Schema(description = "Height of the game field", required = true, example = "100")
  height: Int,
  
  @Schema(description = "Current state of the game field", required = true)
  field: Array[Array[String]]
)

@Schema(description = "Pixel placement request")
case class PixelRequest(
  @Schema(description = "X coordinate", required = true, example = "10")
  x: Int,
  
  @Schema(description = "Y coordinate", required = true, example = "20")
  y: Int,
  
  @Schema(description = "Color of the pixel", required = true, example = "#FF0000")
  color: String,
  
  @Schema(description = "ID of the player placing the pixel", required = true, example = "1")
  playerId: Long
)

@Schema(description = "Error response")
case class ErrorResponse(
  @Schema(description = "Error message", required = true, example = "Invalid parameters")
  message: String
) 