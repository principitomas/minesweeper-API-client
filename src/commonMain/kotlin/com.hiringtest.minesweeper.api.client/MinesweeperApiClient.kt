package com.hiringtest.minesweeper.api.client

import com.hiringtest.minesweeper.api.client.error.ApiError
import com.hiringtest.minesweeper.api.client.error.ItemNotFoundError
import com.hiringtest.minesweeper.api.client.error.NetworkError
import com.hiringtest.minesweeper.api.client.error.UnknownError
import com.hiringtest.minesweeper.api.client.model.Credentials
import com.hiringtest.minesweeper.api.client.model.Game
import com.hiringtest.minesweeper.api.client.model.Settings
import com.hiringtest.minesweeper.api.client.model.UserAccount
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.features.BadResponseStatusException
import io.ktor.client.features.DefaultRequest.Feature.install
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.auth.AuthScheme
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
        const val BASIC_AUTH_HEADER = "Authorization"
        const val BASIC_AUTH_PREFIX = "Basic "
    }

    private val client = HttpClient(httpClientEngine!!) {
        install(JsonFeature) {
            serializer = KotlinxSerializer().apply {
                register(Game.serializer())
            }
        }
    }

    suspend fun getGames(): Either<ApiError, List<Game>> = try {
        val gamesJson = client.get<String>("$BASE_ENDPOINT/games") {
            header("$BASIC_AUTH_HEADER", getBasicAuthCredentials())
        }

        // JsonFeature does not working currently with root-level array
        // https://github.com/Kotlin/kotlinx.serialization/issues/179
        val games = Json.nonstrict.parse(Game.serializer().list, gamesJson)

        Either.Right(games)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun getGamesById(id: Int): Either<ApiError, Game> = try {
        val game = client.get<Game>("$BASE_ENDPOINT/games/$id") {
            header("$BASIC_AUTH_HEADER", getBasicAuthCredentials())
        }
        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun createGame(settings: Settings): Either<ApiError, Game> = try {
        val game = client.post<Game>("$BASE_ENDPOINT/games") {
            contentType(ContentType.Application.Json)
            header("$BASIC_AUTH_HEADER", getBasicAuthCredentials())
            body = settings
        }

        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun pauseResume(id: Int, action: String): Either<ApiError, Game> = try {
        val game = client.put<Game>("$BASE_ENDPOINT/games/$id") {
            parameter("action", action)
            header("$BASIC_AUTH_HEADER", getBasicAuthCredentials())
        }

        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun addFlag(id: Int, column: Int, row: Int, flagType: String): Either<ApiError, Game> = try {
        val game = client.put<Game>("$BASE_ENDPOINT/games/$id/flag") {
            parameter("column", column)
            parameter("row", row)
            parameter("type", flagType)
            header("$BASIC_AUTH_HEADER", getBasicAuthCredentials())
        }

        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun reveal(id: Int, column: Int, row: Int): Either<ApiError, Game> = try {
        val game = client.put<Game>("$BASE_ENDPOINT/games/$id/reveal") {
            parameter("column", column)
            parameter("row", row)
            header("$BASIC_AUTH_HEADER", getBasicAuthCredentials())
        }

        Either.Right(game)
    } catch (e: Exception) {
        handleError(e)
    }

    suspend fun createUser(userAccount: UserAccount): Either<ApiError, Game> = try {
        val game = client.post<Game>("$BASE_ENDPOINT/users") {
            contentType(ContentType.Application.Json)
            header("$BASIC_AUTH_HEADER", getBasicAuthCredentials())
            body = userAccount
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

    private fun getBasicAuthCredentials() : String =
            ("$BASIC_AUTH_PREFIX" + this.credentials.user + ":" + this.credentials.password).encodeBase64ToString()

}