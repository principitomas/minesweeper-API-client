package com.hiringtest.minesweeper.api.client

import com.hiringtest.minesweeper.api.client.error.ApiError
import com.hiringtest.minesweeper.api.client.error.ItemNotFoundError
import com.hiringtest.minesweeper.api.client.error.NetworkError
import com.hiringtest.minesweeper.api.client.error.UnknownError
import com.hiringtest.minesweeper.api.client.model.Credentials
import com.hiringtest.minesweeper.api.client.model.Game
import com.hiringtest.minesweeper.api.client.model.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.toUtf8Bytes

class MinesweeperApiClient constructor(
    httpClientEngine: HttpClientEngine? = null
) {
     var credentials: Credentials
        get() = this.credentials
        set(creds: Credentials) {
            this.credentials = creds
        }

    companion object {
        const val BASE_ENDPOINT = "http://minesweeperapi-env.eba-h2mmpfhs.us-east-2.elasticbeanstalk.com"
    }

    private val client: HttpClient = HttpClient(httpClientEngine!!) {
        install(JsonFeature) {
            serializer = KotlinxSerializer().apply {
                register(Game.serializer())
            }
        }

    }

    suspend fun getGames(): Either<ApiError, List<Game>> = try {
        val gamesJson = client.get<String>("$BASE_ENDPOINT/games") {

        }

        // JsonFeature does not working currently with root-level array
        // https://github.com/Kotlin/kotlinx.serialization/issues/179
        val games = Json.nonstrict.parse(Game.serializer().list, gamesJson)

        Either.Right(games)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun getGamesById(id: Int): Either<ApiError, Game> = try {
        val game = client.get<Game>("$BASE_ENDPOINT/games/$id")

        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun createGame(settings: Settings): Either<ApiError, Game> = try {
        val game = client.post<Game>("$BASE_ENDPOINT/games") {
            contentType(ContentType.Application.Json)
            body = settings
        }

        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun pauseResume(id: Int, action: String): Either<ApiError, Game> = try {
        val game = client.put<Game>("$BASE_ENDPOINT/games/$id") {
            parameter("action", action)
        }

        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    private fun handleError(exception: Exception): Either<ApiError, Nothing> =
        if (exception is BadResponseStatusException) {
            if (exception.statusCode.value == 404) {
                Either.Left(ItemNotFoundError)
            } else {
                Either.Left(UnknownError(exception.statusCode.value))
            }
        } else {
            Either.Left(NetworkError)
        }
}