package swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import controller.GameRoutes
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.{Info => JInfo}
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.{Operation, PathItem, Paths, responses => OASResponses}
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.{ApiResponse => OASApiResponse, ApiResponses}
import io.swagger.v3.oas.models.media.{Schema, Content => OASContent, MediaType}

class SwaggerDocumentation extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set(classOf[GameRoutes])
  
  override val info: Info = Info(
    description = "API documentation for Pixel Battle Game",
    version = "1.0",
    title = "Pixel Battle API"
  )

  override val host = "localhost:8080"
  override val basePath = "/"
  override val apiDocsPath = "api-docs"
  override val schemes = List("http")
  val swaggerUiPath = "swagger"

  def swaggerJson: String = {
    val openApi = new OpenAPI()
      .info(new JInfo()
        .title(info.title)
        .version(info.version)
        .description(info.description))

    val paths = new Paths()

    // POST /game - Create new game
    val createGamePath = new PathItem()
      .post(new Operation()
        .summary("Create a new game")
        .description("Creates a new pixel battle game instance")
        .responses(new ApiResponses()
          .addApiResponse("200", new OASApiResponse()
            .description("Game created successfully"))))

    // GET /game/{id} - Get game by ID
    val getGamePath = new PathItem()
      .get(new Operation()
        .summary("Get game by ID")
        .description("Retrieves a game instance by its ID")
        .addParametersItem(new Parameter()
          .name("id")
          .description("Game ID")
          .required(true)
          .schema({
            val s = new Schema[Long]()
            s.setType("integer")
            s.setFormat("int64")
            s
          }))
        .responses(new ApiResponses()
          .addApiResponse("200", new OASApiResponse().description("Game found"))
          .addApiResponse("404", new OASApiResponse().description("Game not found"))))

    // GET /game - Get current game
    val getCurrentGamePath = new PathItem()
      .get(new Operation()
        .summary("Get current game")
        .description("Retrieves the current active game instance")
        .responses(new ApiResponses()
          .addApiResponse("200", new OASApiResponse()
            .description("Current game retrieved successfully"))))

    // POST /game/pixel - Place pixel
    val placePixelPath = new PathItem()
      .post(new Operation()
        .summary("Place a pixel")
        .description("Places a pixel on the game field with specified coordinates and color")
        .addParametersItem(new Parameter()
          .name("x")
          .description("X coordinate")
          .required(true)
          .schema({
            val s = new Schema[Integer]()
            s.setType("integer")
            s
          }))
        .addParametersItem(new Parameter()
          .name("y")
          .description("Y coordinate")
          .required(true)
          .schema({
            val s = new Schema[Integer]()
            s.setType("integer")
            s
          }))
        .addParametersItem(new Parameter()
          .name("color")
          .description("Color of the pixel")
          .required(true)
          .schema({
            val s = new Schema[String]()
            s.setType("string")
            s
          }))
        .addParametersItem(new Parameter()
          .name("player_id")
          .description("ID of the player")
          .required(true)
          .schema({
            val s = new Schema[Long]()
            s.setType("integer")
            s.setFormat("int64")
            s
          }))
        .responses(new ApiResponses()
          .addApiResponse("200", new OASApiResponse().description("Pixel placed successfully"))
          .addApiResponse("400", new OASApiResponse().description("Invalid parameters or move"))
          .addApiResponse("404", new OASApiResponse().description("No active game found"))))

    paths
      .addPathItem("/game", createGamePath)
      .addPathItem("/game/{id}", getGamePath)
    
    // Add GET operation to existing /game path or create new if not exists
    var gamePath = paths.get("/game")
    if (gamePath == null) {
      gamePath = new PathItem()
      paths.addPathItem("/game", gamePath)
    }
    gamePath.setGet(getCurrentGamePath.getGet())

    paths.addPathItem("/game/pixel", placePixelPath)

    openApi.setPaths(paths)
    Json.mapper().writeValueAsString(openApi)
  }
} 